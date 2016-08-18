package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.UserVariablesAdapter;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.UserVariableInfo;
import nl.hnogames.domoticzapi.Interfaces.UserVariablesReceiver;

public class UserVariables extends DomoticzRecyclerFragment implements DomoticzFragmentListener {

    private ArrayList<UserVariableInfo> mUserVariableInfos;
    private UserVariablesAdapter adapter;
    private Context mContext;
    private String filter = "";
    private SlideInBottomAnimationAdapter alphaSlideIn;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

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
        new GetCachedDataTask().execute();
    }

    private void createListView() {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new UserVariablesAdapter(mContext, mDomoticz, mUserVariableInfos);
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
            } else {
                adapter.setData(mUserVariableInfos);
                adapter.notifyDataSetChanged();
                alphaSlideIn.notifyDataSetChanged();
            }
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

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<UserVariableInfo> cacheUserVariables = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheUserVariables = (ArrayList<UserVariableInfo>) SerializableManager.readSerializedObject(mContext, "UserVariables");
                    UserVariables.this.mUserVariableInfos = cacheUserVariables;
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheUserVariables != null)
                createListView();

            mDomoticz.getUserVariables(new UserVariablesReceiver() {
                @Override
                @DebugLog
                public void onReceiveUserVariables(ArrayList<UserVariableInfo> mVarInfos) {
                    UserVariables.this.mUserVariableInfos = mVarInfos;
                    SerializableManager.saveSerializable(mContext, mVarInfos, "UserVariables");
                    successHandling(mUserVariableInfos.toString(), false);
                    createListView();
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }
}