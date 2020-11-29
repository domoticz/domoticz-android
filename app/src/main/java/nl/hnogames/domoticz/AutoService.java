package nl.hnogames.domoticz;

import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import androidx.annotation.NonNull;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.libraries.car.app.CarAppService;
import com.google.android.libraries.car.app.CarContext;
import com.google.android.libraries.car.app.CarToast;
import com.google.android.libraries.car.app.Screen;
import com.google.android.libraries.car.app.model.*;

import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import org.jetbrains.annotations.NotNull;
import static com.google.android.libraries.car.app.CarToast.LENGTH_LONG;
import static com.google.android.libraries.car.app.CarToast.LENGTH_SHORT;
import static com.google.android.libraries.car.app.model.CarColor.BLUE;
import static com.google.android.libraries.car.app.model.CarColor.GREEN;
import static com.google.android.libraries.car.app.model.CarColor.PRIMARY;
import static com.google.android.libraries.car.app.model.CarColor.RED;
import static com.google.android.libraries.car.app.model.CarColor.SECONDARY;
import static com.google.android.libraries.car.app.model.CarColor.YELLOW;

import java.util.ArrayList;
import java.util.List;

class AutoScreen extends Screen implements DefaultLifecycleObserver {
    ArrayList<DevicesInfo> supportedSwitches=new ArrayList<>();
    ListTemplate.Builder templateBuilder = ListTemplate.builder();
    boolean isFinishedLoading = false;
    protected AutoScreen(@NonNull @NotNull CarContext carContext) {
        super(carContext);
        getLifecycle().addObserver(this);
    }

    @NonNull
    @Override
    public Template getTemplate() {
        templateBuilder.setTitle("Domoticz");
        templateBuilder.setHeaderAction(Action.APP_ICON);
        if( isFinishedLoading){
            return templateBuilder.build();
        }
        else
        {
            return templateBuilder.setIsLoading(true).build();
        }
    }

    public void errorHandling(Exception error) {
        Log.d("android auto", "errorHandling: ",error);
    }

    public void getSwitch(){
        ItemList.Builder itemlist=  ItemList.builder();
        try {
            StaticHelper.getDomoticz(getCarContext()).getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                    SerializableManager.saveSerializable(getCarContext(), switches, "Switches");
                    int x=0;
                    final List<Integer> appSupportedSwitchesValues = StaticHelper.getDomoticz(getCarContext()).getSupportedSwitchesValues();
                    final List<String> appSupportedSwitchesNames = StaticHelper.getDomoticz(getCarContext()).getSupportedSwitchesNames();

                    for (DevicesInfo mDevicesInfo : switches) {
                        String name = mDevicesInfo.getName();
                        int switchTypeVal = mDevicesInfo.getSwitchTypeVal();
                        String switchType = mDevicesInfo.getSwitchType();

                        if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                                appSupportedSwitchesValues.contains(switchTypeVal) &&
                                appSupportedSwitchesNames.contains(switchType)) {
                            supportedSwitches.add(mDevicesInfo);
                            CharSequence charSequence;
                            String status = getCarContext().getResources().getString(R.string.status)+" ";
                            if(x<6){
                                if(!mDevicesInfo.getData().equals("Off")){
                                    charSequence= colorize(status+mDevicesInfo.getData(), BLUE,status.length() , 2);
                                }
                                else{
                                    charSequence= colorize(status+mDevicesInfo.getData(), RED, status.length(), 3);
                                }
                                itemlist.addItem(Row.builder().setTitle(mDevicesInfo.getName()).setOnClickListener(() -> onClick(mDevicesInfo)).addText(charSequence).build());
                                x++;
                            }
                        }
                    }
                    templateBuilder.setIsLoading(false);
                    templateBuilder.setSingleList(itemlist.build());
                    isFinishedLoading = true;
                    invalidate();
                }

                @Override

                public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                }

                @Override

                public void onError(Exception error) {
                    Log.d("AA Service", "onError: ",error);
                    itemlist.addItem(Row.builder().setTitle(getCarContext().getResources().getString(R.string.error_notConnected)).build());
                    templateBuilder.setIsLoading(false);
                    templateBuilder.setSingleList(itemlist.build());
                    isFinishedLoading = true;
                    invalidate();
                }
            }, 0, "light");


        } catch (Exception ex) {
            errorHandling(ex);
            itemlist.addItem(Row.builder().setTitle(getCarContext().getResources().getString(R.string.error_notConnected)).build());
            templateBuilder.setSingleList(itemlist.build());
            isFinishedLoading = true;
            invalidate();
        }
    }
    public void onStart(@NonNull LifecycleOwner owner) {
            getSwitch();
    }

    private void onClick(DevicesInfo devices) {
        toggleButton(devices,devices.getData().equals("Off"));
    }

    private void toggleButton(DevicesInfo clickedSwitch, boolean checked) {

        int idx = clickedSwitch.getIdx();
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
        else jsonAction = DomoticzValues.Device.Switch.Action.OFF;

        StaticHelper.getDomoticz(getCarContext()).setAction(idx, jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                if (result.contains("WRONG CODE")) {
                    CarToast.makeText(getCarContext(),"WRONG CODE", LENGTH_LONG).show();

                }
                else {
                    //if not push button
                    if(clickedSwitch.getSwitchTypeVal()!=9){
                        //update switch state
                        getSwitch();
                    }
                    CarToast.makeText(getCarContext(),clickedSwitch.getName(), LENGTH_SHORT).show();
                }
            }

            @Override

            public void onError(Exception error) {
                CarToast.makeText(getCarContext(),R.string.security_no_rights, LENGTH_LONG).show();
            }
        });
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
}

public class AutoService extends CarAppService {
    @Override
    @NonNull
    public Screen onCreateScreen(@NonNull Intent intent) {
        return new AutoScreen(getCarContext());
        }
}
