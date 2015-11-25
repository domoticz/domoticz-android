package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.R;

/**
 * Created by m.heinis on 11/12/2015.
 */
public class SwitchsDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private ArrayList<SwitchInfo> info;
    private String idx;
    private DismissListener dismissListener;
    private Context mContext;

    public SwitchsDialog(Context c,
                         ArrayList<SwitchInfo> _info,
                         int layout) {
        this.info = _info;
        this.mContext = c;

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(layout, true)
                .negativeText(android.R.string.cancel);
        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    public void show() {
        mdb.title("Connect Switch");
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = (ListView) view.findViewById(R.id.list);
        String[] listData = processSwitches();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, listData);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dismissListener != null)
                    dismissListener.onDismiss(info.get(position).getIdx());

                md.dismiss();
            }
        });

        listView.setAdapter(adapter);
        md.show();
    }

    public String[] processSwitches() {
        String[] listData = new String[info.size()];
        int counter = 0;
        for (SwitchInfo s : info) {
            String log = s.getIdx() + " | " + s.getName();
            listData[counter] = log;
            counter++;
        }
        return listData;
    }


    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int selectedSwitchIDX);
    }
}
