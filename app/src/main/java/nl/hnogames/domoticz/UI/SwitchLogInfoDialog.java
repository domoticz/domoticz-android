package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchLogInfo;
import nl.hnogames.domoticz.R;

/**
 * Created by m.heinis on 11/12/2015.
 */
public class SwitchLogInfoDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private ArrayList<SwitchLogInfo> info;
    private String idx;
    private Context mContext;

    public SwitchLogInfoDialog(Context c,
                               ArrayList<SwitchLogInfo> _info,
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
        mdb.title("Log");
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = (ListView) view.findViewById(R.id.list);

        String[] listData = processLogs();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, listData);
        listView.setAdapter(adapter);

        md.show();
    }

    public String[] processLogs() {
        String[] listData = new String[info.size()];
        int counter = 0;
        for (SwitchLogInfo s : info) {
            String log = s.getDate()/*.substring(s.getDate().indexOf(" ") + 1)*/;
            log += ": " + s.getData();
            listData[counter] = log;
            counter++;
        }
        return listData;
    }
}
