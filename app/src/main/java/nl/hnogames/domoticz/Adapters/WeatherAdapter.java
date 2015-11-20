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

import nl.hnogames.domoticz.Containers.WeatherInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;


public class WeatherAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = WeatherAdapter.class.getSimpleName();

    Context context;
    ArrayList<WeatherInfo> filteredData = null;
    ArrayList<WeatherInfo> data = null;
    Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();

    public WeatherAdapter(Context context,
                          ArrayList<WeatherInfo> data) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);
        Collections.sort(data, new Comparator<WeatherInfo>() {
            @Override
            public int compare(WeatherInfo left, WeatherInfo right) {
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

        WeatherInfo mWeatherInfo = filteredData.get(position);
        final long setPoint = mWeatherInfo.getSetPoint();

        //if (convertView == null) {
        holder = new ViewHolder();

        layoutResourceId = R.layout.weather_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.isProtected = mWeatherInfo.isProtected();
        holder.name = (TextView) convertView.findViewById(R.id.weather_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.data = (TextView) convertView.findViewById(R.id.weather_data);
        holder.hardware = (TextView) convertView.findViewById(R.id.weather_hardware);

        holder.name.setText(mWeatherInfo.getName());
        holder.data.append(mWeatherInfo.getData());
        holder.hardware.append(": "+mWeatherInfo.getHardwareName());

        convertView.setTag(holder);
        Picasso.with(context).load(domoticz.getDrawableIcon(mWeatherInfo.getTypeImg())).into(holder.iconRow);
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        TextView lastSeen;
        TextView setPoint;
        ImageButton buttonPlus;
        ImageButton buttonMinus;
        ImageView iconRow;
        Boolean isProtected;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<WeatherInfo> list = data;

            int count = list.size();
            final ArrayList<WeatherInfo> nlist = new ArrayList<WeatherInfo>(count);

            WeatherInfo filterableObject;

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
            filteredData = (ArrayList<WeatherInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}