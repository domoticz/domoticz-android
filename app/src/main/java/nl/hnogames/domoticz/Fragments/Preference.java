package nl.hnogames.domoticz.Fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
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
        handleImportExportButtons();
    }

    private void handleImportExportButtons()
    {
        final File SettingsFile = new File(Environment.getExternalStorageDirectory(), "/Domoticz/DomoticzSettings.txt");
        final String sPath = SettingsFile.getPath().substring(0, SettingsFile.getPath().lastIndexOf("/"));
        new File(sPath).mkdirs();

        android.preference.Preference exportButton = (android.preference.Preference)findPreference("export_settings");
        exportButton.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Log.v("Export Settings", "Exporting settings to: "+SettingsFile.getPath());
                if(mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
                    Toast.makeText(getActivity(), "Settings Exported.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Failed to Export Settings.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        android.preference.Preference importButton = (android.preference.Preference)findPreference("import_settings");
        importButton.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Log.v("Import Settings", "Importing settings from: "+SettingsFile.getPath());
                mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile);

                if(mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
                    Toast.makeText(getActivity(), "Settings Imported, please restart Domoticz.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Failed to Import Settings.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
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

        final EditTextPreference version = (EditTextPreference) findPreference("version");
        final EditTextPreference domoticzversion = (EditTextPreference) findPreference("version_domoticz");
        version.setSummary(appVersion);

        Domoticz domoticz = new Domoticz(getActivity());
        domoticz.getVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(String version) {
                domoticzversion.setSummary(version);
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    private void setStartUpScreenDefaultValue() {

        int defaultValue = mSharedPrefs.getStartupScreenIndex();

        ListPreference startup_screen = (ListPreference) findPreference("startup_screen");
        startup_screen.setValueIndex(defaultValue);

    }
}