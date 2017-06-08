package nl.hnogames.domoticz.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Containers.SpeechInfo;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class AutoMessageReplyReceiver extends BroadcastReceiver {

    private Context mContext;
    private Domoticz domoticz;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
        mContext = context;
        int conversationId = intent.getIntExtra(NotificationUtil.MESSAGE_CONVERSATION_ID_KEY, -1);
        Log.d("Message", "id: " + conversationId);
        NotificationManagerCompat.from(context).cancel(conversationId);
        String message = getMessageFromIntent(intent);
        if (!UsefulBits.isEmpty(message))
            showSpeechResults(message);
    }

    private void showSpeechResults(String text) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(mContext);
        int jsonAction = -1;
        String actionFound = "Toggle";
        String SPEECH_ID = text.toLowerCase().trim();
        if (mSharedPrefs.isSpeechEnabled()) {
            ArrayList<SpeechInfo> qrList = mSharedPrefs.getSpeechList();
            SpeechInfo foundSPEECH = null;
            if (qrList != null && qrList.size() > 0) {
                for (SpeechInfo n : qrList) {
                    if (n.getId().equals(SPEECH_ID))
                        foundSPEECH = n;
                }
            }

            if (foundSPEECH == null) {
                if (SPEECH_ID.endsWith(mContext.getString(R.string.button_state_off).toLowerCase())) {
                    actionFound = mContext.getString(R.string.button_state_off);
                    SPEECH_ID = SPEECH_ID.replace(mContext.getString(R.string.button_state_off).toLowerCase(), "").trim();
                    jsonAction = 0;
                } else if (SPEECH_ID.endsWith(mContext.getString(R.string.button_state_on).toLowerCase())) {
                    actionFound = mContext.getString(R.string.button_state_on);
                    SPEECH_ID = SPEECH_ID.replace(mContext.getString(R.string.button_state_on).toLowerCase(), "").trim();
                    jsonAction = 1;
                }

                if (qrList != null && qrList.size() > 0) {
                    for (SpeechInfo n : qrList) {
                        if (n.getId().equals(SPEECH_ID))
                            foundSPEECH = n;
                    }
                }
            }

            if (foundSPEECH != null && foundSPEECH.isEnabled()) {
                handleSwitch(foundSPEECH.getSwitchIdx(), foundSPEECH.getSwitchPassword(), jsonAction, foundSPEECH.getValue());
                Toast.makeText(mContext, mContext.getString(R.string.Speech) + ": " + SPEECH_ID + " - " + actionFound, Toast.LENGTH_SHORT).show();
            } else {
                if (foundSPEECH == null)
                    Toast.makeText(mContext, mContext.getString(R.string.Speech_found) + ": " + SPEECH_ID, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, mContext.getString(R.string.Speech_disabled) + ": " + SPEECH_ID, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSwitch(final int idx, final String password, final int inputJSONAction, final String value) {
        if (domoticz == null)
            domoticz = new Domoticz(mContext, AppController.getInstance().getRequestQueue());

        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                int jsonAction;
                int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
                int jsonValue = 0;

                if (inputJSONAction < 0) {
                    if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                        mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                        if (!mDevicesInfo.getStatusBoolean())
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        else {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        }
                    } else {
                        if (!mDevicesInfo.getStatusBoolean()) {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        } else
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                    }
                } else {
                    if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                        mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                        if (inputJSONAction == 1)
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        else {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        }
                    } else {
                        if (inputJSONAction == 1) {
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            if (!UsefulBits.isEmpty(value)) {
                                jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                jsonValue = getSelectorValue(mDevicesInfo, value);
                            }
                        } else
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                    }
                }

                switch (mDevicesInfo.getSwitchTypeVal()) {
                    case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                        break;
                    case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        break;
                }

                domoticz.setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    @DebugLog
                    public void onReceiveResult(String result) {
                        Log.d("AUTO", result);
                    }

                    @Override
                    @DebugLog
                    public void onError(Exception error) {
                    }
                });
            }

            @Override
            public void onError(Exception error) {
            }

        }, idx, false);
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
        int jsonValue = 0;
        if (!UsefulBits.isEmpty(value)) {
            ArrayList<String> levelNames = mDevicesInfo.getLevelNames();
            int counter = 10;
            for (String l : levelNames) {
                if (l.equals(value))
                    break;
                else
                    counter += 10;
            }
            jsonValue = counter;
        }
        return jsonValue;
    }

    private String getMessageFromIntent(Intent intent) {
        //Note that Android Auto does not currently allow voice responses in their simulator
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null && remoteInput.containsKey("extra_voice_reply")) {
            return remoteInput.getCharSequence("extra_voice_reply").toString();
        }
        return null;
    }
}
