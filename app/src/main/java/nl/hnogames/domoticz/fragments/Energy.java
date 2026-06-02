package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;

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

    private NestedScrollView layout;
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

    // MaterialCardView wrappers for extras
    private MaterialCardView cardGasWrapper, cardWaterWrapper, cardExtra1Wrapper, cardExtra2Wrapper, cardExtra3Wrapper;

    // Counter for node center computation attempts
    private int nodeCenterComputeAttempts = 0;

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

        layout = view.findViewById(R.id.energy_scroll);
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

        // Material wrapper cards
        cardGasWrapper = view.findViewById(R.id.card_gas_wrapper);
        cardWaterWrapper = view.findViewById(R.id.card_water_wrapper);
        cardExtra1Wrapper = view.findViewById(R.id.card_extra1_wrapper);
        cardExtra2Wrapper = view.findViewById(R.id.card_extra2_wrapper);
        cardExtra3Wrapper = view.findViewById(R.id.card_extra3_wrapper);

        // Ensure wrappers are hidden initially
        if (cardGasWrapper != null) cardGasWrapper.setVisibility(View.GONE);
        if (cardWaterWrapper != null) cardWaterWrapper.setVisibility(View.GONE);
        if (cardExtra1Wrapper != null) cardExtra1Wrapper.setVisibility(View.GONE);
        if (cardExtra2Wrapper != null) cardExtra2Wrapper.setVisibility(View.GONE);
        if (cardExtra3Wrapper != null) cardExtra3Wrapper.setVisibility(View.GONE);

        // Main nodes start invisible — revealed all at once after data loads
        // (layout uses android:visibility="invisible" already, just confirm)
        if (cardP1 != null) cardP1.setVisibility(View.INVISIBLE);
        if (cardBattery != null) cardBattery.setVisibility(View.INVISIBLE);
        if (cardSolar != null) cardSolar.setVisibility(View.INVISIBLE);
        if (cardHome != null) cardHome.setVisibility(View.INVISIBLE);

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

        // Keep flow lines in sync when user scrolls
        if (layout != null) {
            layout.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        if (energyFlowView != null) {
                            energyFlowView.setScrollOffsetY(scrollY);
                        }
                    });
        }
    }

    private void computeAndSetNodeCenters() {
        if (energyFlowView == null || cardP1 == null || cardBattery == null
                || cardSolar == null || cardHome == null) return;

        if (nodeCenterComputeAttempts > 6) { nodeCenterComputeAttempts = 0; return; }

        // Use getLocationInWindow so we get correct coords even inside scrollviews
        int[] flowLoc = new int[2];
        energyFlowView.getLocationInWindow(flowLoc);

        int[] loc = new int[2];

        cardP1.getLocationInWindow(loc);
        float gridX = (loc[0] - flowLoc[0]) + cardP1.getWidth() / 2f;
        float gridY = (loc[1] - flowLoc[1]) + cardP1.getHeight() / 2f;
        float gridR  = cardP1.getWidth() / 2f;

        cardBattery.getLocationInWindow(loc);
        float batteryX = (loc[0] - flowLoc[0]) + cardBattery.getWidth() / 2f;
        float batteryY = (loc[1] - flowLoc[1]) + cardBattery.getHeight() / 2f;
        float batteryR = cardBattery.getWidth() / 2f;

        cardSolar.getLocationInWindow(loc);
        float solarX = (loc[0] - flowLoc[0]) + cardSolar.getWidth() / 2f;
        float solarY = (loc[1] - flowLoc[1]) + cardSolar.getHeight() / 2f;
        float solarR = cardSolar.getWidth() / 2f;

        cardHome.getLocationInWindow(loc);
        float homeX = (loc[0] - flowLoc[0]) + cardHome.getWidth() / 2f;
        float homeY = (loc[1] - flowLoc[1]) + cardHome.getHeight() / 2f;
        float homeR = cardHome.getWidth() / 2f;

        // Sanity: all radii and positions should be > 0
        if (gridR <= 0 || homeR <= 0) {
            nodeCenterComputeAttempts++;
            energyFlowView.postDelayed(this::computeAndSetNodeCenters, 150);
            return;
        }
        nodeCenterComputeAttempts = 0;

        energyFlowView.setNodeCenters(
                gridX, gridY, gridR,
                batteryX, batteryY, batteryR,
                solarX, solarY, solarR,
                homeX, homeY, homeR);
    }

    @Override
    public void refreshFragment() {
        deviceCache.clear();
        // Reset loaded state so numeric values will be revealed only after fresh data arrives
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
        // reset loaded flags for a fresh cycle

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

        // Build deduplicated list of all needed device IDs for a SINGLE bulk request
        ArrayList<Integer> ids = new ArrayList<>();
        addIdIfValid(ids, energyDashboard.getIdP1());
        addIdIfValid(ids, energyDashboard.getIdSolar());
        addIdIfValid(ids, energyDashboard.getIdBatteryWatt());
        addIdIfValid(ids, energyDashboard.getIdBatterySoc());
        addIdIfValid(ids, energyDashboard.getIdGas());
        addIdIfValid(ids, energyDashboard.getIdWater());
        addIdIfValid(ids, energyDashboard.getIdOutsideTempSensor());
        addIdIfValid(ids, energyDashboard.getIdExtra1());
        addIdIfValid(ids, energyDashboard.getIdExtra2());
        addIdIfValid(ids, energyDashboard.getIdExtra3());

        if (ids.isEmpty()) {
            revealLayout();
            return;
        }

        try {
            StaticHelper.getDomoticz(mContext).getDevicesByIds(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                    if (getActivity() == null) return;
                    // Cache all returned devices by idx
                    for (DevicesInfo d : devices) {
                        deviceCache.put(d.getIdx(), d);
                    }
                    getActivity().runOnUiThread(() -> {
                        // Dispatch each update using the cache
                        if (energyDashboard.getIdP1() > 0 && deviceCache.containsKey(energyDashboard.getIdP1()))
                            updateP1Display(deviceCache.get(energyDashboard.getIdP1()));
                        if (energyDashboard.getIdSolar() > 0 && deviceCache.containsKey(energyDashboard.getIdSolar()))
                            updateSolarDisplay(deviceCache.get(energyDashboard.getIdSolar()));
                        if (energyDashboard.getIdBatteryWatt() > 0 && deviceCache.containsKey(energyDashboard.getIdBatteryWatt()))
                            updateBatteryDisplay(deviceCache.get(energyDashboard.getIdBatteryWatt()));
                        if (energyDashboard.getIdBatterySoc() > 0 && deviceCache.containsKey(energyDashboard.getIdBatterySoc()))
                            updateBatterySoCDisplay(deviceCache.get(energyDashboard.getIdBatterySoc()));
                        if (energyDashboard.getIdGas() > 0 && deviceCache.containsKey(energyDashboard.getIdGas()))
                            updateGasDisplay(deviceCache.get(energyDashboard.getIdGas()));
                        if (energyDashboard.getIdWater() > 0 && deviceCache.containsKey(energyDashboard.getIdWater()))
                            updateWaterDisplay(deviceCache.get(energyDashboard.getIdWater()));
                        if (energyDashboard.getIdOutsideTempSensor() > 0 && deviceCache.containsKey(energyDashboard.getIdOutsideTempSensor()))
                            updateTemperatureDisplay(deviceCache.get(energyDashboard.getIdOutsideTempSensor()));
                        if (energyDashboard.getIdExtra1() > 0 && deviceCache.containsKey(energyDashboard.getIdExtra1()))
                            updateExtra1Display(deviceCache.get(energyDashboard.getIdExtra1()));
                        if (energyDashboard.getIdExtra2() > 0 && deviceCache.containsKey(energyDashboard.getIdExtra2()))
                            updateExtra2Display(deviceCache.get(energyDashboard.getIdExtra2()));
                        if (energyDashboard.getIdExtra3() > 0 && deviceCache.containsKey(energyDashboard.getIdExtra3()))
                            updateExtra3Display(deviceCache.get(energyDashboard.getIdExtra3()));
                        revealLayout();
                    });
                }

                @Override
                public void onReceiveDevice(DevicesInfo device) {
                    // Not used in bulk mode
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error bulk loading devices: " + error.getMessage());
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> revealLayout());
                }
            }, ids);
        } catch (Exception ex) {
            Log.e(TAG, "Exception in loadDeviceData: " + ex.getMessage());
            revealLayout();
        }
    }

    private void addIdIfValid(ArrayList<Integer> list, int id) {
        if (id > 0 && !list.contains(id)) list.add(id);
    }

    /** Show the scroll view and animate all nodes in together. */
    private void revealLayout() {
        if (layout == null) return;
        if (txtLoading != null) txtLoading.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);

        // Reveal all four main nodes together with a smooth scale+fade
        revealNode(cardP1);
        revealNode(cardBattery);
        revealNode(cardSolar);
        revealNode(cardHome);

        // Compute node centers after layout pass
        if (energyFlowView != null) {
            energyFlowView.post(this::computeAndSetNodeCenters);
        }
        // Update flow lines
        updateFlowAnimation();
    }

    private void revealNode(View v) {
        if (v == null) return;
        v.setAlpha(0f);
        v.setScaleX(0.85f);
        v.setScaleY(0.85f);
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(280).start();
    }


    private void updateP1Display(DevicesInfo device) {
        if (cardP1 == null) return;

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
                    setTextAnimated(txtP1Value, netWatt + " Watt");
                } catch (Exception e) {
                    setTextAnimated(txtP1Value, usage.replace(" Watt", "W"));
                }
            } else {
                String data = device.getData();
                setTextAnimated(txtP1Value, data != null && !data.isEmpty() ? data : "0 Watt");
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
                setTextAnimated(txtP1Info, arrow + counter);
            } else {
                txtP1Info.setText("");
            }
        }

        // update flow immediately using device cache
        // (full flow update happens in revealLayout; this is a no-op until then)
    }

    private void updateSolarDisplay(DevicesInfo device) {
        if (cardSolar == null) return;

        // Show current solar power from getUsage() which contains current production
        if (txtSolarValue != null) {
            String usage = device.getUsage();
            String data = device.getData();
            if (usage != null && !usage.isEmpty()) {
                setTextAnimated(txtSolarValue, usage.replace(" Watt", "W"));
            } else {
                setTextAnimated(txtSolarValue, data != null && !data.isEmpty() ? data : "0 Watt");
            }
        }

        // Show daily total in smaller text
        if (txtSolarInfo != null) {
            String counterToday = device.getCounterToday();
            if (counterToday != null && !counterToday.isEmpty()) {
                setTextAnimated(txtSolarInfo, counterToday);
            } else {
                setTextAnimated(txtSolarInfo, "0 kWh");
            }
        }

        // update flow immediately using device cache
        // (full flow update happens in revealLayout; this is a no-op until then)
    }

    private void updateBatteryDisplay(DevicesInfo device) {
        if (cardBattery == null) return;

        if (txtBatteryValue != null) {
            // Show current power from Usage
            String usage = device.getUsage();
            String data = device.getData();
            if (usage != null && !usage.isEmpty()) {
                setTextAnimated(txtBatteryValue, usage.replace(" Watt", "W"));
            } else {
                setTextAnimated(txtBatteryValue, data != null && !data.isEmpty() ? data : "0 Watt");
            }
        }

        // full flow update in revealLayout
    }

    private void updateBatterySoCDisplay(DevicesInfo device) {
        if (cardBattery == null) return;

        try {
            String data = device.getData();
            if (data != null && !data.isEmpty()) {
                String percentStr = data.replace("%", "").trim();
                int percent = Integer.parseInt(percentStr);

                if (txtBatterySoc != null) {
                    setTextAnimated(txtBatterySoc, percent + "%");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing battery SoC: " + e.getMessage());
        }
    }

    private void updateGasDisplay(DevicesInfo device) {
        if (cardGas == null) return;
        if (cardGasWrapper != null) showViewAnimated(cardGasWrapper);

        if (txtGasValue != null) {
            String counterToday = device.getCounterToday();
            if (counterToday != null && !counterToday.isEmpty()) {
                setTextAnimated(txtGasValue, counterToday);
            } else {
                String counter = device.getCounter();
                setTextAnimated(txtGasValue, counter != null && !counter.isEmpty() ? counter : "0 m³");
            }
        }
    }

    private void updateWaterDisplay(DevicesInfo device) {
        if (cardWater == null) return;
        if (cardWaterWrapper != null) showViewAnimated(cardWaterWrapper);

        if (txtWaterValue != null) {
            String counterToday = device.getCounterToday();
            if (counterToday != null && !counterToday.isEmpty()) {
                if (energyDashboard != null && energyDashboard.isConvertWaterM3ToLiter() && counterToday.contains("m3")) {
                    try {
                        double m3 = Double.parseDouble(counterToday.replaceAll("[^0-9.]", ""));
                        setTextAnimated(txtWaterValue, String.format(Locale.getDefault(), "%.0f L", m3 * 1000));
                    } catch (Exception e) {
                        setTextAnimated(txtWaterValue, counterToday);
                    }
                } else {
                    setTextAnimated(txtWaterValue, counterToday);
                }
            } else {
                setTextAnimated(txtWaterValue, "0 L");
            }
        }
    }

    private void updateTemperatureDisplay(DevicesInfo device) {
        if (tempContainer == null) return;
        tempContainer.setVisibility(View.VISIBLE);

        if (txtTemperatureValue != null) {
            String data = device.getData();
            setTextAnimated(txtTemperatureValue, data != null && !data.isEmpty() ? data : "--");
        }
    }

    // Smoothly show a view with fade+scale animation (no-op if already visible)
    private void showViewAnimated(final View v) {
        if (v == null) return;
        if (v.getVisibility() == View.VISIBLE && v.getAlpha() == 1f) return;
        // Make visible immediately but start from transparent/scaled down
        v.setAlpha(0f);
        v.setScaleX(0.92f);
        v.setScaleY(0.92f);
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(240).start();
    }

    // Animate text change (cross-fade) so values update smoothly
    private void setTextAnimated(final TextView tv, final String text) {
        if (tv == null) return;
        String current = tv.getText() == null ? "" : tv.getText().toString();
        String newText = text == null ? "" : text;
        if (newText.equals(current)) return;
        tv.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            tv.setText(newText);
            tv.animate().alpha(1f).setDuration(160).start();
        }).start();
    }

    // Set text on an invisible/hidden view without animating; will be revealed later
    private void setHiddenText(final TextView tv, final String text) {
        if (tv == null) return;
        tv.setText(text == null ? "" : text);
    }

    private void updateFlowAnimation() {
        if (energyFlowView == null || energyDashboard == null) return;

        // Read raw watt values directly from device cache for accuracy
        int solarPower   = getRawWatts(energyDashboard.getIdSolar());
        int batteryPower = getRawWatts(energyDashboard.getIdBatteryWatt());
        int gridPower    = getGridNetWatts(); // net: usage - delivery

        // Home power = Grid (net from grid) + Solar production + Battery discharge
        // Battery convention from Domoticz: positive = charging (absorbing), negative = discharging (providing)
        // home = grid + solar - batteryPower  (charging reduces available power, discharging adds)
        int homePower = gridPower + solarPower - batteryPower;
        if (homePower < 0) homePower = 0;

        // Update home display
        if (txtHomePower != null) {
            setTextAnimated(txtHomePower, homePower + " Watt");
        }

        // Update home total counter from P1 device if available
        if (txtHomeTotal != null && deviceCache.containsKey(energyDashboard.getIdP1())) {
            DevicesInfo p1Device = deviceCache.get(energyDashboard.getIdP1());
            if (p1Device != null) {
                String counter = p1Device.getCounter();
                if (counter != null && !counter.isEmpty()) {
                    setTextAnimated(txtHomeTotal, counter);
                } else {
                    setTextAnimated(txtHomeTotal, "--");
                }
            }
        }

        // Update flow view with signed values:
        //   gridPower  > 0 = consuming from grid (lines animate toward home)
        //   gridPower  < 0 = exporting to grid   (lines animate away from home)
        //   solarPower > 0 = producing            (lines animate toward home)
        //   batteryPower > 0 = charging           (lines animate home → battery)
        //   batteryPower < 0 = discharging        (lines animate battery → home)
        energyFlowView.updatePowerValues(solarPower, batteryPower, gridPower, homePower);
    }

    /** Returns net grid watts: positive = consuming, negative = exporting */
    private int getGridNetWatts() {
        if (energyDashboard == null) return 0;
        DevicesInfo d = deviceCache.get(energyDashboard.getIdP1());
        if (d == null) return 0;
        try {
            int usage    = parseWatts(d.getUsage());
            int delivery = parseWatts(d.getUsageDeliv());
            return usage - delivery;
        } catch (Exception e) {
            return parseWatts(d.getData());
        }
    }

    /** Returns watt value from a device's primary Usage or Data field (preserves sign). */
    private int getRawWatts(int deviceId) {
        if (deviceId <= 0) return 0;
        DevicesInfo d = deviceCache.get(deviceId);
        if (d == null) return 0;
        int w = parseWatts(d.getUsage());
        if (w == 0) w = parseWatts(d.getData());
        return w;
    }

    /** Parse watts from a string like "123 Watt", "-456 W", "123.4 W". Returns int (sign preserved). */
    private int parseWatts(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            String clean = s.trim().replaceAll("[^0-9.\\-]", "");
            if (clean.isEmpty() || clean.equals("-")) return 0;
            return (int) Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateExtra1Display(DevicesInfo device) {
        if (cardExtra1 == null) return;
        if (cardExtra1Wrapper != null) showViewAnimated(cardExtra1Wrapper);

        if (txtExtra1Value != null) {
            try {
                String configuredField = energyDashboard != null ? energyDashboard.getExtra1Field() : null;
                String value = getDeviceFieldValue(device, configuredField);
                txtExtra1Value.setText(value != null && !value.isEmpty() ? value : "--");
            } catch (Exception e) {
                txtExtra1Value.setText("--");
            }
        }
    }

    private void updateExtra2Display(DevicesInfo device) {
        if (cardExtra2 == null) return;
        if (cardExtra2Wrapper != null) showViewAnimated(cardExtra2Wrapper);

        if (txtExtra2Value != null) {
            try {
                String configuredField = energyDashboard != null ? energyDashboard.getExtra2Field() : null;
                String value = getDeviceFieldValue(device, configuredField);
                txtExtra2Value.setText(value != null && !value.isEmpty() ? value : "--");
            } catch (Exception e) {
                txtExtra2Value.setText("--");
            }
        }
    }

    private void updateExtra3Display(DevicesInfo device) {
        if (cardExtra3 == null) return;
        if (cardExtra3Wrapper != null) showViewAnimated(cardExtra3Wrapper);

        if (txtExtra3Value != null) {
            try {
                String configuredField = energyDashboard != null ? energyDashboard.getExtra3Field() : null;
                String value = getDeviceFieldValue(device, configuredField);
                txtExtra3Value.setText(value != null && !value.isEmpty() ? value : "--");
            } catch (Exception e) {
                txtExtra3Value.setText("--");
            }
        }
    }

    // Helper to return the requested field from a device for extra displays
    private String getDeviceFieldValue(DevicesInfo device, String field) {
        if (device == null) return null;
        if (field == null) field = "";
        String f = field.trim().toLowerCase(Locale.getDefault());

        switch (f) {
            case "data":
                return device.getData();
            case "usage":
                // usage usually contains a string like "123 Watt" or "-456 W"
                if (device.getUsage() != null && !device.getUsage().isEmpty()) return device.getUsage().replace(" Watt", "W");
                return device.getData();
            case "counter":
                if (device.getCounter() != null && !device.getCounter().isEmpty()) return device.getCounter();
                return device.getCounterToday();
            case "countertoday":
                if (device.getCounterToday() != null && !device.getCounterToday().isEmpty()) return device.getCounterToday();
                return device.getCounter();
            default:
                // fallback: prefer Data, then CounterToday, then Usage
                if (device.getData() != null && !device.getData().isEmpty()) return device.getData();
                if (device.getCounterToday() != null && !device.getCounterToday().isEmpty()) return device.getCounterToday();
                if (device.getUsage() != null && !device.getUsage().isEmpty()) return device.getUsage();
                return "--";
        }
    }

    @Override
    public void Filter(String text) {
    }
}

