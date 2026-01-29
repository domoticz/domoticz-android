package nl.hnogames.domoticz.service;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

import nl.hnogames.domoticz.R;

/**
 * Main navigation screen for Android Auto
 * Provides access to different device categories
 */
public class AutoMainScreen extends Screen {

    public AutoMainScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        ItemList menuList = new ItemList.Builder()
                .addItem(new Row.Builder()
                        .setTitle(getCarContext().getString(R.string.title_dashboard))
                        .addText(getCarContext().getString(R.string.category_dashboard_subtitle))
                        .setOnClickListener(() -> navigateToDashboard())
                        .build())
                .addItem(new Row.Builder()
                        .setTitle(getCarContext().getString(R.string.title_switches))
                        .addText(getCarContext().getString(R.string.category_switches_subtitle))
                        .setOnClickListener(() -> navigateToSwitches())
                        .build())
                .addItem(new Row.Builder()
                        .setTitle(getCarContext().getString(R.string.title_temperature))
                        .addText(getCarContext().getString(R.string.category_temperature_subtitle))
                        .setOnClickListener(() -> navigateToTemperature())
                        .build())
                .addItem(new Row.Builder()
                        .setTitle(getCarContext().getString(R.string.title_weather))
                        .addText(getCarContext().getString(R.string.category_weather_subtitle))
                        .setOnClickListener(() -> navigateToWeather())
                        .build())
                .addItem(new Row.Builder()
                        .setTitle(getCarContext().getString(R.string.title_utilities))
                        .addText(getCarContext().getString(R.string.category_utilities_subtitle))
                        .setOnClickListener(() -> navigateToUtilities())
                        .build())
                .addItem(new Row.Builder()
                        .setTitle(getCarContext().getString(R.string.title_scenes))
                        .addText(getCarContext().getString(R.string.category_scenes_subtitle))
                        .setOnClickListener(() -> navigateToScenes())
                        .build())
                .build();

        return new ListTemplate.Builder()
                .setSingleList(menuList)
                .setTitle(getCarContext().getString(R.string.app_name_domoticz))
                .setHeaderAction(Action.APP_ICON)
                .setActionStrip(buildActionStrip())
                .build();
    }

    private ActionStrip buildActionStrip() {
        return new ActionStrip.Builder()
                .addAction(new Action.Builder()
                        .setTitle(getCarContext().getString(R.string.refresh))
                        .setOnClickListener(this::refresh)
                        .build())
                .build();
    }

    private void navigateToDashboard() {
        getScreenManager().push(new AutoDevicesScreen(getCarContext(), AutoDevicesScreen.DeviceFilter.DASHBOARD));
    }

    private void navigateToSwitches() {
        getScreenManager().push(new AutoDevicesScreen(getCarContext(), AutoDevicesScreen.DeviceFilter.SWITCHES));
    }

    private void navigateToTemperature() {
        getScreenManager().push(new AutoDevicesScreen(getCarContext(), AutoDevicesScreen.DeviceFilter.TEMPERATURE));
    }

    private void navigateToWeather() {
        getScreenManager().push(new AutoDevicesScreen(getCarContext(), AutoDevicesScreen.DeviceFilter.WEATHER));
    }

    private void navigateToUtilities() {
        getScreenManager().push(new AutoDevicesScreen(getCarContext(), AutoDevicesScreen.DeviceFilter.UTILITIES));
    }

    private void navigateToScenes() {
        getScreenManager().push(new AutoDevicesScreen(getCarContext(), AutoDevicesScreen.DeviceFilter.SCENES));
    }


    private void refresh() {
        invalidate();
    }
}
