package nl.hnogames.domoticz.service;

import static androidx.car.app.model.CarColor.BLUE;
import static androidx.car.app.model.CarColor.GREEN;
import static androidx.car.app.model.CarColor.RED;
import static androidx.car.app.model.CarColor.YELLOW;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.constraints.ConstraintManager;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.ForegroundCarColorSpan;
import androidx.car.app.model.Header;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;
import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;
import nl.hnogames.domoticzapi.Containers.WeatherInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;
import nl.hnogames.domoticzapi.Interfaces.TemperatureReceiver;
import nl.hnogames.domoticzapi.Interfaces.UtilitiesReceiver;
import nl.hnogames.domoticzapi.Interfaces.WeatherReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

/**
 * Modern Android Auto screen for displaying and controlling Domoticz devices
 * Features:
 * - Category-based filtering
 * - Color-coded status indicators
 * - Pull-to-refresh support
 * - Enhanced error handling
 * - Better visual feedback
 */
public class AutoDevicesScreen extends Screen implements DefaultLifecycleObserver {
    private static final String TAG = "AutoDevicesScreen";
    private static final int DEFAULT_MAX_ITEMS = 6; // Fallback if ConstraintManager not available

    private final DeviceFilter filter;
    private final ArrayList<Object> devicesList = new ArrayList<>(); // Can hold DevicesInfo, SceneInfo, TemperatureInfo, etc.
    private SharedPrefUtil mSharedPrefs;
    private boolean isLoading = true;
    private String errorMessage = null;
    private int maxListItems = DEFAULT_MAX_ITEMS;
    private String searchQuery = ""; // Current search query

    public enum DeviceFilter {
        DASHBOARD,
        SWITCHES,
        TEMPERATURE,
        WEATHER,
        UTILITIES,
        SCENES
    }

    public AutoDevicesScreen(@NonNull CarContext carContext, DeviceFilter filter) {
        super(carContext);
        this.filter = filter;
        getLifecycle().addObserver(this);

        // Get dynamic list limit from car's constraints
        try {
            ConstraintManager constraintManager = carContext.getCarService(ConstraintManager.class);
            maxListItems = constraintManager.getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_LIST);
            Log.d(TAG, "Car supports " + maxListItems + " items per list");
        } catch (Exception e) {
            Log.w(TAG, "Could not get list limit from ConstraintManager, using default: " + DEFAULT_MAX_ITEMS, e);
            maxListItems = DEFAULT_MAX_ITEMS;
        }
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        if (isLoading) {
            return new MessageTemplate.Builder(getCarContext().getString(R.string.msg_please_wait))
                    .setHeader(new Header.Builder()
                            .setTitle(getFilterTitle())
                            .setStartHeaderAction(Action.BACK)
                            .build())
                    .setLoading(true)
                    .build();
        }

        if (errorMessage != null) {
            return new MessageTemplate.Builder(errorMessage)
                    .setHeader(new Header.Builder()
                            .setTitle(getFilterTitle())
                            .setStartHeaderAction(Action.BACK)
                            .build())
                    .addAction(new Action.Builder()
                            .setTitle(getCarContext().getString(R.string.retry))
                            .setOnClickListener(this::loadDevices)
                            .build())
                    .build();
        }

        if (devicesList.isEmpty()) {
            return new MessageTemplate.Builder(getCarContext().getString(R.string.no_data_on_domoticz))
                    .setHeader(new Header.Builder()
                            .setTitle(getFilterTitle())
                            .setStartHeaderAction(Action.BACK)
                            .build())
                    .addAction(new Action.Builder()
                            .setTitle(getCarContext().getString(R.string.refresh))
                            .setOnClickListener(this::loadDevices)
                            .build())
                    .build();
        }

        // Filter devices based on search query
        ArrayList<Object> filteredDevices = filterDevicesBySearch(devicesList, searchQuery);

        // If search is active and no results, show a message
        if (filteredDevices.isEmpty() && !searchQuery.isEmpty()) {
            return new MessageTemplate.Builder(getCarContext().getString(R.string.no_data_on_domoticz))
                    .setHeader(new Header.Builder()
                            .setTitle(getFilterTitle())
                            .setStartHeaderAction(Action.BACK)
                            .addEndHeaderAction(new Action.Builder()
                                    .setTitle(getCarContext().getString(R.string.search_items))
                                    .setOnClickListener(this::showSearchInput)
                                    .build())
                            .addEndHeaderAction(new Action.Builder()
                                    .setTitle(getCarContext().getString(R.string.refresh))
                                    .setOnClickListener(this::loadDevices)
                                    .build())
                            .build())
                    .addAction(new Action.Builder()
                            .setTitle(getCarContext().getString(R.string.clear_filter))
                            .setOnClickListener(this::clearSearch)
                            .build())
                    .build();
        }

        ItemList.Builder listBuilder = new ItemList.Builder();
        int itemCount = 0;

        for (Object item : filteredDevices) {
            if (itemCount >= maxListItems) {
                break; // Respect car's content limit
            }

            Row row = createRow(item);
            if (row != null) {
                listBuilder.addItem(row);
                itemCount++;
            }
        }

        // Build header with search action
        Header.Builder headerBuilder = new Header.Builder()
                .setTitle(getFilterTitle())
                .setStartHeaderAction(Action.BACK)
                .addEndHeaderAction(new Action.Builder()
                        .setTitle(getCarContext().getString(R.string.search_items))
                        .setOnClickListener(this::showSearchInput)
                        .build())
                .addEndHeaderAction(new Action.Builder()
                        .setTitle(getCarContext().getString(R.string.refresh))
                        .setOnClickListener(this::loadDevices)
                        .build());

        return new ListTemplate.Builder()
                .setSingleList(listBuilder.build())
                .setHeader(headerBuilder.build())
                .build();
    }

    private Row createRow(Object item) {
        if (item instanceof DevicesInfo) {
            return createDeviceRow((DevicesInfo) item);
        } else if (item instanceof SceneInfo) {
            return createSceneRow((SceneInfo) item);
        } else if (item instanceof TemperatureInfo) {
            return createTemperatureRow((TemperatureInfo) item);
        } else if (item instanceof WeatherInfo) {
            return createWeatherRow((WeatherInfo) item);
        } else if (item instanceof UtilitiesInfo) {
            return createUtilityRow((UtilitiesInfo) item);
        }
        return null;
    }

    private Row createDeviceRow(DevicesInfo device) {
        CharSequence statusText = getDeviceStatus(device);

        Row.Builder rowBuilder = new Row.Builder()
                .setTitle(device.getName())
                .addText(statusText);

        // Add click listener for controllable devices
        if (isControllableDevice(device)) {
            rowBuilder.setOnClickListener(() -> onDeviceClick(device));
        }

        return rowBuilder.build();
    }

    private Row createSceneRow(SceneInfo scene) {
        String status = scene.getStatusInString();
        CarColor color = status != null && status.equalsIgnoreCase("On") ? BLUE : RED;

        Row.Builder rowBuilder = new Row.Builder()
                .setTitle(scene.getName())
                .addText(colorize(status != null ? status : "Off", color));

        rowBuilder.setOnClickListener(() -> onSceneClick(scene));
        return rowBuilder.build();
    }

    private Row createTemperatureRow(TemperatureInfo temp) {
        StringBuilder statusBuilder = new StringBuilder();

        if (temp.getData() != null && !temp.getData().isEmpty()) {
            statusBuilder.append(temp.getData());
        }

        String status = statusBuilder.toString();
        if (status.isEmpty()) {
            status = getCarContext().getString(R.string.no_data_on_domoticz);
        }

        return new Row.Builder()
                .setTitle(temp.getName())
                .addText(colorize(status, GREEN))
                .build();
    }

    private Row createWeatherRow(WeatherInfo weather) {
        StringBuilder statusBuilder = new StringBuilder();

        if (!UsefulBits.isEmpty(weather.getData())) {
            statusBuilder.append(weather.getData());
        }

        if (!UsefulBits.isEmpty(weather.getForecastStr())) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(weather.getForecastStr());
        }

        String status = statusBuilder.toString();
        if (status.isEmpty()) {
            status = getCarContext().getString(R.string.no_data_on_domoticz);
        }

        return new Row.Builder()
                .setTitle(weather.getName())
                .addText(colorize(status, BLUE))
                .build();
    }

    private Row createUtilityRow(UtilitiesInfo utility) {
        StringBuilder statusBuilder = new StringBuilder();

        if (!UsefulBits.isEmpty(utility.getData())) {
            statusBuilder.append(utility.getData());
        }

        if (!UsefulBits.isEmpty(utility.getCounterToday())) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(getCarContext().getString(R.string.today))
                    .append(": ").append(utility.getCounterToday());
        }

        String status = statusBuilder.toString();
        if (status.isEmpty()) {
            status = getCarContext().getString(R.string.no_data_on_domoticz);
        }

        return new Row.Builder()
                .setTitle(utility.getName())
                .addText(colorize(status, YELLOW))
                .build();
    }

    private CharSequence getDeviceStatus(DevicesInfo device) {
        String status = device.getData();
        CarColor color = GREEN;

        // Build detailed status string
        StringBuilder statusBuilder = new StringBuilder();

        if (!UsefulBits.isEmpty(device.getUsage())) {
            statusBuilder.append(getCarContext().getString(R.string.usage))
                    .append(": ").append(device.getUsage());
        } else if (!UsefulBits.isEmpty(status)) {
            statusBuilder.append(status);
        }

        if (!UsefulBits.isEmpty(device.getCounterToday())) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(getCarContext().getString(R.string.today))
                    .append(": ").append(device.getCounterToday());
        }

        if (!UsefulBits.isEmpty(device.getCounter()) &&
            !device.getCounter().equals(device.getData())) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(getCarContext().getString(R.string.total))
                    .append(": ").append(device.getCounter());
        }

        if (device.getType() != null && device.getType().equals("Wind")) {
            statusBuilder.setLength(0);
            statusBuilder.append(getCarContext().getString(R.string.direction))
                    .append(" ").append(device.getDirection())
                    .append(" ").append(device.getDirectionStr());
        }

        if (!UsefulBits.isEmpty(device.getForecastStr())) {
            statusBuilder.setLength(0);
            statusBuilder.append(device.getForecastStr());
        }

        if (!UsefulBits.isEmpty(device.getSpeed())) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(getCarContext().getString(R.string.speed))
                    .append(": ").append(device.getSpeed());
        }

        if (device.getTemp() > 0) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(getCarContext().getString(R.string.temp))
                    .append(": ").append(device.getTemp());
        }

        if (!UsefulBits.isEmpty(device.getHumidityStatus())) {
            if (statusBuilder.length() > 0) statusBuilder.append(" | ");
            statusBuilder.append(getCarContext().getString(R.string.humidity))
                    .append(": ").append(device.getHumidityStatus());
        }

        String finalStatus = statusBuilder.toString();

        // Determine color based on status
        if (status != null) {
            if (status.equalsIgnoreCase("Off") || status.equalsIgnoreCase("Closed")) {
                color = RED;
            } else if (status.equalsIgnoreCase("On") || status.equalsIgnoreCase("Open")) {
                color = BLUE;
            } else if (status.toLowerCase().contains("error") ||
                       status.toLowerCase().contains("alarm")) {
                color = YELLOW;
            }
        }

        return colorize(finalStatus, color);
    }

    private CharSequence colorize(String text, CarColor color) {
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(
                ForegroundCarColorSpan.create(color),
                0,
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private boolean isControllableDevice(DevicesInfo device) {
        if (device.getType() != null &&
            (device.getType().equals(DomoticzValues.Scene.Type.GROUP) ||
             device.getType().equals(DomoticzValues.Scene.Type.SCENE))) {
            return true;
        }

        int switchType = device.getSwitchTypeVal();
        return switchType == DomoticzValues.Device.Type.Value.ON_OFF ||
               switchType == DomoticzValues.Device.Type.Value.MEDIAPLAYER ||
               switchType == DomoticzValues.Device.Type.Value.DOORLOCK ||
               switchType == DomoticzValues.Device.Type.Value.DIMMER ||
               switchType == DomoticzValues.Device.Type.Value.BLINDS ||
               switchType == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
               switchType == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON ||
               switchType == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON;
    }

    private void onSceneClick(SceneInfo scene) {
        try {
            // Toggle scene on/off
            boolean newState = !scene.getStatusInBoolean();
            int jsonAction = newState ? DomoticzValues.Scene.Action.ON : DomoticzValues.Scene.Action.OFF;

            StaticHelper.getDomoticz(getCarContext()).setAction(
                    scene.getIdx(),
                    DomoticzValues.Json.Url.Set.SCENES,
                    jsonAction,
                    0,
                    null,
                    new setCommandReceiver() {
                        @Override
                        public void onReceiveResult(String result) {
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> loadDevices(),
                                    1500
                            );
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e(TAG, "Error executing scene action", error);
                            CarToast.makeText(
                                    getCarContext(),
                                    R.string.security_no_rights,
                                    CarToast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error clicking scene", e);
            CarToast.makeText(
                    getCarContext(),
                    getCarContext().getString(R.string.error_generic),
                    CarToast.LENGTH_SHORT
            ).show();
        }
    }

    private void onDeviceClick(DevicesInfo device) {
        try {
            if (device.getType() != null &&
                (device.getType().equals(DomoticzValues.Scene.Type.GROUP) ||
                 device.getType().equals(DomoticzValues.Scene.Type.SCENE))) {
                if (device.getType().equals(DomoticzValues.Scene.Type.GROUP)) {
                    onButtonClick(device, true);
                } else {
                    toggleDevice(device);
                }
                return;
            }

            switch (device.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                case DomoticzValues.Device.Type.Value.DOORLOCK:
                case DomoticzValues.Device.Type.Value.DIMMER:
                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                    toggleDevice(device);
                    break;

                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                    onButtonClick(device, true);
                    break;

                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    onButtonClick(device, false);
                    break;

                default:
                    CarToast.makeText(
                            getCarContext(),
                            getCarContext().getString(R.string.switch_not_supported),
                            CarToast.LENGTH_SHORT
                    ).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking device", e);
            CarToast.makeText(
                    getCarContext(),
                    getCarContext().getString(R.string.error_generic),
                    CarToast.LENGTH_SHORT
            ).show();
        }
    }

    private void toggleDevice(DevicesInfo device) {
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        boolean newState = !device.getStatusBoolean();

        if (device.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
            device.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
            device.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
            jsonAction = newState ? DomoticzValues.Device.Switch.Action.OFF
                                  : DomoticzValues.Device.Switch.Action.ON;
        } else {
            jsonAction = newState ? DomoticzValues.Device.Switch.Action.ON
                                  : DomoticzValues.Device.Switch.Action.OFF;
        }

        if (device.getType() != null &&
            (device.getType().equals(DomoticzValues.Scene.Type.GROUP) ||
             device.getType().equals(DomoticzValues.Scene.Type.SCENE))) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            jsonAction = newState ? DomoticzValues.Scene.Action.ON
                                  : DomoticzValues.Scene.Action.OFF;
        }

        executeDeviceAction(device, jsonUrl, jsonAction);
    }

    private void onButtonClick(DevicesInfo device, boolean on) {
        int jsonAction = on ? DomoticzValues.Device.Switch.Action.ON
                            : DomoticzValues.Device.Switch.Action.OFF;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (device.getType() != null &&
            (device.getType().equals(DomoticzValues.Scene.Type.GROUP) ||
             device.getType().equals(DomoticzValues.Scene.Type.SCENE))) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            jsonAction = on ? DomoticzValues.Scene.Action.ON
                            : DomoticzValues.Scene.Action.OFF;
        }

        executeDeviceAction(device, jsonUrl, jsonAction);
    }

    private void executeDeviceAction(DevicesInfo device, int jsonUrl, int jsonAction) {
        StaticHelper.getDomoticz(getCarContext()).setAction(
                device.getIdx(),
                jsonUrl,
                jsonAction,
                0,
                null,
                new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        if (result.contains("WRONG CODE")) {
                            CarToast.makeText(
                                    getCarContext(),
                                    getCarContext().getString(R.string.security_wrong_code),
                                    CarToast.LENGTH_LONG
                            ).show();
                        } else {
                            // Reload devices after a short delay
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> loadDevices(),
                                    1500
                            );
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Error executing device action", error);
                        CarToast.makeText(
                                getCarContext(),
                                R.string.security_no_rights,
                                CarToast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String getFilterTitle() {
        switch (filter) {
            case DASHBOARD:
                return getCarContext().getString(R.string.title_dashboard);
            case SWITCHES:
                return getCarContext().getString(R.string.title_switches);
            case TEMPERATURE:
                return getCarContext().getString(R.string.title_temperature);
            case WEATHER:
                return getCarContext().getString(R.string.title_weather);
            case UTILITIES:
                return getCarContext().getString(R.string.title_utilities);
            case SCENES:
                return getCarContext().getString(R.string.title_scenes);
            default:
                return getCarContext().getString(R.string.app_name_domoticz);
        }
    }

    private void loadDevices() {
        isLoading = true;
        errorMessage = null;
        devicesList.clear();
        invalidate();

        try {
            if (mSharedPrefs == null) {
                mSharedPrefs = new SharedPrefUtil(getCarContext());
            }

            Domoticz domoticz = StaticHelper.getDomoticz(getCarContext());

            // Use the appropriate API method for each device type
            switch (filter) {
                case TEMPERATURE:
                    domoticz.getTemperatures(new TemperatureReceiver() {
                        @Override
                        public void onReceiveTemperatures(ArrayList<TemperatureInfo> temperatures) {
                            Log.d(TAG, "Received " + temperatures.size() + " temperatures");
                            filterAndAddTemperatures(temperatures);
                            isLoading = false;
                            invalidate();
                        }

                        @Override
                        public void onError(Exception error) {
                            handleLoadError(error);
                        }
                    });
                    break;

                case WEATHER:
                    domoticz.getWeathers(new WeatherReceiver() {
                        @Override
                        public void onReceiveWeather(ArrayList<WeatherInfo> weathers) {
                            Log.d(TAG, "Received " + weathers.size() + " weather items");
                            filterAndAddWeather(weathers);
                            isLoading = false;
                            invalidate();
                        }

                        @Override
                        public void onError(Exception error) {
                            handleLoadError(error);
                        }
                    });
                    break;

                case UTILITIES:
                    domoticz.getUtilities(new UtilitiesReceiver() {
                        @Override
                        public void onReceiveUtilities(ArrayList<UtilitiesInfo> utilities) {
                            Log.d(TAG, "Received " + utilities.size() + " utilities");
                            filterAndAddUtilities(utilities);
                            isLoading = false;
                            invalidate();
                        }

                        @Override
                        public void onError(Exception error) {
                            handleLoadError(error);
                        }
                    });
                    break;

                case SCENES:
                    domoticz.getScenes(new ScenesReceiver() {
                        @Override
                        public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                            Log.d(TAG, "Received " + scenes.size() + " scenes");
                            filterAndAddScenes(scenes);
                            isLoading = false;
                            invalidate();
                        }

                        @Override
                        public void onReceiveScene(SceneInfo scene) {
                            // Not used
                        }

                        @Override
                        public void onError(Exception error) {
                            handleLoadError(error);
                        }
                    });
                    break;

                case SWITCHES:
                    domoticz.getDevices(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                            Log.d(TAG, "Received " + devices.size() + " devices");
                            filterAndAddDevices(devices);
                            isLoading = false;
                            invalidate();
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo device) {
                            // Not used
                        }

                        @Override
                        public void onError(Exception error) {
                            handleLoadError(error);
                        }
                    }, 0, "light");
                    break;

                case DASHBOARD:
                default:
                    // For switches and dashboard, use getDevices with "light" filter
                    // Dashboard shows all favorites, Switches shows only switch-type favorites
                    domoticz.getDevices(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                            Log.d(TAG, "Received " + devices.size() + " devices");
                            filterAndAddDevices(devices);
                            isLoading = false;
                            invalidate();
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo device) {
                            // Not used
                        }

                        @Override
                        public void onError(Exception error) {
                            handleLoadError(error);
                        }
                    }, 0, "all");
                    break;
            }

        } catch (Exception ex) {
            Log.e(TAG, "Exception loading devices", ex);
            isLoading = false;
            errorMessage = getCarContext().getString(R.string.error_generic);
            invalidate();
        }
    }

    private void handleLoadError(Exception error) {
        Log.e(TAG, "Error loading devices", error);
        isLoading = false;
        errorMessage = getCarContext().getString(R.string.error_notConnected);
        invalidate();
    }

    private void filterAndAddTemperatures(ArrayList<TemperatureInfo> temperatures) {
        devicesList.clear();

        for (TemperatureInfo temp : temperatures) {
            String name = temp.getName();
            if (name == null || name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                continue;
            }
            devicesList.add(temp);
        }
        Log.d(TAG, "Filtered to " + devicesList.size() + " temperatures");
    }

    private void filterAndAddWeather(ArrayList<WeatherInfo> weathers) {
        devicesList.clear();

        for (WeatherInfo weather : weathers) {
            String name = weather.getName();
            if (name == null || name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                continue;
            }
            devicesList.add(weather);
        }
        Log.d(TAG, "Filtered to " + devicesList.size() + " weather items");
    }

    private void filterAndAddUtilities(ArrayList<UtilitiesInfo> utilities) {
        devicesList.clear();

        for (UtilitiesInfo utility : utilities) {
            String name = utility.getName();
            if (name == null || name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                continue;
            }
            devicesList.add(utility);
        }
        Log.d(TAG, "Filtered to " + devicesList.size() + " utilities");
    }

    private void filterAndAddScenes(ArrayList<SceneInfo> scenes) {
        devicesList.clear();

        for (SceneInfo scene : scenes) {
            String name = scene.getName();
            if (name == null || name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                continue;
            }
            devicesList.add(scene);
        }
        Log.d(TAG, "Filtered to " + devicesList.size() + " scenes");
    }

    private void filterAndAddDevices(ArrayList<DevicesInfo> devices) {
        devicesList.clear();

        for (DevicesInfo device : devices) {
            String name = device.getName();
            if (name == null || name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                continue;
            }

            boolean isSceneOrGroup = device.getType() != null &&
                    (device.getType().equals(DomoticzValues.Scene.Type.GROUP) ||
                     device.getType().equals(DomoticzValues.Scene.Type.SCENE));

            boolean shouldAdd;

            switch (filter) {
                case DASHBOARD:
                    // Dashboard: Show only FAVORITES
                    shouldAdd = device.getFavoriteBoolean();
                    break;

                case SWITCHES:
                    // Switches: Show ALL controllable switches (exclude scenes/groups)
                    shouldAdd = !isSceneOrGroup && isControllableDevice(device);
                    break;

                default:
                    shouldAdd = false;
                    break;
            }

            if (shouldAdd) {
                devicesList.add(device);
            }
        }

        Log.d(TAG, "Filtered to " + devicesList.size() + " devices for " + filter);
    }

    /**
     * Filter devices based on search query
     */
    private ArrayList<Object> filterDevicesBySearch(ArrayList<Object> devices, String query) {
        if (query == null || query.trim().isEmpty()) {
            return devices;
        }

        ArrayList<Object> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Object item : devices) {
            String itemName = getItemName(item);
            if (itemName != null && itemName.toLowerCase().contains(lowerQuery)) {
                filtered.add(item);
            }
        }

        Log.d(TAG, "Search filtered from " + devices.size() + " to " + filtered.size() + " items");
        return filtered;
    }

    /**
     * Get the name of any device type
     */
    private String getItemName(Object item) {
        if (item instanceof DevicesInfo) {
            return ((DevicesInfo) item).getName();
        } else if (item instanceof SceneInfo) {
            return ((SceneInfo) item).getName();
        } else if (item instanceof TemperatureInfo) {
            return ((TemperatureInfo) item).getName();
        } else if (item instanceof WeatherInfo) {
            return ((WeatherInfo) item).getName();
        } else if (item instanceof UtilitiesInfo) {
            return ((UtilitiesInfo) item).getName();
        }
        return null;
    }

    /**
     * Show search input screen
     */
    private void showSearchInput() {
        getScreenManager().push(new AutoSearchScreen(getCarContext(), this::onSearchSubmitted, searchQuery));
    }

    /**
     * Clear search query and refresh
     */
    private void clearSearch() {
        searchQuery = "";
        invalidate();
    }

    /**
     * Handle search submission
     */
    private void onSearchSubmitted(String query) {
        searchQuery = query != null ? query : "";
        invalidate();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "onStart - Loading devices for filter: " + filter);
        loadDevices();
    }
}
