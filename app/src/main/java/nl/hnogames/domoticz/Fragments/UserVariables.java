package nl.hnogames.domoticz.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.LogAdapter;
import nl.hnogames.domoticz.Adapters.UserVariablesAdapter;
import nl.hnogames.domoticz.Containers.UserVariableInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.LogsReceiver;
import nl.hnogames.domoticz.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class UserVariables extends DomoticzFragment implements DomoticzFragmentListener {

    private Domoticz mDomoticz;
    private ArrayList<UserVariableInfo> mUserVariableInfos;

    private ListView listView;
    private UserVariablesAdapter adapter;
    private ProgressDialog progressDialog;
    private Activity mActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUserVariables();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        getActionBar().setTitle(R.string.title_vars);
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
        processUserVariables();
    }

    private void processUserVariables() {
        mDomoticz.getUserVariables(new UserVariablesReceiver() {
            @Override
            public void onReceiveUserVariabeles(ArrayList<UserVariableInfo> mVarInfos) {
                UserVariables.this.mUserVariableInfos = mVarInfos;
                successHandling(mUserVariableInfos.toString(), false);

                adapter = new UserVariablesAdapter(mActivity, mUserVariableInfos);

                createListView();
                hideProgressDialog();
            }

            @Override
            public void onError(Exception error)  {
                errorHandling(error);
            }
        });
    }

    private void createListView() {
        if(getView()!=null) {
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);

            listView = (ListView) getView().findViewById(R.id.listView);
            listView.setAdapter(adapter);

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    processUserVariables();
                }
            });
        }
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