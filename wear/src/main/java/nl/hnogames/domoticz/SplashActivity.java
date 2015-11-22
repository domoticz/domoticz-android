package nl.hnogames.domoticz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SplashActivity extends DomoticzActivity implements
        MessageApi.MessageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        super.onMessageReceived(messageEvent);
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
    }
}
