package nl.hnogames.domoticz.Service;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;


/**
 * Created by M. Heinis on 3-11-2015.
 */
public class WearMessageListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {

    private String responseDetails = "";
    private Domoticz domoticz;

    private static final String TAG = "WEARLISTENER";
    private static final String SEND_DATA = "/send_data";
    private static final String RECEIVE_DATA = "/receive_data";
    private static final String SEND_ERROR = "/error";

    private static final String ERROR_NO_SWITCHES = "NO_SWITCHES";
    private static GoogleApiClient mApiClient;

    private static final String SEND_SWITCH = "/send_switch";
    private ArrayList<ExtendedStatusInfo> extendedStatusSwitches;

    private int currentSwitch = 1;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(RECEIVE_DATA)) {
            Log.v("WEAR SERVICE", "Get data from Domoticz");
            if (mApiClient == null || mApiClient.equals(null)) {
                Log.v("WEAR SERVICE", "Init Google Wear API");
                initGoogleApiClient();
            }
            else
                getSwitches();
        } else  if (messageEvent.getPath().equalsIgnoreCase(SEND_SWITCH)) {
            Log.v("WEAR SERVICE", "Toggle Switch request received");
            String data = new String(messageEvent.getData());
            try {
                ExtendedStatusInfo selectedSwitch = new ExtendedStatusInfo(new JSONObject(data));
                domoticz=new Domoticz(getApplicationContext());

                if(selectedSwitch!=null)
                {
                    switch (selectedSwitch.getSwitchTypeVal()) {
                        case Domoticz.Device.Type.Value.ON_OFF:
                        case Domoticz.Device.Type.Value.MEDIAPLAYER:
                        case Domoticz.Device.Type.Value.X10SIREN:
                        case Domoticz.Device.Type.Value.DOORLOCK:
                        case Domoticz.Device.Type.Value.BLINDS:
                        case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
                            onSwitchToggle(selectedSwitch);
                            break;

                        case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                        case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                        case Domoticz.Device.Type.Value.DOORBELL:
                            //push on
                            onButtonClick(selectedSwitch.getIdx(), true);
                            break;

                        case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                            //push off

                            onButtonClick(selectedSwitch.getIdx(), false);
                            break;

                        default:
                            throw new NullPointerException(
                                    "No supported switch type defined in the adapter (setSwitchRowData)");
                    }

                    //now send latest status
                    getSwitches();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }



    private void getSwitches() {
        extendedStatusSwitches = new ArrayList<>();

        currentSwitch = 1;
        domoticz = new Domoticz(getApplicationContext());
        domoticz.getSwitches(new SwitchesReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                for (SwitchInfo switchInfo : switches) {
                    int idx = switchInfo.getIdx();
                    final int totalNumberOfSwitches = switches.size();

                    domoticz.getStatus(idx, new StatusReceiver() {
                        @Override
                        public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {
                            extendedStatusSwitches.add(extendedStatusInfo);     // Add to array
                            if (currentSwitch == totalNumberOfSwitches) {
                                processAllSwitches(extendedStatusSwitches);         // All extended info is in
                            } else currentSwitch++;                               // Not there yet
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e(TAG, error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private void processAllSwitches(ArrayList<ExtendedStatusInfo> extendedStatusSwitches) {
        final List<Integer> appSupportedSwitchesValues = domoticz.getWearSupportedSwitchesValues();
        final List<String> appSupportedSwitchesNames = domoticz.getWearSupportedSwitchesNames();

        ArrayList<ExtendedStatusInfo> supportedSwitches = new ArrayList<>();

        for (ExtendedStatusInfo mExtendedStatusInfo : extendedStatusSwitches) {
            String name = mExtendedStatusInfo.getName();
            int switchTypeVal = mExtendedStatusInfo.getSwitchTypeVal();
            String switchType = mExtendedStatusInfo.getSwitchType();

            if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                    appSupportedSwitchesValues.contains(switchTypeVal) &&
                    appSupportedSwitchesNames.contains(switchType) &&
                    mExtendedStatusInfo.getFavoriteBoolean()) {//only dashboard switches..
                supportedSwitches.add(mExtendedStatusInfo);
            }
        }
        if (supportedSwitches.size() > 0) {
            String parsedData = new Gson().toJson(supportedSwitches);
            Log.v(TAG, "Sending data: " + parsedData);
            sendMessage(SEND_DATA, parsedData);
        } else {
            Log.v(TAG, "Sending error to wearable: no switches on dashboard");
            sendMessage(SEND_ERROR, ERROR_NO_SWITCHES);
        }
    }

    public static void sendMessage(final String path, final String text) {
        Log.d("WEAR Message", "Send: " + text);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();

                    if (result.getStatus().isSuccess()) {
                        Log.v("WEAR", "Message: {" + "my object" + "} sent to: " + node.getDisplayName());
                    } else {
                        Log.v("WEAR", "ERROR: failed to send Message");
                    }
                }
            }
        }).start();
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("WEAR SERVICE", "Google Wear API Connected");
        getSwitches();
    }

    @Override
    public void onConnectionSuspended(int i) {}


    public void onSwitchToggle(ExtendedStatusInfo toggledDevice) {
        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        boolean checked = !toggledDevice.getStatusBoolean();
        if (toggledDevice.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                toggledDevice.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
            if (checked) jsonAction = Domoticz.Device.Switch.Action.OFF;
            else jsonAction = Domoticz.Device.Switch.Action.ON;
        } else {
            if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
            else jsonAction = Domoticz.Device.Switch.Action.OFF;
        }

        domoticz.setAction(toggledDevice.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }

    public void onButtonClick(int idx, boolean checked) {
        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction = Domoticz.Device.Switch.Action.OFF;

        domoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }
}

