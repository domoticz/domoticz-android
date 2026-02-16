
package nl.hnogames.domoticz.ui;

import android.content.Context;

import nl.hnogames.domoticz.R;

public class ScheduledTemperatureDialog extends TemperatureDialog {
    public ScheduledTemperatureDialog(Context mContext, double temp, boolean hasStep, double step, boolean hasMax, double max, boolean hasMin, double min, boolean canCancel, String vunit) {
        super(mContext, temp, hasStep, step, hasMax, max, hasMin, min, vunit);

        if (canCancel) {
            getMaterialDialogBuilder()
                    .neutralText(R.string.follow_schedule);
        }
    }
}
