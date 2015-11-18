package nl.hnogames.domoticz.Adapter;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.R;

public class ListAdapter extends WearableListView.Adapter {
    private ArrayList<ExtendedStatusInfo> mDataset;
    private final Context mContext;
    private final LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAdapter(Context context, ArrayList<ExtendedStatusInfo> dataset) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public void setData(ArrayList<ExtendedStatusInfo> dataset)
    {

        mDataset = dataset;
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        private TextView statusView;
        private ImageView imageView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            textView = (TextView) itemView.findViewById(R.id.name);
            statusView = (TextView) itemView.findViewById(R.id.status);
            imageView = (ImageView) itemView.findViewById(R.id.circle);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.textView;
        TextView status = itemHolder.statusView;

        view.setText(mDataset.get(position).getName());
        status.setText(mDataset.get(position).getData());

        String imageType = mDataset.get(position).getTypeImg();
        if(imageType!=null && imageType.length()>0)
            Picasso.with(this.mContext).load(getDrawableIcon(mDataset.get(position).getTypeImg())).into(itemHolder.imageView);

        // replace list item's metadata
        holder.itemView.setTag(position);
    }

    public int getDrawableIcon(String type) {
        int test = R.drawable.defaultimage;
        switch (type) {
            case "scene":
                return R.drawable.generic;
            case "group":
                return R.drawable.generic;
            case "wind":
                return R.drawable.wind;
            case "doorbell":
                return R.drawable.door;
            case "door":
                return R.drawable.door;
            case "lightbulb":
                return R.drawable.lights;
            case "push":
                return R.drawable.pushoff;
            case "pushoff":
                return R.drawable.pushoff;
            case "siren":
                return R.drawable.siren;
            case "smoke":
                return R.drawable.smoke;
            case "uv":
                return R.drawable.uv;
            case "contact":
                return R.drawable.contact;
            case "LogitechMediaServer":
                return R.drawable.media;
            case "Media":
                return R.drawable.media;
            case "blinds":
                return R.drawable.down;
            case "dimmer":
                return R.drawable.lights;
            case "motion":
                return R.drawable.motion;
            case "security":
                return R.drawable.security;
            case "temperature":
                return R.drawable.temperature;
            case "counter":
                return R.drawable.up;
            case "override_mini":
                return R.drawable.defaultimage;
            case "visibility":
                return R.drawable.visibility;
            case "radiation":
                return R.drawable.radiation;
            case "moisture":
                return R.drawable.rain;
            case "leaf":
                return R.drawable.leaf;
            case "hardware":
                return R.drawable.computer;
            case "Fan":
                return R.drawable.fan;
            case "Speaker":
                return R.drawable.speaker;
            case "current":
                return R.drawable.wall;
            case "text":
                return R.drawable.text;
            case "Alert":
                return R.drawable.siren;
            case "gauge":
                return R.drawable.gauge;
            case "clock":
                return R.drawable.clock48;
            case "mode":
                return R.drawable.defaultimage;
            case "utility":
                return R.drawable.scale;
        }

        return test;
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}