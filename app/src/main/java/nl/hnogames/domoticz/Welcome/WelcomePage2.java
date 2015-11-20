package nl.hnogames.domoticz.Welcome;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class WelcomePage2 extends Fragment {

    public static final WelcomePage2 newInstance() {
        return new WelcomePage2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome2, container, false);

        final File SettingsFile = new File(Environment.getExternalStorageDirectory(), "/Domoticz/DomoticzSettings.txt");
        Button importButton = (Button) v.findViewById(R.id.import_settings);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //import settings
                SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
                if (mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile)) {
                    Toast.makeText(getActivity(), "Settings Imported, we need to restart Domoticz!!", Toast.LENGTH_LONG).show();
                    System.exit(99);
                }
                else
                    Toast.makeText(getActivity(), "Failed to Import Settings.", Toast.LENGTH_SHORT).show();
            }
        });
        if(!SettingsFile.exists()) {
            importButton.setVisibility(View.GONE);
        }
        return v;
    }
}