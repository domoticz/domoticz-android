
package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;

public class SortDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private final Context mContext;
    private final String[] names;
    private DismissListener dismissListener;

    public SortDialog(Context c,
                      int layout,
                      String[] n) {
        this.mContext = c;
        if (n != null)
            names = n;
        else
            names = new String[]{mContext.getString(R.string.filterOn_on), mContext.getString(R.string.filterOn_off), mContext.getString(R.string.filterOn_static), mContext.getString(R.string.filterOn_all)};
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(layout, true)
                .negativeText(android.R.string.cancel);
        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    public void show() {
        mdb.title(R.string.filter);
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = view.findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, names);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dismissListener != null)
                    dismissListener.onDismiss(names[position]);
                md.dismiss();
            }
        });

        listView.setAdapter(adapter);
        md.show();
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(String selectedSort);
    }
}
