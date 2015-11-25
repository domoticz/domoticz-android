package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.TemperatureInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;


public class TemperatureAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = TemperatureAdapter.class.getSimpleName();

    Domoticz domoticz;
    Context context;
    ArrayList<TemperatureInfo> filteredData = null;
    ArrayList<TemperatureInfo> data = null;
    private ItemFilter mFilter = new ItemFilter();

    public TemperatureAdapter(Context context,
                              ArrayList<TemperatureInfo> data) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);
        Collections.sort(data, new Comparator<TemperatureInfo>() {
            @Override
            public int compare(TemperatureInfo left, TemperatureInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
        this.data = data;
        this.filteredData = data;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    public Filter getFilter() {
        return mFilter;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int layoutResourceId;

        TemperatureInfo mTemperatureInfo = filteredData.get(position);
        final long setPoint = mTemperatureInfo.getSetPoint();

        //if (convertView == null) {
        holder = new ViewHolder();

        layoutResourceId = R.layout.temperature_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.isProtected = mTemperatureInfo.isProtected();
        holder.name = (TextView) convertView.findViewById(R.id.temperature_name);
        holder.data = (TextView) convertView.findViewById(R.id.temperature_data);
        holder.hardware = (TextView) convertView.findViewById(R.id.temperature_hardware);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        Picasso.with(context).load(domoticz.getDrawableIcon(mTemperatureInfo.getTypeImg())).into(holder.iconRow);

        holder.name.setText(mTemperatureInfo.getName());
        holder.data.append(": " + mTemperatureInfo.getData());
        holder.hardware.append(": " + mTemperatureInfo.getHardwareName());

        convertView.setTag(holder);
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        TextView lastSeen;
        TextView setPoint;
        ImageButton buttonPlus;
        ImageView iconRow;
        ImageButton buttonMinus;
        Boolean isProtected;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<TemperatureInfo> list = data;

            int count = list.size();
            final ArrayList<TemperatureInfo> nlist = new ArrayList<TemperatureInfo>(count);

            TemperatureInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<TemperatureInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}