package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.ui.EnergyFlowView;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.EnergyDashboardInfo;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.EnergyDashboardReceiver;

public class Energy extends DomoticzCardFragment implements DomoticzFragmentListener {

    private static final String TAG = Energy.class.getSimpleName();

    private Context mContext;
    private EnergyDashboardInfo energyDashboard;
    private final Map<Integer, DevicesInfo> deviceCache = new HashMap<>();

    private RelativeLayout layout;
    private TextView txtLoading;
    private LinearLayout cardP1, cardSolar, cardBattery, cardGas, cardWater, cardHome, tempContainer;
    private LinearLayout cardExtra1, cardExtra2, cardExtra3;
    private TextView txtP1Value, txtP1Info;
    private TextView txtSolarValue, txtSolarInfo;
    private TextView txtBatteryValue, txtBatterySoc;
    private TextView txtGasValue;
    private TextView txtWaterValue;
    private TextView txtTemperatureValue;
    private TextView txtHomePower, txtHomeTotal;
    private TextView txtExtra1Value, txtExtra2Value, txtExtra3Value;
    private EnergyFlowView energyFlowView;

    // ImageViews for dynamic extra icons
    private ImageView imgGasIcon, imgWaterIcon, imgExtra1Icon, imgExtra2Icon, imgExtra3Icon;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        setActionbar(getString(R.string.title_energy));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_energy, container, false);

        layout = view.findViewById(R.id.energy_layout);
        txtLoading = view.findViewById(R.id.energy_loading);

        energyFlowView = view.findViewById(R.id.energy_flow_view);
        cardP1 = view.findViewById(R.id.card_p1);
        cardSolar = view.findViewById(R.id.card_solar);
        cardBattery = view.findViewById(R.id.card_battery);
        cardGas = view.findViewById(R.id.card_gas);
        cardWater = view.findViewById(R.id.card_water);
        cardHome = view.findViewById(R.id.card_home);
        cardExtra1 = view.findViewById(R.id.card_extra1);
        cardExtra2 = view.findViewById(R.id.card_extra2);
        cardExtra3 = view.findViewById(R.id.card_extra3);
        tempContainer = view.findViewById(R.id.temp_container);

        txtP1Value = view.findViewById(R.id.txt_p1_value);
        txtP1Info = view.findViewById(R.id.txt_p1_info);
        txtSolarValue = view.findViewById(R.id.txt_solar_value);
        txtSolarInfo = view.findViewById(R.id.txt_solar_info);
        txtBatteryValue = view.findViewById(R.id.txt_battery_value);
        txtBatterySoc = view.findViewById(R.id.txt_battery_soc);
        txtGasValue = view.findViewById(R.id.txt_gas_value);
        txtWaterValue = view.findViewById(R.id.txt_water_value);
        txtTemperatureValue = view.findViewById(R.id.txt_temperature_value);
        txtHomePower = view.findViewById(R.id.txt_home_power);
        txtHomeTotal = view.findViewById(R.id.txt_home_total);
        txtExtra1Value = view.findViewById(R.id.txt_extra1_value);
        txtExtra2Value = view.findViewById(R.id.txt_extra2_value);
        txtExtra3Value = view.findViewById(R.id.txt_extra3_value);

        // ImageViews
        imgGasIcon = view.findViewById(R.id.img_gas_icon);
        imgWaterIcon = view.findViewById(R.id.img_water_icon);
        imgExtra1Icon = view.findViewById(R.id.img_extra1_icon);
        imgExtra2Icon = view.findViewById(R.id.img_extra2_icon);
        imgExtra3Icon = view.findViewById(R.id.img_extra3_icon);

        setTheme();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide the sort FAB on energy screen
        setSortFab(false);

        getEnergyDashboard();
    }

    @Override
    public void refreshFragment() {
        deviceCache.clear();
        getEnergyDashboard();
    }

    @Override
    public void onConnectionOk() {
        if (getView() != null) {
            getEnergyDashboard();
        }
    }

    @Override
    public void onConnectionFailed() {
        getEnergyDashboard();
    }

    @Override
    public void errorHandling(Exception error, View frameLayout) {
        if (error != null && isAdded()) {
            super.errorHandling(error, frameLayout);
        }
    }

    private void getEnergyDashboard() {
        if (txtLoading != null)
            txtLoading.setVisibility(View.VISIBLE);
        if (layout != null)
            layout.setVisibility(View.GONE);

        try {
            StaticHelper.getDomoticz(mContext).getEnergyDashboard(new EnergyDashboardReceiver() {
                @Override
                public void onReceiveEnergyDashboard(EnergyDashboardInfo dashboard) {
                    energyDashboard = dashboard;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Log.d(TAG, "Energy dashboard loaded: " + dashboard.toString());
                            // Apply icons for extras based on API settings
                            applyExtraIcons();
                            loadDeviceData();
                        });
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (txtLoading != null) {
                                txtLoading.setText(R.string.msg_please_wait);
                                txtLoading.setVisibility(View.VISIBLE);
                            }
                            if (layout != null)
                                layout.setVisibility(View.GONE);
                            Log.e(TAG, "Error loading energy dashboard: " + error.getMessage());
                        });
                    }
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Exception getting energy dashboard: " + ex.getMessage());
        }
    }

    private void applyExtraIcons() {
        if (energyDashboard == null || !isAdded()) return;

        // Gas icon (idGas) - use extra icon mapping if set, otherwise keep mode_heat as default
        try {
            String gasIcon = null;
            // Domoticz ESettings may not include a separate icon for gas; keep default
            if (gasIcon != null && imgGasIcon != null) {
                int res = getDrawableIdForIcon(gasIcon);
                if (res != 0) imgGasIcon.setImageResource(res);
            }
        } catch (Exception e) {
            Log.d(TAG, "applyExtraIcons gas: " + e.getMessage());
        }

        // Water icon remains drop by default, but allow override if API supplies icon for extras
        try {
            if (imgWaterIcon != null) {
                // keep default (drop) - no API icon for water in ESettings
            }
        } catch (Exception e) {
            Log.d(TAG, "applyExtraIcons water: " + e.getMessage());
        }

        // Extra1
        try {
            if (imgExtra1Icon != null) {
                String icon = energyDashboard.getExtra1Icon();
                int res = getDrawableIdForIcon(icon);
                if (res != 0) imgExtra1Icon.setImageResource(res);
            }
        } catch (Exception e) {
            Log.d(TAG, "applyExtraIcons extra1: " + e.getMessage());
        }

        // Extra3 (extra2 currently unused in layout)
        try {
            if (imgExtra3Icon != null) {
                String icon = energyDashboard.getExtra3Icon();
                int res = getDrawableIdForIcon(icon);
                if (res != 0) imgExtra3Icon.setImageResource(res);
            }
        } catch (Exception e) {
            Log.d(TAG, "applyExtraIcons extra3: " + e.getMessage());
        }

        // Extra2
        try {
            if (imgExtra2Icon != null) {
                String icon = energyDashboard.getExtra2Icon();
                int res = getDrawableIdForIcon(icon);
                if (res != 0) imgExtra2Icon.setImageResource(res);
            }
        } catch (Exception e) {
            Log.d(TAG, "applyExtraIcons extra2: " + e.getMessage());
        }
    }

    private int getDrawableIdForIcon(String iconName) {
        if (iconName == null || iconName.trim().isEmpty()) {
            // fallback icon (use a small built-in or app drawable)
            int fallback = getResources().getIdentifier("factory_24px", "drawable", mContext.getPackageName());
            return fallback != 0 ? fallback : android.R.drawable.ic_menu_help;
        }

        String icon = iconName.trim().toLowerCase(Locale.getDefault());

        // First try direct resource lookup (some icons may be present in domoticzapi or app)
        int resId = getResources().getIdentifier(icon, "drawable", mContext.getPackageName());
        if (resId != 0) return resId;

        // Map known API names to local drawable names
        switch (icon) {
            case "heater":
            case "heat":
            case "wave": // treat wave like heater (radiator)
                resId = getResources().getIdentifier("mode_heat_24px", "drawable", mContext.getPackageName());
                break;
            case "car":
                resId = getResources().getIdentifier("car", "drawable", mContext.getPackageName());
                break;
            case "power":
            case "solar":
                // prefer solar_power icon for production/power
                resId = getResources().getIdentifier("solar_power_24px", "drawable", mContext.getPackageName());
                if (resId == 0)
                    resId = getResources().getIdentifier("power", "drawable", mContext.getPackageName());
                break;
            case "battery":
                resId = getResources().getIdentifier("battery_charging_90_24px", "drawable", mContext.getPackageName());
                break;
            default:
                // Try to find with common prefixes/suffixes
                resId = getResources().getIdentifier(icon + "_24px", "drawable", mContext.getPackageName());
                if (resId == 0) {
                    resId = getResources().getIdentifier(icon + "_24", "drawable", mContext.getPackageName());
                }
                break;
        }

        if (resId != 0) return resId;

        // final fallback
        int fallback = getResources().getIdentifier("factory_24px", "drawable", mContext.getPackageName());
        return fallback != 0 ? fallback : android.R.drawable.ic_menu_help;
    }

    private void loadDeviceData() {
        if (energyDashboard == null) return;

        if (energyDashboard.getIdP1() > 0) {
            loadDevice(energyDashboard.getIdP1(), this::updateP1Display);
        }
        if (energyDashboard.getIdSolar() > 0) {
            loadDevice(energyDashboard.getIdSolar(), this::updateSolarDisplay);
        }
        if (energyDashboard.getIdBatteryWatt() > 0) {
            loadDevice(energyDashboard.getIdBatteryWatt(), this::updateBatteryDisplay);
        }
        if (energyDashboard.getIdBatterySoc() > 0) {
            loadDevice(energyDashboard.getIdBatterySoc(), this::updateBatterySoCDisplay);
        }
        if (energyDashboard.getIdGas() > 0) {
            loadDevice(energyDashboard.getIdGas(), this::updateGasDisplay);
        }
        if (energyDashboard.getIdWater() > 0) {
            loadDevice(energyDashboard.getIdWater(), this::updateWaterDisplay);
        }
        if (energyDashboard.getIdOutsideTempSensor() > 0) {
            loadDevice(energyDashboard.getIdOutsideTempSensor(), this::updateTemperatureDisplay);
        }
        if (energyDashboard.getIdExtra1() > 0) {
            loadDevice(energyDashboard.getIdExtra1(), this::updateExtra1Display);
        }
        if (energyDashboard.getIdExtra2() > 0) {
            loadDevice(energyDashboard.getIdExtra2(), this::updateExtra2Display);
        }
        if (energyDashboard.getIdExtra3() > 0) {
            loadDevice(energyDashboard.getIdExtra3(), this::updateExtra3Display);
        }

        if (txtLoading != null)
            txtLoading.setVisibility(View.GONE);
        if (layout != null)
            layout.setVisibility(View.VISIBLE);
    }

    private void loadDevice(int deviceIdx, DeviceUpdateCallback callback) {
        try {
            StaticHelper.getDomoticz(mContext).getDevice(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                }

                @Override
                public void onReceiveDevice(DevicesInfo device) {
                    if (device != null && getActivity() != null) {
                        deviceCache.put(deviceIdx, device);
                        getActivity().runOnUiThread(() -> callback.onDeviceLoaded(device));
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error loading device " + deviceIdx + ": " + error.getMessage());
                }
            }, deviceIdx, false);
        } catch (Exception ex) {
            Log.e(TAG, "Exception loading device " + deviceIdx + ": " + ex.getMessage());
        }
    }

    private void updateP1Display(DevicesInfo device) {
        if (cardP1 == null) return;
        cardP1.setVisibility(View.VISIBLE);

        // For P1, show current USAGE (net power consumption)
        if (txtP1Value != null) {
            String usage = device.getUsage();
            if (usage != null && !usage.isEmpty()) {
                // Calculate net usage (usage - delivery)
                try {
                    int usageWatt = Integer.parseInt(usage.replace("Watt", "").replace("W", "").trim());
                    int deliveryWatt = 0;

                    if (device.getUsageDeliv() != null && !device.getUsageDeliv().isEmpty()) {
                        deliveryWatt = Integer.parseInt(device.getUsageDeliv().replace("Watt", "").replace("W", "").trim());
                    }

                    int netWatt = usageWatt - deliveryWatt;
                    txtP1Value.setText(netWatt + " Watt");
                } catch (Exception e) {
                    txtP1Value.setText(usage.replace(" Watt", "W"));
                }
            } else {
                String data = device.getData();
                txtP1Value.setText(data != null && !data.isEmpty() ? data : "0 Watt");
            }
        }

        // Show total counter in info text
        if (txtP1Info != null) {
            String counter = device.getCounter();
            if (counter != null && !counter.isEmpty()) {
                String arrow = "→ ";
                // Check if we're exporting (delivery > usage)
                try {
                    if (device.getUsage() != null && device.getUsageDeliv() != null) {
                        int usageWatt = Integer.parseInt(device.getUsage().replace("Watt", "").replace("W", "").trim());
                        int deliveryWatt = Integer.parseInt(device.getUsageDeliv().replace("Watt", "").replace("W", "").trim());
                        if (deliveryWatt > usageWatt) {
                            arrow = "← ";
                        }
                    }
                } catch (Exception e) {
                    // Keep default arrow
                }
                txtP1Info.setText(arrow + counter);
            } else {
                txtP1Info.setText("");
            }
        }

        updateFlowAnimation();
    }

    private void updateSolarDisplay(DevicesInfo device) {
        if (cardSolar == null) return;
        cardSolar.setVisibility(View.VISIBLE);

        // Show current solar power from getUsage() which contains current production
        if (txtSolarValue != null) {
            String usage = device.getUsage();
            if (usage != null && !usage.isEmpty()) {
                txtSolarValue.setText(usage.replace(" Watt", "W"));
            } else {
                String data = device.getData();
                txtSolarValue.setText(data != null && !data.isEmpty() ? data : "0 Watt");
            }
        }

        // Show daily total in smaller text
        if (txtSolarInfo != null) {
            String counterToday = device.getCounterToday();
            if (counterToday != null && !counterToday.isEmpty()) {
                txtSolarInfo.setText(counterToday);
            } else {
                txtSolarInfo.setText("0 kWh");
            }
        }

        updateFlowAnimation();
    }

    private void updateBatteryDisplay(DevicesInfo device) {
        if (cardBattery == null) return;
        cardBattery.setVisibility(View.VISIBLE);

        if (txtBatteryValue != null) {
            // Show current power from Usage
            String usage = device.getUsage();
            if (usage != null && !usage.isEmpty()) {
                txtBatteryValue.setText(usage.replace(" Watt", "W"));
            } else {
                String data = device.getData();
                txtBatteryValue.setText(data != null && !data.isEmpty() ? data : "0 Watt");
            }
        }

        updateFlowAnimation();
    }

    private void updateBatterySoCDisplay(DevicesInfo device) {
        if (cardBattery == null) return;
        cardBattery.setVisibility(View.VISIBLE);

        try {
            String data = device.getData();
            if (data != null && !data.isEmpty()) {
                String percentStr = data.replace("%", "").trim();
                int percent = Integer.parseInt(percentStr);

                if (txtBatterySoc != null) {
                    txtBatterySoc.setText(percent + "%");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing battery SoC: " + e.getMessage());
        }
    }

    private void updateGasDisplay(DevicesInfo device) {
        if (cardGas == null) return;
        cardGas.setVisibility(View.VISIBLE);

        if (txtGasValue != null) {
            // Show today's consumption or counter
            String counterToday = device.getCounterToday();
            if (counterToday != null && !counterToday.isEmpty()) {
                txtGasValue.setText(counterToday);
            } else {
                String counter = device.getCounter();
                txtGasValue.setText(counter != null && !counter.isEmpty() ? counter : "0 m³");
            }
        }
    }

    private void updateWaterDisplay(DevicesInfo device) {
        if (cardWater == null) return;
        cardWater.setVisibility(View.VISIBLE);

        if (txtWaterValue != null) {
            // Show today's consumption (already in m3 or converted format)
            String counterToday = device.getCounterToday();
            if (counterToday != null && !counterToday.isEmpty()) {
                // counterToday is already formatted (e.g., "0.530 m3"), just display it
                // If conversion is enabled, convert m3 to liters
                if (energyDashboard != null && energyDashboard.isConvertWaterM3ToLiter() && counterToday.contains("m3")) {
                    try {
                        double m3 = Double.parseDouble(counterToday.replaceAll("[^0-9.]", ""));
                        txtWaterValue.setText(String.format(Locale.getDefault(), "%.0f L", m3 * 1000));
                    } catch (Exception e) {
                        txtWaterValue.setText(counterToday);
                    }
                } else {
                    txtWaterValue.setText(counterToday);
                }
            } else {
                txtWaterValue.setText("0 L");
            }
        }
    }

    private void updateTemperatureDisplay(DevicesInfo device) {
        if (tempContainer == null) return;
        tempContainer.setVisibility(View.VISIBLE);

        if (txtTemperatureValue != null) {
            String data = device.getData();
            txtTemperatureValue.setText(data != null && !data.isEmpty() ? data : "--");
        }
    }

    private void updateFlowAnimation() {
        if (energyFlowView == null) return;

        // Extract power values
        int solarPower = extractPower(txtSolarValue);
        int batteryPower = extractPower(txtBatteryValue);
        int gridPower = extractPower(txtP1Value);

        // Calculate home power consumption
        // Home = Grid + Solar + Battery (considering battery is negative when discharging to home)
        int homePower = gridPower + solarPower - batteryPower;

        // Ensure home power is not negative
        if (homePower < 0) {
            homePower = 0;
        }

        // Update home display
        if (txtHomePower != null) {
            txtHomePower.setText(homePower + " Watt");
        }

        // Update home total counter from P1 device if available
        if (txtHomeTotal != null && deviceCache.containsKey(energyDashboard.getIdP1())) {
            DevicesInfo p1Device = deviceCache.get(energyDashboard.getIdP1());
            if (p1Device != null) {
                String counter = p1Device.getCounter();
                if (counter != null && !counter.isEmpty()) {
                    txtHomeTotal.setText(counter);
                } else {
                    txtHomeTotal.setText("--");
                }
            }
        }

        // Update flow view
        energyFlowView.updatePowerValues(solarPower, batteryPower, gridPower, homePower);
    }

    private int extractPower(TextView textView) {
        if (textView == null) return 0;
        try {
            String text = textView.getText().toString();
            String numberStr = text.replaceAll("[^0-9-]", "");
            if (numberStr.isEmpty()) return 0;
            return Integer.parseInt(numberStr);
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateExtra1Display(DevicesInfo device) {
        if (cardExtra1 == null) return;
        cardExtra1.setVisibility(View.VISIBLE);

        if (txtExtra1Value != null) {
            String data = device.getData();
            if (data != null && !data.isEmpty()) {
                txtExtra1Value.setText(data);
            } else {
                String counterToday = device.getCounterToday();
                if (counterToday != null && !counterToday.isEmpty()) {
                    txtExtra1Value.setText(counterToday);
                } else {
                    txtExtra1Value.setText("--");
                }
            }
        }
    }

    private void updateExtra2Display(DevicesInfo device) {
        if (cardExtra2 == null) return;
        cardExtra2.setVisibility(View.VISIBLE);

        if (txtExtra2Value != null) {
            String data = device.getData();
            if (data != null && !data.isEmpty()) {
                txtExtra2Value.setText(data);
            } else {
                String counterToday = device.getCounterToday();
                if (counterToday != null && !counterToday.isEmpty()) {
                    txtExtra2Value.setText(counterToday);
                } else {
                    txtExtra2Value.setText("--");
                }
            }
        }
    }

    private void updateExtra3Display(DevicesInfo device) {
        if (cardExtra3 == null) return;
        cardExtra3.setVisibility(View.VISIBLE);

        if (txtExtra3Value != null) {
            String data = device.getData();
            if (data != null && !data.isEmpty()) {
                txtExtra3Value.setText(data);
            } else {
                String counterToday = device.getCounterToday();
                if (counterToday != null && !counterToday.isEmpty()) {
                    txtExtra3Value.setText(counterToday);
                } else {
                    txtExtra3Value.setText("--");
                }
            }
        }
    }

    @Override
    public void Filter(String text) {
    }

    private interface DeviceUpdateCallback {
        void onDeviceLoaded(DevicesInfo device);
    }
}

