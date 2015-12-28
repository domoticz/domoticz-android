package nl.hnogames.domoticz.Welcome;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class WelcomePage4 extends Fragment {

    private LinearLayout please_wait_layout;
    private TextView result;
    private LinearLayout result_layout;
    private String tempText = "";

    public static WelcomePage4 newInstance() {
        return new WelcomePage4();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome4, container, false);

        please_wait_layout = (LinearLayout) v.findViewById(R.id.layout_please_wait);
        result_layout = (LinearLayout) v.findViewById(R.id.layout_result);
        result = (TextView) v.findViewById(R.id.result);

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            resetLayout();
            checkConnectionData();
            disableFinishButton();
        }
    }

    private void checkConnectionData() {
        final Domoticz mDomoticz = new Domoticz(getActivity());

        if (!mDomoticz.isConnectionDataComplete()) {
            setResultText(getString(R.string.welcome_msg_connectionDataIncomplete) + "\n\n"
                    + getString(R.string.welcome_msg_correctOnPreviousPage));
        } else if (!mDomoticz.isUrlValid()) {
            setResultText(getString(R.string.welcome_msg_connectionDataInvalid) + "\n\n"
                    + getString(R.string.welcome_msg_correctOnPreviousPage));
        } else {
            mDomoticz.getVersion(new VersionReceiver() {
                @Override
                public void onReceiveVersion(String version) {
                    tempText = getString(R.string.welcome_msg_serverVersion) + ": " + version;

                    mDomoticz.getDevices(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                            tempText = tempText + "\n";
                            String formatted = String.format(getString(R.string.welcome_msg_numberOfDevices), mDevicesInfo.size());
                            tempText = tempText + formatted;
                            setSuccessText(tempText);
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                        }

                        @Override
                        public void onError(Exception error) {
                            setErrorText(mDomoticz.getErrorMessage(error));
                        }
                    }, 0, null);
                }

                @Override
                public void onError(Exception error) {
                    setErrorText(mDomoticz.getErrorMessage(error));
                }
            });
        }
    }

    private void setErrorText(String errorMessage) {
        tempText = tempText + "\n";
        tempText = tempText + errorMessage;
        tempText = tempText + "\n\n";
        tempText = tempText + getString(R.string.welcome_msg_correctOnPreviousPage);
        disableFinishButton();
        setResultText(tempText);

        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
        mSharedPrefs.setWelcomeWizardSuccess(false);
        tempText = "";
    }

    private void setSuccessText(String message) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
        mSharedPrefs.setWelcomeWizardSuccess(true);
        enableFinishButton();
        setResultText(message);
        tempText = "";
    }

    private void setResultText(String text) {
        please_wait_layout.setVisibility(View.GONE);
        result_layout.setVisibility(View.VISIBLE);
        result.setText(text);
    }

    private void resetLayout() {
        please_wait_layout.setVisibility(View.VISIBLE);
        result_layout.setVisibility(View.GONE);
        result.setText("");
    }

    private void disableFinishButton() {
        WelcomeViewActivity activity = (WelcomeViewActivity) getActivity();
        activity.disableFinishButton(true);
    }

    private void enableFinishButton() {
        WelcomeViewActivity activity = (WelcomeViewActivity) getActivity();
        activity.disableFinishButton(false);
    }
}