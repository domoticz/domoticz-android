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

import org.reactivestreams.FlowAdapters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;

@RequiresApi(api = Build.VERSION_CODES.R)
public class CustomControlService extends ControlsProviderService {
    private ReplayProcessor updatePublisher;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public Flow.Publisher createPublisherForAllAvailable() {
        Context context = getBaseContext();
        Intent i = new Intent();
        PendingIntent pi = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        List controls = new ArrayList<>();
        Control control = new Control.StatelessBuilder("domoticz1", pi)
                .setTitle("Lights")
                .setSubtitle("Living room")
                .setStructure("House")
                .setDeviceType(DeviceTypes.TYPE_LIGHT)
                .build();
        controls.add(control);
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls));
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
