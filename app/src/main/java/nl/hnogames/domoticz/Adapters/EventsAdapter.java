package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;

// Example used: http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
// And: http://www.survivingwithandroid.com/2013/02/android-listview-adapter-checkbox-item_7.html
public class EventsAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = EventsAdapter.class.getSimpleName();

    Context context;
    ArrayList<EventInfo> filteredData = null;
    ArrayList<EventInfo> data = null;
    Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();

    public EventsAdapter(Context context,
                         ArrayList<EventInfo> data) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);

        Collections.reverse(data);
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

        EventInfo mEventInfo = filteredData.get(position);

        //if (convertView == null) {
        holder = new ViewHolder();
        layoutResourceId = R.layout.event_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.name = (TextView) convertView.findViewById(R.id.logs_name);
        holder.message = (TextView) convertView.findViewById(R.id.logs_message);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.buttonON = (Switch) convertView.findViewById(R.id.switch_button);

        holder.buttonON.setEnabled(false);

        holder.name.setText(mEventInfo.getName());
        if(mEventInfo.getStatus().equals("1")) {
            holder.message.setText("Status: " + context.getString(R.string.button_state_on));
            holder.buttonON.setChecked(true);
        }else {
            holder.buttonON.setChecked(false);
            holder.message.setText("Status: " + context.getString(R.string.button_state_off));
        }

        Picasso.with(context).load(R.drawable.cone).into(holder.iconRow);
        convertView.setTag(holder);
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView message;
        Switch buttonON;
        ImageView iconRow;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<EventInfo> list = data;

            int count = list.size();
            final ArrayList<EventInfo> nlist = new ArrayList<EventInfo>(count);

            EventInfo filterableObject;

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
            filteredData = (ArrayList<EventInfo>) results.values;
            notifyDataSetChanged();
        }
    }

}