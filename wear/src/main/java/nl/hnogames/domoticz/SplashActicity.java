package nl.hnogames.domoticz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SplashActicity extends DomoticzActivity implements
        MessageApi.MessageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        if (messageEvent.getPath().equalsIgnoreCase(this.SEND_DATA)) {
            String rawData = new String(messageEvent.getData());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_SWITCH, rawData).apply();

            Intent intent = new Intent(this, WearActivity.class);
            startActivity(intent);
            this.finish();
        }
    }
}
