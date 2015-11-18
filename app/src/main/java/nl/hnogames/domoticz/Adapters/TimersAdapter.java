package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchTimerInfo;
import nl.hnogames.domoticz.R;

// Example used: http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
// And: http://www.survivingwithandroid.com/2013/02/android-listview-adapter-checkbox-item_7.html
public class TimersAdapter extends BaseAdapter {

    private static final String TAG = TimersAdapter.class.getSimpleName();

    Context context;
    ArrayList<SwitchTimerInfo> data = null;

    public TimersAdapter(Context context,
                         ArrayList<SwitchTimerInfo> data) {
        super();

        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int layoutResourceId;

        SwitchTimerInfo mSwitchTimerInfo = data.get(position);

        holder = new ViewHolder();
        layoutResourceId = R.layout.timer_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
        holder.switch_status = (TextView) convertView.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) convertView.findViewById(R.id.switch_signal_level);

        holder.switch_name.setText(mSwitchTimerInfo.getActive());
        String commando = "";
        if (mSwitchTimerInfo.getCmd() == 1)
            commando += "Commando: " + context.getString(R.string.button_state_on);
        else
            commando += "Commando: " + context.getString(R.string.button_state_off);

        String type = "";
        if (mSwitchTimerInfo.getType() == 0)
            type += "Type: " + context.getString(R.string.timer_before_sunrise);
        else if (mSwitchTimerInfo.getType() == 1)
            type += "Type: " + context.getString(R.string.timer_after_sunrise);
        else if (mSwitchTimerInfo.getType() == 2)
            type += "Type: " + context.getString(R.string.timer_ontime);
        else if (mSwitchTimerInfo.getType() == 3)
            type += "Type: " + context.getString(R.string.timer_before_sunset);
        else if (mSwitchTimerInfo.getType() == 4)
            type += "Type: " + context.getString(R.string.timer_after_sunset);
        else if (mSwitchTimerInfo.getType() == 5)
            type += "Type: " + context.getString(R.string.timer_fixed);
        else
            type += "Type: N/A";

        if (mSwitchTimerInfo.getDate().length() > 0)
            holder.switch_name.setText(holder.switch_name.getText() + " | " + mSwitchTimerInfo.getDate());
        else {
            if (mSwitchTimerInfo.getDays() == 128)
                holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_every_days));
            else if (mSwitchTimerInfo.getDays() == 512)
                holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_weekend));
            else if (mSwitchTimerInfo.getDays() == 256)
                holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_working_days));
            else if (mSwitchTimerInfo.getDays() == 512)
                holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_weekend));
            else
                holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_other));
        }

        holder.switch_status.setText(commando);
        holder.signal_level.setText(type);

        convertView.setTag(holder);

        return convertView;
    }

    static class ViewHolder {
        TextView switch_name, signal_level, switch_status;
    }
}