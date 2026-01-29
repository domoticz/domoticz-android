
package nl.hnogames.domoticz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.util.Arrays;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SplashActivity extends DomoticzActivity implements
        MessageClient.OnMessageReceivedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(TAG, "Splash Receive: " + messageEvent.getPath() + " - " + Arrays.toString(messageEvent.getData()));
        if (messageEvent.getPath().equalsIgnoreCase(this.SEND_DATA)) {
            String rawData = new String(messageEvent.getData());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_SWITCH, rawData).apply();

            Intent intent = new Intent(this, WearActivity.class);
            startActivity(intent);
            this.finish();
        } else if (messageEvent.getPath().equalsIgnoreCase(SEND_ERROR)) {
            String errorMessage = new String(messageEvent.getData());
            if (errorMessage.equals(ERROR_NO_SWITCHES)) {
                Intent intent = new Intent(this, ErrorActivity.class);
                startActivity(intent);
                this.finish();
            }
        }
        super.onMessageReceived(messageEvent);
    }
}
