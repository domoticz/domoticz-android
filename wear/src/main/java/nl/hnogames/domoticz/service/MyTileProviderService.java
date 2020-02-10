package nl.hnogames.domoticz.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.clockwork.tiles.TileData;
import com.google.android.clockwork.tiles.TileProviderService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.DevicesInfo;
import nl.hnogames.domoticz.utils.WearUsefulBits;

public class MyTileProviderService extends TileProviderService {
    private static final String TAG = MyTileProviderService.class.getSimpleName();
    private int id = -1;
    private ArrayList<DevicesInfo> switches = null;
    public final String PREF_SWITCH = "SWITCHES";

    private Thread updateJob = new Thread() {
        @Override
        public void run() {
            try {
                while (true) {
                    sendRemoteViews();
                    sleep(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onTileUpdate(int tileId) {
        Log.d(TAG, "onTileUpdate() called with: tileId = " + tileId);
        if (!isIdForDummyData(tileId)) {
            Log.d(TAG, "tileId is not dummy data " + tileId);
            id = tileId;
            sendRemoteViews();
        }
    }

    @Override
    public void onTileBlur(int tileId) {
        Log.d(TAG, "onTileBlur() called with: tileId = " + tileId);
        updateJob.interrupt();
    }

    @Override
    public void onTileFocus(int tileId) {
        Log.d(TAG, "onTileFocus() called with: tileId = " + tileId);
        id = tileId;
        updateJob.interrupt();
        updateJob.run();
    }

    private void sendRemoteViews() {
        Log.d(TAG, "sendRemoteViews");
        RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.activity_list_widget);

        // Load data of switches
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String switchesRawData = prefs.getString(PREF_SWITCH, "");
        Log.d(TAG, "switchesRawData found " + switchesRawData);
        if (!WearUsefulBits.isEmpty(switchesRawData)) {
            try {
                switches = new ArrayList<>();
                for (String s : new Gson().fromJson(switchesRawData, String[].class)) {
                    switches.add(new DevicesInfo(new JSONObject(s)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.v("WEAR", "Parsing error: " + e.getMessage());
            }
        }

        if(switches != null && switches.size()>0) {
            Log.d(TAG, "switches parsed " + switches.size());
            // Add views to the remote view for every switch
            updateViews.removeAllViews(R.id.widget_container);
            for (int i = 0; i < switches.size(); i++) {
                DevicesInfo s = switches.get(i);
                createSwitchView(updateViews, s);
            }
        }

        // Send the remote view
        TileData.Builder tileBuilder = new TileData.Builder();
        tileBuilder.setRemoteViews(updateViews);
        TileData bob = tileBuilder.build();

        Log.d(TAG, "sendData with id " + id);
        sendData(id, bob);
    }

    private void createSwitchView(RemoteViews updateViews, DevicesInfo s) {
        RemoteViews switchLayout = new RemoteViews(this.getPackageName(), R.layout.list_item);
        switchLayout.setTextViewText(R.id.name, s.getName());
        switchLayout.setTextViewText(R.id.status, s.getData());
        switchLayout.setImageViewResource(R.id.circle, Domoticz.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
        if (!s.getStatusBoolean())
            switchLayout.setInt(R.id.circle, "setAlpha", 100);
        else
            switchLayout.setInt(R.id.circle, "setAlpha", 255);
        updateViews.addView(R.id.widget_container, switchLayout);
    }
}
