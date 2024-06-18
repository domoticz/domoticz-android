package nl.hnogames.domoticz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nl.hnogames.domoticz.utils.WidgetUtils;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            // Refresh your widgets here
            WidgetUtils.RefreshWidgets(context.getApplicationContext());
        }
    }
}