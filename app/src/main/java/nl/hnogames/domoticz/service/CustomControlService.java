package nl.hnogames.domoticz.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlButton;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.ToggleRangeTemplate;
import android.service.controls.templates.ToggleTemplate;
import android.util.Log;

import org.reactivestreams.FlowAdapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.reactivex.processors.ReplayProcessor;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.utils.WidgetUtils;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

@RequiresApi(api = Build.VERSION_CODES.R)
public class CustomControlService extends ControlsProviderService {
    private ArrayList<DevicesInfo> extendedStatusSwitches;
    private ReplayProcessor publisherForAll;
    private ReplayProcessor updatePublisher;
    private List activeControlIds;

    @Override
    public Flow.Publisher createPublisherForAllAvailable() {
        Log.d(TAG, "Creating publishers for all");
        publisherForAll = ReplayProcessor.create();
        processAll();
        return FlowAdapters.toFlowPublisher(publisherForAll);
    }

    private void processAll() {
        StaticHelper.getDomoticz(getApplicationContext()).getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                extendedStatusSwitches = mDevicesInfo;
                for (DevicesInfo device : extendedStatusSwitches) {
                    String name = device.getName();
                    if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) && DeviceUtils.isSupportedForExternalControl(device)) {
                        process(device);
                    }
                }
                if (publisherForAll != null) {
                    Log.d(TAG, "Completing all publisher");
                    publisherForAll.onComplete();
                }
                Log.d(TAG, "Done processing");
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            public void onError(Exception error) {
                String errorMessage = StaticHelper.getDomoticz(getApplicationContext()).getErrorMessage(error);
                Log.e(TAG, errorMessage);
            }
        }, 0, "all");
    }

    private void process(DevicesInfo device) {
        if (publisherForAll != null) {
            Control control = DeviceToControl(device, true);
            publisherForAll.onNext(control);
        }

        if (updatePublisher != null) {
            Control control = DeviceToControl(device, false);
            if (activeControlIds.contains(device.getIdx() + "")) {
                Log.d(TAG, "Adding stateful");
                updatePublisher.onNext(control);
            }
        }
    }

    private PendingIntent pi;
    public Control DeviceToControl(DevicesInfo d, boolean stateless) {
        if(pi == null) {
            Context context = getBaseContext();
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            pi = PendingIntent.getActivity(context, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d(TAG, "Created pendingintent for " + d.getName() + " with intent id " + 101 + " for device id " + d.getIdx());
        }

        String description = d.getDescription();
        if (description.contains("|"))
            description = description.substring(description.lastIndexOf("|") + 1).replace("controlicz:", "").replace("Controlicz:", "").trim();
        if(UsefulBits.isEmpty(description))
            description = d.getType();

        String status = d.getStatus() != null ? d.getStatus() : "";
        if (status.contains("|"))
            status = status.substring(status.lastIndexOf("|") + 1);

        if(!stateless) {
            Control.StatefulBuilder builder = new Control.StatefulBuilder(d.getIdx() + "", pi)
                    .setTitle(d.getName())
                    .setStructure(d.getPlanID());
            builder.setStatus(Control.STATUS_OK);
            builder.setStatusText(status);
            if(!UsefulBits.isEmpty(description))
                builder.setSubtitle(description);
            ControlTemplate template = getControlTemplate(d);
            if (template != null)
                builder.setControlTemplate(template);
            builder.setDeviceType(getDeviceType(d));
            return builder.build();
        }
        else return new Control.StatelessBuilder(d.getIdx() + "", pi)
                    .setTitle(d.getName())
                    .setSubtitle(description)
                    .setStructure(d.getPlanID())
                    .setDeviceType(getDeviceType(d)).build();
    }

    public ControlTemplate getControlTemplate(DevicesInfo mDeviceInfo) {
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            switch (mDeviceInfo.getType()) {
                case DomoticzValues.Scene.Type.GROUP:
                    return new ToggleTemplate(mDeviceInfo.getIdx() + "_toggle", new ControlButton(mDeviceInfo.getStatusBoolean(), "toggle"));
                case DomoticzValues.Scene.Type.SCENE:
                    return null;
                case DomoticzValues.Device.Utility.Type.THERMOSTAT:
            }
        } else {
            switch (mDeviceInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.DOORLOCK:
                case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    return new ToggleTemplate(mDeviceInfo.getIdx() + "_toggle", new ControlButton(mDeviceInfo.getStatusBoolean(), "toggle"));

                case DomoticzValues.Device.Type.Value.X10SIREN:
                case DomoticzValues.Device.Type.Value.MOTION:
                case DomoticzValues.Device.Type.Value.CONTACT:
                case DomoticzValues.Device.Type.Value.DUSKSENSOR:
                case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                case DomoticzValues.Device.Type.Value.DOORBELL:
                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    return null;

                case DomoticzValues.Device.Type.Value.DIMMER:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                    int maxValue = mDeviceInfo.getMaxDimLevel() <= 0 ? 100 : mDeviceInfo.getMaxDimLevel();
                    return new ToggleRangeTemplate(mDeviceInfo.getIdx() + "_toggle", new ControlButton(mDeviceInfo.getStatusBoolean(), "toggle"), new RangeTemplate(mDeviceInfo.getIdx() + "_range", 0.0f,
                            maxValue,
                            mDeviceInfo.getLevel() > maxValue ? maxValue : mDeviceInfo.getLevel(),
                            1, "%d"));
                default:
                    return new ToggleTemplate(mDeviceInfo.getIdx() + "_toggle", new ControlButton(mDeviceInfo.getStatusBoolean(), "toggle"));
            }
        }
        return new ToggleTemplate(mDeviceInfo.getIdx() + "_toggle", new ControlButton(mDeviceInfo.getStatusBoolean(), "toggle"));
    }

    public int getDeviceType(DevicesInfo mDeviceInfo) {
        return DomoticzIcons.getDrawableIconForGoogle(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage());
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        Log.d(TAG, "Creating publishers for " + Integer.toString(controlIds.size()));
        updatePublisher = ReplayProcessor.create();
        activeControlIds = controlIds;
        processAll();
        return FlowAdapters.toFlowPublisher(updatePublisher);
    }

    @Override
    public void performControlAction(String controlId, ControlAction action,
                                     Consumer consumer) {
        try {
            Log.i(TAG, "pca " + controlId + " act " + action);

            DevicesInfo device = null;
            for (DevicesInfo d : extendedStatusSwitches) {
                if ((d.getIdx() + "").equals(controlId))
                    device = d;
            }
            if (device != null) {
                if (action instanceof BooleanAction) {
                    boolean newState = ((BooleanAction) action).getNewState();
                    handleSwitch(getApplicationContext(), device, null, !newState, null);
                }
                consumer.accept(ControlAction.RESPONSE_OK);
            } else
                consumer.accept(ControlAction.RESPONSE_FAIL);
        } catch (Exception ex) {
            Log.e("pca", ex.getMessage());
        }
    }

    private void handleSwitch(final Context context, DevicesInfo mDevicesInfo, final String password, final boolean checked, final String value) {
        if (mDevicesInfo == null)
            return;

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
        int jsonValue = 0;

        if (!mDevicesInfo.isSceneOrGroup()) {
            if (!checked) {
                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                if (!UsefulBits.isEmpty(value)) {
                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                    jsonValue = getSelectorValue(mDevicesInfo, value);
                }
            } else {
                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                if (!UsefulBits.isEmpty(value)) {
                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                    jsonValue = 0;
                    if (mDevicesInfo.getStatus() != value)//before turning stuff off check if the value is still the same as the on value (else something else took over)
                        return;
                }
            }

            if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                    mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
                    mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
                if (checked)
                    jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                else
                    jsonAction = DomoticzValues.Device.Switch.Action.ON;
            } else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON)
                jsonAction = DomoticzValues.Device.Switch.Action.ON;
            else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON)
                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
        } else {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            if (!checked) {
                jsonAction = DomoticzValues.Scene.Action.ON;
            } else
                jsonAction = DomoticzValues.Scene.Action.OFF;
            if (mDevicesInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                jsonAction = DomoticzValues.Scene.Action.ON;
        }
        StaticHelper.getDomoticz(context).setAction(mDevicesInfo.getIdx(), jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                WidgetUtils.RefreshWidgets(context);
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
        if (mDevicesInfo == null || mDevicesInfo.getLevelNames() == null)
            return 0;
        int jsonValue = 0;
        if (!UsefulBits.isEmpty(value)) {
            ArrayList<String> levelNames = mDevicesInfo.getLevelNames();
            int counter = 0;
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
}