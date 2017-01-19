package nl.hnogames.domoticz.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import nl.hnogames.domoticz.Utils.NotificationUtil;

public class AutoMessageHeardReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int conversationId = intent.getIntExtra(NotificationUtil.MESSAGE_CONVERSATION_ID_KEY, -1);
        Log.d("Message", "id: " + conversationId);
        NotificationManagerCompat.from(context).cancel(conversationId);
    }
}
