
package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.NotificationsAdapter;
import nl.hnogames.domoticzapi.Containers.NotificationInfo;

public class NotificationInfoDialog {

    private final MaterialDialog.Builder mdb;
    private final ArrayList<NotificationInfo> info;
    private final Context mContext;

    public NotificationInfoDialog(Context c,
                                  ArrayList<NotificationInfo> _info) {
        this.info = _info;
        this.mContext = c;

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_switch_timer, true)
                .positiveText(android.R.string.ok);
    }

    public void show() {
        mdb.title(R.string.category_notification);
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = view.findViewById(R.id.list);
        NotificationsAdapter adapter = new NotificationsAdapter(mContext, info);
        listView.setAdapter(adapter);

        md.show();
    }
}
