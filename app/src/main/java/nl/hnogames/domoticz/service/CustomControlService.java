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
import android.util.Log;

import org.reactivestreams.FlowAdapters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.DashboardAdapter;
import nl.hnogames.domoticz.adapters.SwitchesAdapter;
import nl.hnogames.domoticz.helpers.StaticHelper;
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
            if (!name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                supportedSwitches.add(mDevicesInfo);
            }
        }
        for (DevicesInfo d: supportedSwitches) {
            stateControls.add(new Control.StatefulBuilder(d.getIdx()+"", activityIntent)
                    .setTitle(d.getName())
                    .setSubtitle(d.getDescription())
                    .setStructure(d.getPlanID())
                    .setDeviceType(getDeviceType(d))
                    .setStatus(Control.STATUS_OK)
                    .build());
        }
    }

    public int getDeviceType(DevicesInfo device)
    {
        if (device.getSwitchTypeVal() == 0 &&
                (device.getSwitchType() == null)) {
            switch (device.getType()) {
                case DomoticzValues.Scene.Type.GROUP:
                    return DeviceTypes.TYPE_LIGHT;

                case DomoticzValues.Scene.Type.SCENE:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;

                default:
                    return DeviceTypes.TYPE_LIGHT;

            }
        }else {
            switch (device.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                case DomoticzValues.Device.Type.Value.DOORLOCK:
                case DomoticzValues.Device.Type.Value.DOORLOCKINVERTED:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;


                case DomoticzValues.Device.Type.Value.X10SIREN:
                case DomoticzValues.Device.Type.Value.MOTION:
                case DomoticzValues.Device.Type.Value.CONTACT:
                case DomoticzValues.Device.Type.Value.DUSKSENSOR:
                case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                case DomoticzValues.Device.Type.Value.DOORBELL:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;

                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;


                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;


                case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;


                case DomoticzValues.Device.Type.Value.DIMMER:
                    return DeviceTypes.TYPE_LIGHT;


                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                    return DeviceTypes.TYPE_BLINDS;


                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                    return DeviceTypes.TYPE_BLINDS;


                case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                    return DeviceTypes.TYPE_BLINDS;


                default:
                    return DeviceTypes.TYPE_LIGHT;
            }
        }
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        Context context = getBaseContext();
        Intent i = new Intent();
        PendingIntent pi = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        updatePublisher = ReplayProcessor.create();

        // For each controlId in controlIds
        if (controlIds.contains("domoticz1")) {
            Control control = new Control.StatefulBuilder("domoticz1", pi)
                    .setTitle("Lights")
                    .setSubtitle("Living room")
                    .setStructure("House")
                    .setDeviceType(DeviceTypes.TYPE_LIGHT)
                    .setStatus(Control.STATUS_OK)
                    .build();
            updatePublisher.onNext(control);
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