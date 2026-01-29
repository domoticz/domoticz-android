
package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.TimersAdapter;
import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;

public class SwitchTimerInfoDialog {

    private final MaterialDialog.Builder mdb;
    private final ArrayList<SwitchTimerInfo> info;
    private final Context mContext;

    public SwitchTimerInfoDialog(Context c,
                                 ArrayList<SwitchTimerInfo> _info,
                                 int layout) {
        this.info = _info;
        this.mContext = c;

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(layout, true)
                .positiveText(android.R.string.ok);
    }

    public void show() {
        mdb.title(R.string.timers);
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = view.findViewById(R.id.list);
        TimersAdapter adapter = new TimersAdapter(mContext, info);
        listView.setAdapter(adapter);
        md.show();
    }
}
