
package nl.hnogames.domoticz.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.interfaces.ServerClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.ServerInfo;

public class ServerAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = ServerAdapter.class.getSimpleName();
    private final Context context;
    private final ServerClickListener listener;
    private final SharedPrefUtil mSharedPrefs;
    public ArrayList<ServerInfo> data = null;

    public ServerAdapter(Context context,
                         ArrayList<ServerInfo> data,
                         ServerClickListener l) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        this.data = data;
        this.listener = l;
    }

    @Override
    public int getCount() {
        if (data == null)
            return 0;

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
        final ViewHolder holder;
        int layoutResourceId;

        final ServerInfo mServerInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.server_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.enable = convertView.findViewById(R.id.enableServer);
        holder.server_name = convertView.findViewById(R.id.server_name);
        holder.server_remote_url = convertView.findViewById(R.id.server_remote_ip);
        holder.server_local_url = convertView.findViewById(R.id.server_local_ip);
        holder.remove = convertView.findViewById(R.id.remove_button);

        holder.server_local_url.setText(context.getString(R.string.local) + ": " + mServerInfo.getLocalServerUrl());
        holder.server_remote_url.setText(context.getString(R.string.remote) + ": " + mServerInfo.getRemoteServerUrl());
        holder.server_name.setText(mServerInfo.getServerName());

        holder.remove.setId(position);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    handleRemoveButtonClick(data.get(v.getId()));
                } catch (Exception ex) {
                }
            }
        });

        holder.enable.setId(position);
        holder.enable.setChecked(mServerInfo.isEnabled());
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleEnableChanged(data.get(buttonView.getId()), isChecked);
            }
        });

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(ServerInfo server) {
        listener.onRemoveClick(server);
    }

    private boolean handleEnableChanged(ServerInfo server, boolean enabled) {
        return listener.onEnableClick(server, enabled);
    }

    static class ViewHolder {
        TextView server_name;
        TextView server_remote_url;
        TextView server_local_url;
        CheckBox enable;
        Button remove;
    }
}