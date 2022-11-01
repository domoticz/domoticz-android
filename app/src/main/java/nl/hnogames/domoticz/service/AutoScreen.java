package nl.hnogames.domoticz.service;

import static androidx.car.app.model.CarColor.BLUE;
import static androidx.car.app.model.CarColor.GREEN;
import static androidx.car.app.model.CarColor.RED;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.car.app.CarToast;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.ForegroundCarColorSpan;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

class AutoScreen extends Screen implements DefaultLifecycleObserver {
    ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
    ListTemplate.Builder templateBuilder = new ListTemplate.Builder();
    boolean isFinishedLoading = false;
    int x = 0;
    private SharedPrefUtil mSharedPrefs;
    private ItemList.Builder itemlist;

    protected AutoScreen(@NonNull @NotNull CarContext carContext) {
        super(carContext);
        getLifecycle().addObserver(this);
    }

    public static CharSequence colorize(String s, CarColor color, int index, int length) {
        SpannableString ss = new SpannableString(s);
        ss.setSpan(
                ForegroundCarColorSpan.create(color),
                index,
                index + length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        templateBuilder.setTitle(getCarContext().getResources().getString(R.string.app_name_domoticz));
        templateBuilder.setHeaderAction(Action.APP_ICON);
        if (isFinishedLoading) {
            return templateBuilder.build();
        } else {
            return templateBuilder.setLoading(true).build();
        }
    }

    public void errorHandling(Exception error) {
        Log.d("android auto", "errorHandling: ", error);
    }

    public void AddDeviceToList(DevicesInfo mDevicesInfo) {
        supportedSwitches.add(mDevicesInfo);
        CharSequence charSequence;

        String status = mDevicesInfo.getData();
        if (!UsefulBits.isEmpty(mDevicesInfo.getUsage()))
            status = getCarContext().getString(R.string.usage) + ": " + mDevicesInfo.getUsage();
        if (!UsefulBits.isEmpty(mDevicesInfo.getCounterToday()))
            status += " " + getCarContext().getString(R.string.today) + ": " + mDevicesInfo.getCounterToday();
        if (!UsefulBits.isEmpty(mDevicesInfo.getCounter()) && !mDevicesInfo.getCounter().equals(mDevicesInfo.getData()))
            status += " " + getCarContext().getString(R.string.total) + ": " + mDevicesInfo.getCounter();
        if (mDevicesInfo.getType().equals("Wind"))
            status = getCarContext().getString(R.string.direction) + " " + mDevicesInfo.getDirection() + " " + mDevicesInfo.getDirectionStr();
        if (!UsefulBits.isEmpty(mDevicesInfo.getForecastStr()))
            status = mDevicesInfo.getForecastStr();
        if (!UsefulBits.isEmpty(mDevicesInfo.getSpeed()))
            status += ", " + getCarContext().getString(R.string.speed) + ": " + mDevicesInfo.getSpeed();
        if (mDevicesInfo.getDewPoint() > 0)
            status += ", " + getCarContext().getString(R.string.dewPoint) + ": " + mDevicesInfo.getDewPoint();
        if (mDevicesInfo.getTemp() > 0)
            status += ", " + getCarContext().getString(R.string.temp) + ": " + mDevicesInfo.getTemp();
        if (mDevicesInfo.getBarometer() > 0)
            status += ", " + getCarContext().getString(R.string.pressure) + ": " + mDevicesInfo.getBarometer();
        if (!UsefulBits.isEmpty(mDevicesInfo.getChill()))
            status += ", " + getCarContext().getString(R.string.chill) + ": " + mDevicesInfo.getChill();
        if (!UsefulBits.isEmpty(mDevicesInfo.getHumidityStatus()))
            status += ", " + getCarContext().getString(R.string.humidity) + ": " + mDevicesInfo.getHumidityStatus();

        if (status.equals("Off"))
            charSequence = colorize(status, RED, 0, status.length());
        else if (status.equals("On"))
            charSequence = colorize(status, BLUE, 0, status.length());
        else
            charSequence = colorize(status, GREEN, 0, status.length());

        if (x < 6) {
            itemlist.addItem(new Row.Builder().setTitle(mDevicesInfo.getName()).setOnClickListener(() -> onClick(mDevicesInfo)).addText(charSequence).build());
            x++;
        }
    }

    public void getSwitch() {
        try {
            if (mSharedPrefs == null)
                mSharedPrefs = new SharedPrefUtil(getCarContext());

            Log.d("AA Service", "getSwitch triggered");
            itemlist = new ItemList.Builder();
            x = 0;
            StaticHelper.getDomoticz(getCarContext()).getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                    Log.d("AA Service", "Devices received");
                    int x = 0;
                    if (!mSharedPrefs.showCustomAndroidAuto() || (mSharedPrefs.getAutoSwitches() == null || mSharedPrefs.getAutoSwitches().length <= 0)) {
                        for (DevicesInfo mDevicesInfo : switches) {
                            String name = mDevicesInfo.getName();
                            if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) && mDevicesInfo.getFavoriteBoolean()) {//only dashboard switches..
                                AddDeviceToList(mDevicesInfo);
                            }
                        }
                    } else {
                        String[] filterSwitches = mSharedPrefs.getAutoSwitches();
                        if (filterSwitches != null && filterSwitches.length > 0) {
                            for (DevicesInfo mDevicesInfo : switches) {
                                String name = mDevicesInfo.getName();
                                String idx = mDevicesInfo.getIdx() + "";
                                if (!name.startsWith(Domoticz.HIDDEN_CHARACTER)) {
                                    for (String f : filterSwitches) {
                                        if (f.equals(idx)) {
                                            AddDeviceToList(mDevicesInfo);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    templateBuilder.setLoading(false);
                    templateBuilder.setSingleList(itemlist.build());
                    isFinishedLoading = true;
                    invalidate();
                }

                @Override
                public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                }

                @Override
                public void onError(Exception error) {
                    Log.d("AA Service", "onError: ", error);
                    itemlist.addItem(new Row.Builder().setTitle(getCarContext().getResources().getString(R.string.error_notConnected)).build());
                    templateBuilder.setLoading(false);
                    templateBuilder.setSingleList(itemlist.build());
                    isFinishedLoading = true;
                    invalidate();
                }
            }, 0, "all");


        } catch (Exception ex) {
            errorHandling(ex);
            itemlist.addItem(new Row.Builder().setTitle(getCarContext().getResources().getString(R.string.error_notConnected)).build());
            templateBuilder.setSingleList(itemlist.build());
            isFinishedLoading = true;
            invalidate();
        }
    }

    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("AA Service", "OnStart triggered");
        getSwitch();
    }

    private void onClick(DevicesInfo selectedSwitch) {
        try {
            if (selectedSwitch.getType() != null && (selectedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || selectedSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE))) {
                if (selectedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP))
                    onButtonClick(selectedSwitch, true);
                else
                    onSwitchToggle(selectedSwitch);
            } else {
                switch (selectedSwitch.getSwitchTypeVal()) {
                    case DomoticzValues.Device.Type.Value.ON_OFF:
                    case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                    case DomoticzValues.Device.Type.Value.X10SIREN:
                    case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    case DomoticzValues.Device.Type.Value.DOORLOCK:
                    case DomoticzValues.Device.Type.Value.DIMMER:
                    case DomoticzValues.Device.Type.Value.BLINDS:
                    case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                        onSwitchToggle(selectedSwitch);
                        break;

                    case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                    case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                    case DomoticzValues.Device.Type.Value.DOORBELL:
                        onButtonClick(selectedSwitch, true);
                        break;

                    case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        onButtonClick(selectedSwitch, false);
                        break;

                    default:
                        throw new NullPointerException(
                                "Toggle event received from wear device for unsupported switch type: " + selectedSwitch.getSwitchTypeVal());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSwitchToggle(DevicesInfo toggledDevice) {
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        boolean checked = !toggledDevice.getStatusBoolean();
        if (toggledDevice.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                toggledDevice.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
                toggledDevice.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
            if (checked) jsonAction = DomoticzValues.Device.Switch.Action.OFF;
            else jsonAction = DomoticzValues.Device.Switch.Action.ON;
        } else {
            if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
            else jsonAction = DomoticzValues.Device.Switch.Action.OFF;
        }

        if (toggledDevice.getType().equals(DomoticzValues.Scene.Type.GROUP) || toggledDevice.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
            else jsonAction = DomoticzValues.Scene.Action.OFF;
        }

        StaticHelper.getDomoticz(getCarContext()).setAction(toggledDevice.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                if (result.contains("WRONG CODE")) {
                    CarToast.makeText(getCarContext(), "WRONG CODE", CarToast.LENGTH_LONG).show();

                } else {
                    //if not push button
                    if (toggledDevice.getSwitchTypeVal() != 9) {
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> getSwitch(), 2000);
                    }
                    CarToast.makeText(getCarContext(), toggledDevice.getName(), CarToast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                CarToast.makeText(getCarContext(), R.string.security_no_rights, CarToast.LENGTH_LONG).show();
            }
        });
    }

    public void onButtonClick(DevicesInfo toggledDevice, boolean checked) {
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
        else jsonAction = DomoticzValues.Device.Switch.Action.OFF;

        if (toggledDevice.getType().equals(DomoticzValues.Scene.Type.GROUP) || toggledDevice.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
            else jsonAction = DomoticzValues.Scene.Action.OFF;
        }

        StaticHelper.getDomoticz(getCarContext()).setAction(toggledDevice.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                if (result.contains("WRONG CODE")) {
                    CarToast.makeText(getCarContext(), "WRONG CODE", CarToast.LENGTH_LONG).show();

                } else {
                    //if not push button
                    if (toggledDevice.getSwitchTypeVal() != 9) {
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> getSwitch(), 2000);
                    }
                    CarToast.makeText(getCarContext(), toggledDevice.getName(), CarToast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                CarToast.makeText(getCarContext(), R.string.security_no_rights, CarToast.LENGTH_LONG).show();
            }
        });
    }
}