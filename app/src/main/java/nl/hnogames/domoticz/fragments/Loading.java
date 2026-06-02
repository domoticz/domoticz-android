
package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.os.Bundle;

import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;

public class Loading extends DomoticzRecyclerFragment implements DomoticzFragmentListener {

    @Override
    public void onConnectionFailed() {
    }

    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override

    public void onConnectionOk() {
        super.showSpinner(true);
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
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
}