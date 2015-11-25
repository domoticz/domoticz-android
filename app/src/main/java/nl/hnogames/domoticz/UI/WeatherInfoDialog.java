package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.Containers.WeatherInfo;
import nl.hnogames.domoticz.R;

@SuppressWarnings("unused")
public class WeatherInfoDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private WeatherInfo info;
    private Context mContext;
    private Switch favorite_switch;

    public WeatherInfoDialog(Context mContext,
                             WeatherInfo info,
                             int layout) {
        this.mContext = mContext;
        this.info = info;
        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        mdb.customView(layout, wrapInScrollView)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void setWeatherInfo(WeatherInfo weather) {
        this.info = weather;
    }

    public void show() {
        mdb.title(info.getName());
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        TextView IDX_value = (TextView) view.findViewById(R.id.IDX_value);

        TextView weather_forcast_title = (TextView) view.findViewById(R.id.weather_forcast);
        TextView weather_humidity_title = (TextView) view.findViewById(R.id.weather_humidity);
        TextView weather_barometer_title = (TextView) view.findViewById(R.id.weather_barometer);
        TextView weather_drewpoint_title = (TextView) view.findViewById(R.id.weather_drewpoint);
        TextView weather_temperature_title = (TextView) view.findViewById(R.id.weather_temperature);
        TextView weather_chill_title = (TextView) view.findViewById(R.id.weather_chill);
        TextView weather_direction_title= (TextView) view.findViewById(R.id.weather_direction);
        TextView weather_speed_title = (TextView) view.findViewById(R.id.weather_speed);

        TextView weather_forcast = (TextView) view.findViewById(R.id.weather_forcast_value);
        TextView weather_humidity = (TextView) view.findViewById(R.id.weather_humidity_value);
        TextView weather_barometer = (TextView) view.findViewById(R.id.weather_barometer_value);
        TextView weather_drewpoint = (TextView) view.findViewById(R.id.weather_drewpoint_value);
        TextView weather_temperature = (TextView) view.findViewById(R.id.weather_temperature_value);
        TextView weather_chill = (TextView) view.findViewById(R.id.weather_chill_value);
        TextView weather_direction = (TextView) view.findViewById(R.id.weather_direction_value);
        TextView weather_speed = (TextView) view.findViewById(R.id.weather_speed_value);

        IDX_value.setText(info.getIdx()+"");
           TextView LastUpdate_value = (TextView) view.findViewById(R.id.LastUpdate_value);
        LastUpdate_value.setText(info.getLastUpdate());

        favorite_switch = (Switch) view.findViewById(R.id.favorite_switch);
        favorite_switch.setChecked(info.getFavoriteBoolean());
        favorite_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {}
        });

        //data of weather object:
        if(info.getForecastStr()!=null && info.getForecastStr().length()>0)
            weather_forcast.setText( info.getForecastStr());
        else {
            weather_forcast.setVisibility(View.GONE);
            weather_forcast_title.setVisibility(View.GONE);
        }
        if(info.getSpeed()!=null && info.getSpeed().length()>0)
            weather_speed.setText(info.getSpeed());
        else {
            weather_speed.setVisibility(View.GONE);
            weather_speed_title.setVisibility(View.GONE);
        }
        if(info.getDewPoint()>0)
            weather_drewpoint.setText(info.getDewPoint()+"");
        else {
            weather_drewpoint.setVisibility(View.GONE);
            weather_drewpoint_title.setVisibility(View.GONE);
        }
        if(info.getTemp()>0)
            weather_temperature.setText(info.getTemp()+"");
        else {
            weather_temperature.setVisibility(View.GONE);
            weather_temperature_title.setVisibility(View.GONE);
        }
        if(info.getBarometer()>0)
            weather_barometer.setText(info.getBarometer()+"");
        else {
            weather_barometer.setVisibility(View.GONE);
            weather_barometer_title.setVisibility(View.GONE);
        }
        if(info.getChill()!=null && info.getChill().length()>0)
            weather_chill.setText(info.getChill());
        else {
            weather_chill.setVisibility(View.GONE);
            weather_chill_title.setVisibility(View.GONE);
        }
        if(info.getDirectionStr()!=null && info.getDirectionStr().length()>0)
            weather_direction.setText(info.getDirectionStr());
        else {
            weather_direction.setVisibility(View.GONE);
            weather_direction_title.setVisibility(View.GONE);
        }
        if(info.getHumidityStatus()!=null && info.getHumidityStatus().length()>0)
            weather_humidity.setText(info.getHumidityStatus());
        else {
            weather_humidity.setVisibility(View.GONE);
            weather_humidity_title.setVisibility(View.GONE);
        }

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        boolean isChanged = false;
        boolean isChecked = favorite_switch.isChecked();
        if (isChecked != info.getFavoriteBoolean()) isChanged = true;
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