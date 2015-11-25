package nl.hnogames.domoticz.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.LogAdapter;
import nl.hnogames.domoticz.Containers.LogInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.LogsReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Logs extends DomoticzFragment implements DomoticzFragmentListener {

    private Domoticz mDomoticz;
    private ArrayList<LogInfo> mLogInfos;

    private long thermostatSetPointValue;

    private ListView listView;
    private LogAdapter adapter;
    private ProgressDialog progressDialog;
    private Activity mActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processLogs();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        getActionBar().setTitle(R.string.title_logs);
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

        mDomoticz = new Domoticz(mActivity);
        processLogs();
    }

    private void processLogs() {
        mDomoticz.getLogs(new LogsReceiver() {
            @Override
            public void onReceiveLogs(ArrayList<LogInfo> mLogInfos) {
                successHandling(mLogInfos.toString(), false);

                Logs.this.mLogInfos = mLogInfos;
                adapter = new LogAdapter(mActivity, mLogInfos);

                createListView();
                hideProgressDialog();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private void createListView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);

        listView = (ListView) getView().findViewById(R.id.listView);
        listView.setAdapter(adapter);

        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processLogs();
            }
        });
    }


    /**
     * Notifies the list view adapter the data has changed and refreshes the list view
     */
    private void notifyDataSetChanged() {
        addDebugText("notifyDataSetChanged");
        // adapter.notifyDataSetChanged();
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