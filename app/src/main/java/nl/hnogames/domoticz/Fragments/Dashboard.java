package nl.hnogames.domoticz.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Adapters.DevicesAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.DeviceInfoDialog;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Dashboard extends DomoticzFragment implements DomoticzFragmentListener,
        switchesClickListener {

    private static final String TAG = Switches.class.getSimpleName();
    private ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Domoticz mDomoticz;
    private Context mActivity;
    private int currentSwitch = 1;
    private DevicesAdapter adapter;
    private ListView listView;
    private ArrayList<DevicesInfo> extendedStatusSwitches;

    private int planID = 0;
    private String planName = "";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        processDashboard();
    }

    public void selectedPlan(int plan, String name) {
        planID = plan;
        planName = name;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void Filter(String text) {
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onConnectionOk() {
        showProgressDialog();
        getActionBar().setTitle(R.string.title_dashboard);
        if (planName != null && planName.length() > 0)
            getActionBar().setTitle(planName + "");

        processDashboard();
    }

    private void processDashboard() {
        mDomoticz = new Domoticz(mActivity);
        mDomoticz.getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                processDevices(switches);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        }, planID);
    }

    private void processDevices(ArrayList<DevicesInfo> devicesInfos) {
        extendedStatusSwitches = new ArrayList<>();
        final int totalNumberOfSwitches = devicesInfos.size();

        for (DevicesInfo switchInfo : devicesInfos) {
            successHandling(switchInfo.toString(), false);
            int idx = switchInfo.getIdx();

            if (this.planID <= 0) {
                if (switchInfo.getFavoriteBoolean()) {//only favorites
                    extendedStatusSwitches.add(switchInfo);     // Add to array
                }
            } else {
                extendedStatusSwitches.add(switchInfo);
            }
        }
        createListView(extendedStatusSwitches);
    }

    // add dynamic list view
    // https://github.com/nhaarman/ListViewAnimations
    private void createListView(ArrayList<DevicesInfo> switches) {

        if (switches == null)
            return;

        try {
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
            supportedSwitches = new ArrayList<>();
            final List<Integer> appSupportedSwitchesValues = mDomoticz.getSupportedSwitchesValues();
            final List<String> appSupportedSwitchesNames = mDomoticz.getSupportedSwitchesNames();

            for (DevicesInfo mExtendedStatusInfo : switches) {
                String name = mExtendedStatusInfo.getName();
                int switchTypeVal = mExtendedStatusInfo.getSwitchTypeVal();
                String switchType = mExtendedStatusInfo.getSwitchType();

                if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                        (appSupportedSwitchesValues.contains(switchTypeVal) && appSupportedSwitchesNames.contains(switchType)) ||
                        (switchType == null || switchType.equals(null) || switchType.length() <= 0))//utilities
                {
                    supportedSwitches.add(mExtendedStatusInfo);
                }
            }

            final switchesClickListener listener = this;
            adapter = new DevicesAdapter(mActivity, supportedSwitches, listener);
            listView = (ListView) getView().findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                    showInfoDialog(supportedSwitches.get(index));
                    return true;
                }
            });

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    processDashboard();
                }
            });
        } catch (Exception ex) {
        }

        hideProgressDialog();
    }


    private void showInfoDialog(final DevicesInfo mSwitch) {
        DeviceInfoDialog infoDialog = new DeviceInfoDialog(
                getActivity(),
                mSwitch,
                R.layout.dialog_switch_info);
        infoDialog.setIdx(String.valueOf(mSwitch.getIdx()));
        infoDialog.setLastUpdate(mSwitch.getLastUpdate());
        infoDialog.setSignalLevel(String.valueOf(mSwitch.getSignalLevel()));
        infoDialog.setBatteryLevel(String.valueOf(mSwitch.getBatteryLevel()));
        infoDialog.setIsFavorite(mSwitch.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener(new DeviceInfoDialog.DismissListener() {
            @Override
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged) {
                    changeFavorite(mSwitch, isFavorite);
                    processDashboard();
                }
            }
        });
    }

    private void changeFavorite(final DevicesInfo mSwitch, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mSwitch.getIdx() + " favorite to " + isFavorite);

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = Domoticz.Device.Favorite.ON;
        else jsonAction = Domoticz.Device.Favorite.OFF;

        mDomoticz.setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSwitch.setFavoriteBoolean(isFavorite);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });

    }

    @Override
    public void onSwitchClick(int idx, boolean checked) {
        addDebugText("onSwitchClick");
        addDebugText("Set idx " + idx + " to " + checked);

        DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch != null) {
            int jsonAction;
            int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
            if (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                    clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.OFF;
                else jsonAction = Domoticz.Device.Switch.Action.ON;
            } else {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
                else jsonAction = Domoticz.Device.Switch.Action.OFF;
            }
            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }

    private DevicesInfo getDevice(int idx) {

        DevicesInfo clickedSwitch = null;
        for (DevicesInfo mExtendedStatusInfo : extendedStatusSwitches) {
            if (mExtendedStatusInfo.getIdx() == idx) {
                clickedSwitch = mExtendedStatusInfo;
            }
        }
        return clickedSwitch;
    }

    @Override
    public void onButtonClick(int idx, boolean checked) {
        addDebugText("onButtonClick");
        addDebugText("Set idx " + idx + " to ON");

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction = Domoticz.Device.Switch.Action.OFF;

        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onLogButtonClick(int idx) {
    }

    @Override
    public void onTimerButtonClick(int idx) {
    }

    @Override
    public void onBlindClick(int idx, int jsonAction) {
        addDebugText("onBlindClick");
        addDebugText("Set idx " + idx + " to " + String.valueOf(jsonAction));

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onDimmerChange(int idx, int value) {
        addDebugText("onDimmerChange");

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        int jsonAction = Domoticz.Device.Dimmer.Action.DIM_LEVEL;

        mDomoticz.setAction(idx, jsonUrl, jsonAction, value, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }


    /**
     * Notifies the list view adapter the data has changed and refreshes the list view
     */
    private void notifyDataSetChanged() {
        listView.setAdapter(adapter);
    }

    /**
     * Initializes the progress dialog
     */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage(getString(R.string.msg_please_wait));
        progressDialog.setCancelable(false);
    }

    /**
     * Shows the progress dialog if isn't already showing
     */
    private void showProgressDialog() {
        if (progressDialog == null) initProgressDialog();
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**
     * Hides the progress dialog if it is showing
     */
    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void errorHandling(Exception error) {
        super.errorHandling(error);
        hideProgressDialog();
    }
}