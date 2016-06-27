package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.UserVariablesAdapter;
import nl.hnogames.domoticz.Containers.UserVariableInfo;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;

public class UserVariables extends DomoticzRecyclerFragment implements DomoticzFragmentListener {

    private ArrayList<UserVariableInfo> mUserVariableInfos;
    private UserVariablesAdapter adapter;
    private Context mContext;
    private String filter = "";

    @Override
    public void onConnectionFailed() {}

    @Override
    @DebugLog
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUserVariables();
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_vars);
    }

    @Override
    @DebugLog
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
    @DebugLog
    public void onConnectionOk() {
        super.showSpinner(true);
        processUserVariables();
    }

    private void processUserVariables() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        mDomoticz.getUserVariables(new UserVariablesReceiver() {
            @Override
            @DebugLog
            public void onReceiveUserVariables(ArrayList<UserVariableInfo> mVarInfos) {
                UserVariables.this.mUserVariableInfos = mVarInfos;
                successHandling(mUserVariableInfos.toString(), false);
                adapter = new UserVariablesAdapter(mContext, mDomoticz, mUserVariableInfos);
                createListView();
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private void createListView() {
        if (getView() != null) {
            SlideInBottomAnimationAdapter alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
            gridView.setAdapter(alphaSlideIn);
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                @DebugLog
                public void onRefresh() {
                    processUserVariables();
                }
            });
            super.showSpinner(false);
            this.Filter(filter);
        }
    }

    @Override
    @DebugLog
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }
}