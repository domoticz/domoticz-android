package nl.hnogames.domoticz.app;

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

import nl.hnogames.domoticz.R;

public class DomoticzActivity extends Activity implements
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks {

    public final String TAG = "WEARDEVICE";
    public final String SEND_DATA = "/send_data";
    public final String RECEIVE_DATA = "/receive_data";
    public GoogleApiClient mApiClient;
    public final String PREF_SWITCH = "SWITCHES";
    public final String SEND_SWITCH = "/send_switch";

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
        Wearable.MessageApi.addListener(mApiClient, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Wearable.MessageApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_main);
        initGoogleApiClient();//request new data
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        if (messageEvent.getPath().equalsIgnoreCase(SEND_DATA)) {
            String rawData = new String(messageEvent.getData());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_SWITCH, rawData).apply();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected " + bundle);
        sendMessage(RECEIVE_DATA, "");
    }

    public void sendMessage( final String path, final String text ) {
        Log.d(TAG, "Send: " + text);
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                    if (result.getStatus().isSuccess()) {
                        Log.v(TAG, "Message: {" + "my object" + "} sent to: " + node.getDisplayName());
                    } else {
                        Log.v(TAG, "ERROR: failed to send Message");
                    }
                }
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {}
}
