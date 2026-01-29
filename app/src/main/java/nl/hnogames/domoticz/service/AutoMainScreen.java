package nl.hnogames.domoticz.service;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.Header;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;

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
        ItemList.Builder gridBuilder = new ItemList.Builder();

        // Dashboard - gmd_dashboard
        gridBuilder.addItem(new GridItem.Builder()
                .setImage(new CarIcon.Builder(IconCompat.createWithResource(
                        getCarContext(), R.drawable.ic_auto_dashboard)).build())
                .setTitle(getCarContext().getString(R.string.title_dashboard))
                .setOnClickListener(this::navigateToDashboard)
                .build());

        // Switches - gmd_lightbulb_outline
        gridBuilder.addItem(new GridItem.Builder()
                .setImage(new CarIcon.Builder(IconCompat.createWithResource(
                        getCarContext(), R.drawable.ic_auto_lightbulb)).build())
                .setTitle(getCarContext().getString(R.string.title_switches))
                .setOnClickListener(this::navigateToSwitches)
                .build());

        // Scenes - gmd_view_carousel
        gridBuilder.addItem(new GridItem.Builder()
                .setImage(new CarIcon.Builder(IconCompat.createWithResource(
                        getCarContext(), R.drawable.ic_auto_scenes)).build())
                .setTitle(getCarContext().getString(R.string.title_scenes))
                .setOnClickListener(this::navigateToScenes)
                .build());

        // Temperature - gmd_opacity
        gridBuilder.addItem(new GridItem.Builder()
                .setImage(new CarIcon.Builder(IconCompat.createWithResource(
                        getCarContext(), R.drawable.ic_auto_temperature)).build())
                .setTitle(getCarContext().getString(R.string.title_temperature))
                .setOnClickListener(this::navigateToTemperature)
                .build());

        // Weather - gmd_wb_sunny
        gridBuilder.addItem(new GridItem.Builder()
                .setImage(new CarIcon.Builder(IconCompat.createWithResource(
                        getCarContext(), R.drawable.ic_auto_weather)).build())
                .setTitle(getCarContext().getString(R.string.title_weather))
                .setOnClickListener(this::navigateToWeather)
                .build());

        // Utilities - gmd_dvr
        gridBuilder.addItem(new GridItem.Builder()
                .setImage(new CarIcon.Builder(IconCompat.createWithResource(
                        getCarContext(), R.drawable.ic_auto_utilities)).build())
                .setTitle(getCarContext().getString(R.string.title_utilities))
                .setOnClickListener(this::navigateToUtilities)
                .build());

        return new GridTemplate.Builder()
                .setSingleList(gridBuilder.build())
                .setHeader(new Header.Builder()
                        .setTitle(getCarContext().getString(R.string.app_name_domoticz))
                        .setStartHeaderAction(Action.APP_ICON)
                        .addEndHeaderAction(new Action.Builder()
                                .setTitle(getCarContext().getString(R.string.refresh))
                                .setOnClickListener(this::refresh)
                                .build())
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
