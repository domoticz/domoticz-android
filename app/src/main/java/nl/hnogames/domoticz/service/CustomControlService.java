package nl.hnogames.domoticz.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.DeviceTypes;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlButton;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.StatelessTemplate;
import android.service.controls.templates.ToggleTemplate;
import android.util.Log;

import org.reactivestreams.FlowAdapters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

@RequiresApi(api = Build.VERSION_CODES.R)
public class CustomControlService extends ControlsProviderService {
    private ReplayProcessor updatePublisher;
    private List<Control> stateControls = new ArrayList<>();
    private ArrayList<DevicesInfo> extendedStatusSwitches;
    private PendingIntent activityIntent;

    @Override
    public void onCreate() {
        activityIntent = PendingIntent.getActivity(getBaseContext(), 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        getSwitches();
        super.onCreate();
    }

    @Override
    public Flow.Publisher createPublisherForAllAvailable() {
        Log.i(TAG, "cpa");
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(stateControls));
    }

    private void getSwitches() {
        extendedStatusSwitches = new ArrayList<>();
        StaticHelper.getDomoticz(getApplicationContext()).getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                extendedStatusSwitches = mDevicesInfo;
                processAllSwitches(extendedStatusSwitches);
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {}

            @Override
            public void onError(Exception error) {
                String errorMessage = StaticHelper.getDomoticz(getApplicationContext()).getErrorMessage(error);
                Log.e(TAG, errorMessage);
            }
        }, 0, "all");
    }

    private void processAllSwitches(ArrayList<DevicesInfo> extendedStatusSwitches) {
        ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo mDevicesInfo : extendedStatusSwitches) {
            String name = mDevicesInfo.getName();
            if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) && DeviceUtils.isSupportedForExternalControl(mDevicesInfo)) {
                supportedSwitches.add(mDevicesInfo);
            }
        }
        for (DevicesInfo d: supportedSwitches) {
            Control control = (new Control.StatefulBuilder(d.getIdx()+"", activityIntent)
                    .setTitle(d.getName())
                    .setSubtitle(d.getDescription())
                    .setStructure(d.getPlanID())
                    .setControlTemplate(getControlTemplate(d))
                    .setDeviceType(getDeviceType(d))
                    .setStatus(Control.STATUS_OK)
                    .setStatusText(d.getStatus() != null ? d.getStatus() : "")
                    .build());
            updateControl(control);
        }
    }

    private void processSwitch(DevicesInfo extendedStatusSwitches) {
        ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        supportedSwitches.add(extendedStatusSwitches);
        for (DevicesInfo d: supportedSwitches) {
            Control control = (new Control.StatefulBuilder(d.getIdx()+"", activityIntent)
                    .setTitle(d.getName())
                    .setSubtitle(d.getDescription())
                    .setStructure(d.getPlanID())
                    .setControlTemplate(getControlTemplate(d))
                    .setDeviceType(getDeviceType(d))
                    .setStatus(Control.STATUS_OK)
                    .setStatusText(d.getStatus() != null ? d.getStatus() : "")
                    .build());
            updateControl(control);
        }
    }

    public ControlTemplate getControlTemplate(DevicesInfo mDeviceInfo)
    {
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            if (mDeviceInfo.getSubType() != null && mDeviceInfo.getSubType().equals(DomoticzValues.Device.Utility.SubType.SMARTWARES)) {
                return new StatelessTemplate(mDeviceInfo.getIdx() + "_stateless");
            } else {
                switch (mDeviceInfo.getType()) {
                    case DomoticzValues.Scene.Type.GROUP:
                        return new ToggleTemplate(mDeviceInfo.getIdx() + "_toggle", new ControlButton(mDeviceInfo.getStatusBoolean(), "toggle"));
                    case DomoticzValues.Scene.Type.SCENE:
                        return new StatelessTemplate(mDeviceInfo.getIdx() + "_stateless");
                    case DomoticzValues.Device.Utility.Type.THERMOSTAT:
                }
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
                    return new StatelessTemplate(mDeviceInfo.getIdx() + "_stateless");

                    case DomoticzValues.Device.Type.Value.DIMMER:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                    int maxValue = mDeviceInfo.getMaxDimLevel() <= 0 ? 100 : mDeviceInfo.getMaxDimLevel();
                    return new RangeTemplate(mDeviceInfo.getIdx() + "_range",0.0f,
                            maxValue,
                            mDeviceInfo.getLevel() > maxValue ? maxValue : mDeviceInfo.getLevel(),
                            1, "%d");
                default:
                    return new StatelessTemplate(mDeviceInfo.getIdx() + "_stateless");
            }
        }
        return new StatelessTemplate(mDeviceInfo.getIdx() + "_stateless");
    }

    public int getDeviceType(DevicesInfo mDeviceInfo) {
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            if (mDeviceInfo.getSubType() != null && mDeviceInfo.getSubType().equals(DomoticzValues.Device.Utility.SubType.SMARTWARES)) {
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            } else {
                switch (mDeviceInfo.getType()) {
                    case DomoticzValues.Scene.Type.GROUP:
                        return DeviceTypes.TYPE_LIGHT;
                    case DomoticzValues.Scene.Type.SCENE:
                        return DeviceTypes.TYPE_GENERIC_ON_OFF;
                    case DomoticzValues.Device.Utility.Type.THERMOSTAT:
                }
            }
        } else {
            switch (mDeviceInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                case DomoticzValues.Device.Type.Value.DOORLOCK:
                case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    switch (mDeviceInfo.getSwitchType()) {
                        case DomoticzValues.Device.Type.Name.SECURITY:
                            return -1;
                        default:
                            return DeviceTypes.TYPE_GENERIC_ON_OFF;
                    }
                case DomoticzValues.Device.Type.Value.X10SIREN:
                case DomoticzValues.Device.Type.Value.MOTION:
                case DomoticzValues.Device.Type.Value.CONTACT:
                case DomoticzValues.Device.Type.Value.DUSKSENSOR:
                case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                case DomoticzValues.Device.Type.Value.DOORBELL:
                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case DomoticzValues.Device.Type.Value.DIMMER:
                    return DeviceTypes.TYPE_LIGHT;
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                    return DeviceTypes.TYPE_BLINDS;
                default:
                    return -1;
            }
        }
        return DeviceTypes.TYPE_GENERIC_ON_OFF;
    }

    private void updateControl(Control control) {
        stateControls = stateControls.stream().filter(c -> !c.getControlId().equals(control.getControlId())).collect(Collectors.toList());
        stateControls.add(control);
        if (updatePublisher != null)
            updatePublisher.onNext(control);
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        updatePublisher = ReplayProcessor.create();
        getSwitches();
        for(String id : controlIds)
        {
            Log.i(TAG, "cpf "+ id);
            for (DevicesInfo d: extendedStatusSwitches) {
                if((d.getIdx()+"").equals(id))
                    processSwitch(d);
            }
        }
        return FlowAdapters.toFlowPublisher(updatePublisher);
    }

    @Override
    public void performControlAction(String controlId, ControlAction action,
                                     Consumer consumer) {
        if (action instanceof BooleanAction) {
            Context context = getBaseContext();
            Intent i = new Intent();
            PendingIntent pi = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
            consumer.accept(ControlAction.RESPONSE_OK);
            Control control = new Control.StatefulBuilder("domoticz1", pi)
                    .setTitle("Lights")
                    .setSubtitle("Living room")
                    .setStructure("House")
                    .setDeviceType(DeviceTypes.TYPE_LIGHT)
                    .setStatus(Control.STATUS_OK)
                    .build();
            updatePublisher.onNext(control);
        }
    }
}