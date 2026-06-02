package nl.hnogames.domoticz.app;

import android.annotation.TargetApi;
import android.app.assist.AssistContent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class AppCompatAssistActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable edge-to-edge display for Android 15+ compatibility
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        // Apply window insets to the root view
        applyWindowInsets();
    }

    /**
     * Apply window insets to handle edge-to-edge display properly.
     * Based on Android 15 edge-to-edge best practices.
     * See: https://medium.com/androiddevelopers/insets-handling-tips-for-android-15s-edge-to-edge-enforcement
     */
    protected void applyWindowInsets() {
        // Handle CoordinatorLayout + AppBarLayout (MainActivity layouts)
        handleCoordinatorLayoutWithAppBar();

        // Handle standalone Toolbar layouts (GraphActivity, Settings, etc.)
        handleStandaloneToolbar();

        // Handle bottom content (AdView, etc.)
        handleBottomContent();
    }

    /**
     * Handle CoordinatorLayout with AppBarLayout using fitsSystemWindows approach.
     * This allows AppBarLayout to draw edge-to-edge under system bars.
     */
    private void handleCoordinatorLayoutWithAppBar() {
        androidx.coordinatorlayout.widget.CoordinatorLayout coordinator =
            findViewById(getResources().getIdentifier("coordinator_layout", "id", getPackageName()));
        com.google.android.material.appbar.AppBarLayout appBar = findViewById(getAppBarId());

        if (coordinator != null && appBar != null) {
            // Set fitsSystemWindows on both CoordinatorLayout and AppBarLayout
            // This makes AppBarLayout draw under the status bar automatically
            coordinator.setFitsSystemWindows(true);
            appBar.setFitsSystemWindows(true);

            // Handle main content to avoid being hidden under AppBarLayout
            handleContentUnderAppBar(appBar);
        }
    }

    /**
     * Apply insets to main content that scrolls under AppBarLayout.
     * This ensures content appears after the AppBarLayout is laid out.
     */
    private void handleContentUnderAppBar(com.google.android.material.appbar.AppBarLayout appBar) {
        // Find the scrolling content (NestedScrollView or RecyclerView)
        androidx.core.widget.NestedScrollView scrollView =
            findViewById(getResources().getIdentifier("nested_scroll_view", "id", getPackageName()));
        androidx.recyclerview.widget.RecyclerView recyclerView =
            findViewById(getResources().getIdentifier("RecyclerView", "id", getPackageName()));

        android.view.View scrollingView = scrollView != null ? scrollView : recyclerView;

        if (scrollingView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(scrollingView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );

                // Apply padding after AppBarLayout is laid out to account for its height
                appBar.post(() -> {
                    v.setPadding(
                        insets.left,
                        appBar.getHeight(), // Use AppBar height instead of status bar
                        insets.right,
                        insets.bottom
                    );
                });

                // For RecyclerView/NestedScrollView, set clipToPadding=false for immersive scrolling
                if (v instanceof androidx.recyclerview.widget.RecyclerView) {
                    ((androidx.recyclerview.widget.RecyclerView) v).setClipToPadding(false);
                } else if (v instanceof androidx.core.widget.NestedScrollView) {
                    ((androidx.core.widget.NestedScrollView) v).setClipToPadding(false);
                }

                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Handle standalone Toolbar layouts (not inside AppBarLayout).
     * Apply insets using ViewCompat listener for consistent behavior.
     */
    private void handleStandaloneToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(getToolbarId());

        if (toolbar != null) {
            // Check if toolbar is standalone (not in AppBarLayout)
            android.view.ViewParent parent = toolbar.getParent();
            boolean isStandalone = !(parent instanceof com.google.android.material.appbar.CollapsingToolbarLayout);
            if (parent != null && parent.getParent() instanceof com.google.android.material.appbar.AppBarLayout) {
                isStandalone = false;
            }

            if (isStandalone) {
                // Find the root layout containing the toolbar
                android.view.View rootLayout = findViewById(getResources().getIdentifier("phoneLayoutWrapper", "id", getPackageName()));
                if (rootLayout == null) {
                    rootLayout = (android.view.View) toolbar.getParent();
                }

                final android.view.View finalRootLayout = rootLayout;
                ViewCompat.setOnApplyWindowInsetsListener(finalRootLayout, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                    );

                    // Apply padding to push toolbar down from status bar
                    if (toolbar.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                        android.view.ViewGroup.MarginLayoutParams params =
                            (android.view.ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                        params.topMargin = insets.top;
                        toolbar.setLayoutParams(params);
                    }

                    // Handle main content area
                    android.view.View mainContent = findViewById(getResources().getIdentifier("main", "id", getPackageName()));
                    if (mainContent != null) {
                        mainContent.setPadding(
                            insets.left,
                            0, // Toolbar already handles top
                            insets.right,
                            insets.bottom
                        );
                    }

                    return WindowInsetsCompat.CONSUMED;
                });
            }
        }
    }

    /**
     * Handle bottom content like AdView to ensure it doesn't hide under navigation bar.
     */
    private void handleBottomContent() {
        int adViewId = getResources().getIdentifier("adView", "id", getPackageName());
        if (adViewId != 0) {
            android.view.View adView = findViewById(adViewId);
            if (adView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(adView, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    if (v.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                        android.view.ViewGroup.MarginLayoutParams params =
                            (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
                        params.bottomMargin = insets.bottom;
                        v.setLayoutParams(params);
                    }

                    return windowInsets;
                });
            }
        }
    }

    /**
     * Override this to return the toolbar ID if your activity has a toolbar
     * Default is R.id.toolbar
     */
    protected int getToolbarId() {
        return getResources().getIdentifier("toolbar", "id", getPackageName());
    }

    /**
     * Override this to return the AppBar ID if your activity has an AppBarLayout
     * Default is R.id.appBar
     */
    protected int getAppBarId() {
        return getResources().getIdentifier("appBar", "id", getPackageName());
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        super.onProvideAssistContent(outContent);
        try {
            outContent.setStructuredData(
                    new JSONObject()
                            .put("@type", "SoftwareApplication")
                            .put("author", "Domoticz")
                            .put("name", "Domoticz")
                            .put("id", "http://www.domoticz.com")
                            .put("description", "Domoticz is a very light weight home automation system that lets you monitor and configure miscellaneous devices, including lights, switches, various sensors/meters like temperature, rainfall, wind, ultraviolet (UV) radiation, electricity usage/production, gas consumption, water consumption and many more."
                            ).toString()
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
