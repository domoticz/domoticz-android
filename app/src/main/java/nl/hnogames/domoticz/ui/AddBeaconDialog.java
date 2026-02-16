
package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.BeaconInfo;
import nl.hnogames.domoticz.utils.UsefulBits;

public class AddBeaconDialog {
    private final MaterialDialog.Builder mdb;
    private final Context mContext;
    private final OnDoneListener listener;
    private AppCompatEditText uuid;
    private AppCompatEditText major;
    private AppCompatEditText minor;

    public AddBeaconDialog(final Context mContext, OnDoneListener l) {
        this.mContext = mContext;
        this.listener = l;
        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        mdb.customView(R.layout.dialog_beacon, wrapInScrollView)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String UUID = String.valueOf(uuid.getText());
                        String Major = String.valueOf(major.getText());
                        if (UsefulBits.isEmpty(Major))
                            Major = "0";
                        String Minor = String.valueOf(minor.getText());
                        if (UsefulBits.isEmpty(Minor))
                            Minor = "0";

                        if (UsefulBits.isEmpty(UUID))
                            Toast.makeText(mContext, "The UUID is mandatory", Toast.LENGTH_LONG).show();
                        else {
                            BeaconInfo beacon = new BeaconInfo();
                            beacon.setId(UUID);
                            beacon.setMinor(Integer.parseInt(Minor));
                            beacon.setMajor(Integer.parseInt(Major));
                            if (listener != null)
                                listener.onAdded(beacon);
                        }
                    }
                })
                .negativeText(android.R.string.cancel);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.beacon));
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        uuid = view.findViewById(R.id.uuid);
        major = view.findViewById(R.id.major);
        minor = view.findViewById(R.id.minor);
        md.show();
    }

    public interface OnDoneListener {
        void onAdded(BeaconInfo beacon);
    }
}