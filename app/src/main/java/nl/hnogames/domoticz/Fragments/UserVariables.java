package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.UserVariablesAdapter;
import nl.hnogames.domoticz.Containers.UserVariableInfo;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class UserVariables extends DomoticzFragment implements DomoticzFragmentListener {

    private ArrayList<UserVariableInfo> mUserVariableInfos;
    private UserVariablesAdapter adapter;
    private Context mContext;
    private String filter = "";

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUserVariables();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        getActionBar().setTitle(R.string.title_vars);
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
        processUserVariables();
    }

    private void processUserVariables() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        mDomoticz.getUserVariables(new UserVariablesReceiver() {
            @Override
            public void onReceiveUserVariables(ArrayList<UserVariableInfo> mVarInfos) {
                UserVariables.this.mUserVariableInfos = mVarInfos;
                successHandling(mUserVariableInfos.toString(), false);
                adapter = new UserVariablesAdapter(mContext, mUserVariableInfos);
                createListView();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private void createListView() {
        if (getView() != null) {
            SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
            animationAdapter.setAbsListView(listView);
            listView.setAdapter(animationAdapter);


            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    processUserVariables();
                }
            });
            super.showSpinner(false);
            this.Filter(filter);

        }
    }

    @Override
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }
}