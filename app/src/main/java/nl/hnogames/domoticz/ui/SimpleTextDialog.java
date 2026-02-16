
package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;

public class SimpleTextDialog {

    private final MaterialDialog.Builder mdb;

    private final Context mContext;
    private String title;
    private String text;

    public SimpleTextDialog(Context mContext) {

        this.mContext = mContext;

        mdb = new MaterialDialog.Builder(mContext);

        boolean wrapInScrollView = true;

        //noinspection ConstantConditions
        mdb.customView(R.layout.dialog_text, wrapInScrollView)
                .positiveText(android.R.string.ok);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(int titleResourceId) {
        this.title = mContext.getResources().getString(titleResourceId);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setText(int textResourceId) {
        this.text = mContext.getResources().getString(textResourceId);
    }

    public void show() {
        mdb.title(title);
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        TextView dialogText = view.findViewById(R.id.textDialog_text);
        dialogText.setText(text);
        md.show();
    }
}