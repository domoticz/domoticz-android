package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.TimersAdapter;
import nl.hnogames.domoticz.Containers.SwitchTimerInfo;
import nl.hnogames.domoticz.R;

/**
 * Created by m.heinis on 11/12/2015.
 */
public class SwitchTimerInfoDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private ArrayList<SwitchTimerInfo> info;
    private String idx;
    private Context mContext;

    public SwitchTimerInfoDialog(Context c,
                                 ArrayList<SwitchTimerInfo> _info,
                                 int layout) {
        this.info = _info;
        this.mContext = c;

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(layout, true)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    public void show() {
        mdb.title("Timers");
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = (ListView) view.findViewById(R.id.list);
        TimersAdapter adapter = new TimersAdapter(mContext, info);
        listView.setAdapter(adapter);

        md.show();
    }

}
