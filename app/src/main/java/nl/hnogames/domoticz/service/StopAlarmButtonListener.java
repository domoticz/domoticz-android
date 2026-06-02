
package nl.hnogames.domoticz.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class StopAlarmButtonListener extends IntentService {
    public StopAlarmButtonListener() {
        super("Stop Alarm");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent stopIntent = new Intent(this, RingtonePlayingService.class);
        this.stopService(stopIntent);
    }
}
