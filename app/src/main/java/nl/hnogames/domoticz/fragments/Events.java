
package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.EventsAdapter;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.EventInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.EventReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class Events extends DomoticzRecyclerFragment implements DomoticzFragmentListener {

    private EventsAdapter adapter;
    private Context mContext;
    private String filter = "";
    private boolean itemDecorationAdded = false;

    @Override

    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processEvents();
    }

    @Override
    public void onConnectionFailed() {
        GetEvents();
    }

    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        setActionbar(getString(R.string.title_events));
        setSortFab(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override

    public void Filter(String text) {
        filter = text;
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override

    public void onConnectionOk() {
        super.showSpinner(true);
        processEvents();
    }

    private void processEvents() {
        try {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            GetEvents();
        } catch (Exception ex) {
        }
    }

    private void createListView(ArrayList<EventInfo> mEventInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new EventsAdapter(mContext, StaticHelper.getDomoticz(mContext), mEventInfos, (idx, action) -> {
                    UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
                    if (user != null && user.getRights() <= 1) {
                        UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                        refreshFragment();
                        return;
                    }

                    int jsonAction = action ? DomoticzValues.Event.Action.ON : DomoticzValues.Event.Action.OFF;
                    int jsonUrl = DomoticzValues.Json.Url.Set.EVENTS_UPDATE_STATUS;
                    StaticHelper.getDomoticz(mContext).setAction(idx, jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
                        @Override
                        public void onReceiveResult(String result) {
                            successHandling(result, false);
                        }

                        @Override
                        public void onError(Exception error) {
                            errorHandling(error);
                        }
                    });
                });
                gridView.setAdapter(adapter);
                if (!isTablet && !itemDecorationAdded) {
                    gridView.addItemDecoration(new MarginItemDecoration(20));
                    itemDecorationAdded = true;
                }
            } else {
                adapter.setData(mEventInfos);
                adapter.notifyDataSetChanged();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(() -> processEvents());
            super.showSpinner(false);
            this.Filter(filter);
        }
    }

    @Override

    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);

                super.errorHandling(error);
            }
        }
    }

    public void GetEvents() {
        StaticHelper.getDomoticz(mContext).getEvents(new EventReceiver() {
            @Override

            public void onReceiveEvents(final ArrayList<EventInfo> mEventInfos) {
                successHandling(mEventInfos.toString(), false);
                SerializableManager.saveSerializable(mContext, mEventInfos, "Events");
                createListView(mEventInfos);
            }

            @Override

            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }
}