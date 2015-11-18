package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;

public class SwitchInfoDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private ExtendedStatusInfo mSwitch;
    private String idx;
    private String lastUpdate;
    private String signalLevel;
    private String batteryLevel;
    private boolean isFavorite;
    private Context mContext;
    private Switch favorite_switch;

    public SwitchInfoDialog(Context mContext,
                            ExtendedStatusInfo mSwitch,
                            int layout) {
        this.mContext = mContext;
        this.mSwitch = mSwitch;

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        mdb.customView(layout, wrapInScrollView)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setSignalLevel(String signalLevel) {
        this.signalLevel = signalLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void show() {

        mdb.title(mSwitch.getName());

        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        TextView IDX_value = (TextView) view.findViewById(R.id.IDX_value);
        IDX_value.setText(idx);

        TextView LastUpdate_value = (TextView) view.findViewById(R.id.LastUpdate_value);
        LastUpdate_value.setText(lastUpdate);

        int signalLevelVal;
        try {
            signalLevelVal = Integer.valueOf(signalLevel);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            signalLevelVal = 0;
        }

        SeekBar signalLevelIndicator = (SeekBar) view.findViewById(R.id.SignalLevel_indicator);
        signalLevelIndicator.setVisibility(View.VISIBLE);
        signalLevelIndicator.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;    // This disables touch
            }
        });
        signalLevelIndicator.setMax(Domoticz.signalLevelMax * 100);
        ProgressBarAnimation anim =
                new ProgressBarAnimation(signalLevelIndicator, 5, signalLevelVal * 100);
        anim.setDuration(1000);
        signalLevelIndicator.startAnimation(anim);


        TextView BatteryLevel_value = (TextView) view.findViewById(R.id.BatteryLevel_value);
        int batteryLevelVal;
        try {
            batteryLevelVal = Integer.valueOf(batteryLevel);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            batteryLevelVal = 255;
        }
        if (batteryLevelVal == 255 || batteryLevelVal > Domoticz.batteryLevelMax) {
            batteryLevel = mContext.getString(R.string.txt_notAvailable);
            BatteryLevel_value.setText(batteryLevel);
        } else {
            BatteryLevel_value.setVisibility(View.GONE);
            SeekBar batteryLevelIndicator = (SeekBar) view.findViewById(R.id.BatteryLevel_indicator);
            batteryLevelIndicator.setVisibility(View.VISIBLE);
            batteryLevelIndicator.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;    // This disables touch
                }
            });
            batteryLevelIndicator.setMax(Domoticz.batteryLevelMax * 100);
            anim = new ProgressBarAnimation(batteryLevelIndicator, 5, batteryLevelVal * 100);
            anim.setDuration(1000);
            batteryLevelIndicator.startAnimation(anim);
        }


        favorite_switch = (Switch) view.findViewById(R.id.favorite_switch);
        favorite_switch.setChecked(isFavorite);
        favorite_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

            }
        });

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        boolean isChanged = false;
        boolean isChecked = favorite_switch.isChecked();
        if (isChecked != isFavorite) isChanged = true;
        if (dismissListener != null)
            dismissListener.onDismiss(isChanged, isChecked);
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(boolean isChanged, boolean isFavorite);
    }
}