package nl.hnogames.domoticz.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.CameraInfo;
import nl.hnogames.domoticz.R;

@SuppressWarnings("unused")
public class CamerasAdapter extends RecyclerView.Adapter<CamerasAdapter.DataObjectHolder> {
    private static onClickListener onClickListener;
    private final Context mContext;
    private ArrayList<CameraInfo> mDataset;

    public CamerasAdapter(ArrayList<CameraInfo> data, Context mContext) {
        this.mDataset = data;
        this.mContext = mContext;
    }

    public void setOnItemClickListener(onClickListener onClickListener) {
        CamerasAdapter.onClickListener = onClickListener;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.camera_row, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {

        if (mDataset != null && mDataset.size() > 0) {
            CameraInfo cameraInfo = mDataset.get(position);
            String name = cameraInfo.getName();
            String address = cameraInfo.getAddress();
            String imageUrl = cameraInfo.getImageURL();

            int numberOfDevices = mDataset.get(position).getDevices();
            String text = mContext.getResources().getQuantityString(R.plurals.devices, numberOfDevices, numberOfDevices);

            holder.name.setText(name);
            Picasso.with(mContext).load(cameraInfo.getFullURL()).into(holder.camera);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public interface onClickListener {
        void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView name;
        ImageView camera;

        public DataObjectHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            camera = (ImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onItemClick(getLayoutPosition(), v);
        }
    }
}