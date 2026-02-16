package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.app.AppController;

public class FirebaseConfigHelper {
    private static final String TAG = "FirebaseConfigHelper";

    public interface TestCallback {
        void onSuccess(String token);
        void onError(String error);
    }

    /**
     * Initialize Firebase with user-provided configuration
     */
    public static boolean initializeFirebase(Context context, SharedPrefUtil prefs) {
        if (!prefs.hasFirebaseConfig()) {
            Log.d(TAG, "No Firebase configuration found");
            return false;
        }

        try {
            // Check if default Firebase app is already initialized
            try {
                FirebaseApp defaultApp = FirebaseApp.getInstance();
                if (defaultApp != null) {
                    // Delete existing instance to reinitialize with new config
                    defaultApp.delete();
                    Log.d(TAG, "Deleted existing Firebase app for reinitialization");
                }
            } catch (IllegalStateException e) {
                // No existing app, which is fine
                Log.d(TAG, "No existing Firebase app to delete");
            }

            // Create Firebase options from user configuration
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId(prefs.getFcmProjectId())
                    .setApplicationId(prefs.getFcmAppId())
                    .setApiKey(prefs.getFcmApiKey())
                    .setGcmSenderId(prefs.getFcmSenderId())
                    .build();

            // Initialize Firebase app as default instance
            FirebaseApp.initializeApp(context, options);

            Log.d(TAG, "Firebase initialized successfully with user configuration");

            // Get FCM token
            getFirebaseToken(context, new TestCallback() {
                @Override
                public void onSuccess(String token) {
                    Log.d(TAG, "FCM Token retrieved: " + token);
                    // Send token to server
                    GCMUtils.sendRegistrationIdToBackend(context, token);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to retrieve FCM token: " + error);
                }
            });

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
            return false;
        }
    }

    /**
     * Delete existing Firebase app instance
     */
    private static void deleteExistingFirebaseApp() {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            if (app != null) {
                app.delete();
                Log.d(TAG, "Deleted existing Firebase app");
            }
        } catch (IllegalStateException e) {
            // App doesn't exist, which is fine
            Log.d(TAG, "No existing Firebase app to delete");
        }
    }

    /**
     * Get Firebase messaging instance
     */
    public static FirebaseMessaging getFirebaseMessaging() {
        try {
            // After initializing FirebaseApp, the default instance is used
            return FirebaseMessaging.getInstance();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Firebase not initialized", e);
            return null;
        }
    }

    /**
     * Get FCM token
     */
    public static void getFirebaseToken(Context context, TestCallback callback) {
        try {
            FirebaseMessaging messaging = getFirebaseMessaging();
            if (messaging == null) {
                if (callback != null) {
                    callback.onError("Firebase Messaging not initialized");
                }
                return;
            }

            messaging.getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        if (callback != null) {
                            callback.onSuccess(token);
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting Firebase token", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    /**
     * Test the Firebase configuration
     */
    public static void testConfiguration(Context context, TestCallback callback) {
        SharedPrefUtil prefs = new SharedPrefUtil(context);

        if (!prefs.hasFirebaseConfig()) {
            if (callback != null) {
                callback.onError("No Firebase configuration found");
            }
            return;
        }

        // Try to initialize and get token
        boolean initialized = initializeFirebase(context, prefs);
        if (!initialized) {
            if (callback != null) {
                callback.onError("Failed to initialize Firebase");
            }
            return;
        }

        // Get token to verify configuration works
        getFirebaseToken(context, callback);
    }

    /**
     * Check if Firebase is configured
     */
    public static boolean isFirebaseConfigured(Context context) {
        SharedPrefUtil prefs = new SharedPrefUtil(context);
        return prefs.hasFirebaseConfig();
    }
}

