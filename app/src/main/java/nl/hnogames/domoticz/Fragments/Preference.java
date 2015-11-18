package nl.hnogames.domoticz.Fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class Preference extends PreferenceFragment {

    SharedPrefUtil mSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mSharedPrefs = new SharedPrefUtil(getActivity());

        setStartUpScreenDefaultValue();
        setVersionInfo();

    }

    private void setVersionInfo() {

        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersion = "";
        if (pInfo != null) appVersion = pInfo.versionName;

        EditTextPreference version = (EditTextPreference) findPreference("version");
        version.setSummary(appVersion);
    }

    private void setStartUpScreenDefaultValue() {

        int defaultValue = mSharedPrefs.getStartupScreenIndex();

        ListPreference startup_screen = (ListPreference) findPreference("startup_screen");
        startup_screen.setValueIndex(defaultValue);

    }
}