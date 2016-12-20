package nl.hnogames.domoticz.Service;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class RingtonePlayingService extends Service {
    private Ringtone ringtone;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(this.ringtone != null && this.ringtone.isPlaying())
            return START_NOT_STICKY;//only start once

        Uri ringtoneUri = Uri.parse(intent.getExtras().getString("ringtone-uri"));
        this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        ringtone.play();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ringtone != null)
            ringtone.stop();
    }
}