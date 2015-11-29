package nl.hnogames.domoticz.Preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.R;

@TargetApi(11)
public class WearMultiSelectListPreference extends MultiSelectListPreference {
    private static final String TAG = WearMultiSelectListPreference.class.getName();

    private boolean selectAllValuesByDefault;

    private Context mContext;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Domoticz mDomoticz;
    private ArrayList<ExtendedStatusInfo> extendedStatusSwitches;
    private int currentSwitch = 1;

    public WearMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomListPreference);
        selectAllValuesByDefault = typedArray.getBoolean(R.styleable.CustomListPreference_selectAllValuesByDefault, false);
        typedArray.recycle();
        mContext = context;
        mDomoticz = new Domoticz(context);
        initSwitches();
    }

    private void initSwitches() {
        extendedStatusSwitches = new ArrayList<>();
        currentSwitch = 1;
        mDomoticz.getSwitches(new SwitchesReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                for (SwitchInfo switchInfo : switches) {
                    int idx = switchInfo.getIdx();
                    final int totalNumberOfSwitches = switches.size();

                    mDomoticz.getStatus(idx, new StatusReceiver() {
                        @Override
                        public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {
                            extendedStatusSwitches.add(extendedStatusInfo);
                            if (currentSwitch == totalNumberOfSwitches) {
                                {
                                    final List<Integer> appSupportedSwitchesValues = mDomoticz.getWearSupportedSwitchesValues();
                                    final List<String> appSupportedSwitchesNames = mDomoticz.getWearSupportedSwitchesNames();
                                    ArrayList<ExtendedStatusInfo> supportedSwitches = new ArrayList<>();

                                    for (ExtendedStatusInfo mExtendedStatusInfo : extendedStatusSwitches) {
                                        String name = mExtendedStatusInfo.getName();
                                        int switchTypeVal = mExtendedStatusInfo.getSwitchTypeVal();
                                        String switchType = mExtendedStatusInfo.getSwitchType();
                                        if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                                                appSupportedSwitchesValues.contains(switchTypeVal) &&
                                                appSupportedSwitchesNames.contains(switchType)) {
                                            supportedSwitches.add(mExtendedStatusInfo);
                                        }
                                    }

                                    if (supportedSwitches.size() > 0)
                                        processSwitches(supportedSwitches);
                                }
                            } else currentSwitch++;                               // Not there yet
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e(TAG, error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }


    private void processSwitches(ArrayList<ExtendedStatusInfo> switches) {
        mEntries = getEntries();
        mEntryValues = getEntryValues();

        if (switches != null) {
            List<String> entries = new ArrayList<>();
            List<String> entryValues = new ArrayList<>();

            for (ExtendedStatusInfo s : switches) {
                entryValues.add(s.getIdx() + "");
                entries.add(s.getIdx() + " - " + s.getName());
            }

            if (entries != null && entryValues != null && !entries.isEmpty() && !entryValues.isEmpty()) {
                CharSequence[] dynamicEntries = entries.toArray(new CharSequence[entries.size()]);
                CharSequence[] dynamicEntryValues = entryValues.toArray(new CharSequence[entryValues.size()]);

                //if either of the android attributes for specifying the entries and their values have been left empty, then ignore both and use only the dynamic providers
                if (mEntries == null || mEntryValues == null) {
                    mEntries = dynamicEntries;
                    mEntryValues = dynamicEntryValues;
                } else {
                    CharSequence[] fullEntriesList = new CharSequence[mEntries.length + dynamicEntries.length];
                    CharSequence[] fullEntryValuesList = new CharSequence[mEntryValues.length + dynamicEntryValues.length];

                    int i = 0, j = 0;
                    for (i = 0; i <= mEntries.length - 1; i++) {
                        fullEntriesList[i] = mEntries[i];
                        fullEntryValuesList[i] = mEntryValues[i];
                    }

                    for (i = mEntries.length, j = 0; j <= dynamicEntries.length - 1; i++, j++) {
                        fullEntriesList[i] = dynamicEntries[j];
                        fullEntryValuesList[i] = dynamicEntryValues[j];
                    }
                    //replace the entries and entryValues arrays with the new lists
                    mEntries = fullEntriesList;
                    mEntryValues = fullEntryValuesList;

                    setEntries(mEntries);
                    setEntryValues(mEntryValues);
                }
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (!restoreValue && selectAllValuesByDefault && mEntryValues != null) {
            final int valueCount = mEntryValues.length;
            final Set<String> result = new HashSet<String>();

            for (int i = 0; i < valueCount; i++) {
                result.add(mEntryValues[i].toString());
            }
            setValues(result);
            return;
        }
        super.onSetInitialValue(restoreValue, defaultValue);
    }
}