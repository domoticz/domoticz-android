
package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.LogAdapter;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticzapi.Containers.LogInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.LogsReceiver;

public class Logs extends DomoticzRecyclerFragment implements DomoticzFragmentListener {
    private LogAdapter adapter;
    private Context mContext;
    private String filter = "";
    private boolean itemDecorationAdded = false;
    private int LogLevel;

    @Override
    public void onConnectionFailed() {
        GetLogs();
    }

    @Override
    public void refreshFragment() {
        startSwipeRefreshing();
        processLogs();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        SetTitle(getString(R.string.title_logs));
        setSortFab(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        lySortLogs.setVisibility(View.VISIBLE);
        return view;
    }

    public void SetTitle(String title) {
        setActionbar(title);
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
        processLogs();
    }

    private void processLogs() {
        startSwipeRefreshing();
        GetLogs();
    }

    private void createListView(ArrayList<LogInfo> mLogInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new LogAdapter(mContext, StaticHelper.getDomoticz(mContext), mLogInfos);
                gridView.setAdapter(adapter);
            } else {
                adapter.setData(mLogInfos);
                adapter.notifyDataSetChanged();
            }
            if (!isTablet && !itemDecorationAdded) {
                gridView.addItemDecoration(new MarginItemDecoration(20));
                itemDecorationAdded = true;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(() -> processLogs());
        }
        super.showSpinner(false);
        this.Filter(filter);
    }

    @Override

    public void onPause() {
        super.onPause();
    }

    @Override

    public void errorHandling(Exception error) {
        if (error != null) {
            if (isAdded()) {
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                super.errorHandling(error);
            }
        }
    }

    public void GetLogs() {
        if (isAdded()) {
            LogLevel = DomoticzValues.Log.LOGLEVEL.ALL;
            if (getSort().equals(getString(R.string.filter_normal)))
                LogLevel = DomoticzValues.Log.LOGLEVEL.NORMAL;
            if (getSort().equals(getString(R.string.filter_status)))
                LogLevel = DomoticzValues.Log.LOGLEVEL.STATUS;
            if (getSort().equals(getString(R.string.filter_error)))
                LogLevel = DomoticzValues.Log.LOGLEVEL.ERROR;

            StaticHelper.getDomoticz(mContext).getLogs(new LogsReceiver() {
                @Override
                public void onReceiveLogs(ArrayList<LogInfo> mLogInfos) {
                    successHandling(mLogInfos.toString(), false);
                    SerializableManager.saveSerializable(mContext, mLogInfos, "Logs");
                    createListView(mLogInfos);
                }

                @Override
                public void onError(Exception error) {
                    if (LogLevel == DomoticzValues.Log.LOGLEVEL.ALL)
                        errorHandling(error);
                    else
                        createListView(new ArrayList<LogInfo>());
                }
            }, LogLevel);
        }
    }
}