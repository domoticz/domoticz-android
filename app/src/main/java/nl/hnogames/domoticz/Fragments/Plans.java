package nl.hnogames.domoticz.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Adapters.PlansAdapter;
import nl.hnogames.domoticz.Containers.PlanInfo;
import nl.hnogames.domoticz.Containers.SceneInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.PlansReceiver;
import nl.hnogames.domoticz.PlanActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.SceneInfoDialog;
import nl.hnogames.domoticz.app.DomoticzCardFragment;

@SuppressWarnings("unused")
public class Plans extends DomoticzCardFragment implements DomoticzFragmentListener {

    private static final String TAG = Plans.class.getSimpleName();

    private ProgressDialog progressDialog;
    private Activity mActivity;
    private Domoticz mDomoticz;
    private RecyclerView mRecyclerView;
    private PlansAdapter mAdapter;
    private ArrayList<PlanInfo> mPlans;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void refreshFragment() {
        processPlans();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void processPlans() {
        showProgressDialog();

        mDomoticz = new Domoticz(mActivity);
        mDomoticz.getPlans(new PlansReceiver() {

            @Override
            public void OnReceivePlans(ArrayList<PlanInfo> plans) {
                successHandling(plans.toString(), false);

                Plans.this.mPlans = plans;

                Collections.sort(plans, new Comparator<PlanInfo>() {
                    @Override
                    public int compare(PlanInfo left, PlanInfo right) {
                        return left.getOrder() - right.getOrder();
                    }
                });

                mAdapter = new PlansAdapter(plans, getActivity());
                mAdapter.setOnItemClickListener(new PlansAdapter.onClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        //Toast.makeText(getActivity(), "Clicked " + mPlans.get(position).getName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), PlanActivity.class);
                        intent.putExtra("PLANNAME", mPlans.get(position).getName());
                        intent.putExtra("PLANID", mPlans.get(position).getIdx());
                        startActivity(intent);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
                hideProgressDialog();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private void showInfoDialog(final SceneInfo mSceneInfo) {

        SceneInfoDialog infoDialog = new SceneInfoDialog(
                getActivity(),
                mSceneInfo,
                R.layout.dialog_scene_info);
        infoDialog.setIdx(String.valueOf(mSceneInfo.getIdx()));
        infoDialog.setLastUpdate(mSceneInfo.getLastUpdate());
        infoDialog.setIsFavorite(mSceneInfo.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener(new SceneInfoDialog.DismissListener() {

            @Override
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                //TODO
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        getActionBar().setTitle(R.string.title_plans);
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

    public void errorHandling(Exception error) {
        super.errorHandling(error);
        hideProgressDialog();
    }

    public ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onConnectionOk() {
        mDomoticz = new Domoticz(getActivity());
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        processPlans();
    }
}