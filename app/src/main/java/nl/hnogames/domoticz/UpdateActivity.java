package nl.hnogames.domoticz;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.UpdateReceiver;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class UpdateActivity extends AppCompatActivity {

    private String TAG = UpdateActivity.class.getSimpleName();

    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mSharedPrefs = new SharedPrefUtil(this);
        domoticz = new Domoticz(this);
    }

    private void initViews() {

        TextView currentServerVersionValue =
                (TextView) findViewById(R.id.currentServerVersion_value);

        currentServerVersionValue.setText(mSharedPrefs.getUpdateVersionAvailable());

    }

    private void checkDomoticzServerUpdate() {

        // Get latest Domoticz version
        domoticz.getUpdate(new UpdateReceiver() {
            @Override
            public void onReceiveUpdate(String version, boolean haveUpdate) {
                // Write update version to shared preferences
                mSharedPrefs.setUpdateVersionAvailable(version);
                mSharedPrefs.setServerUpdateAvailable(haveUpdate);
            }

            @Override
            public void onError(Exception error) {
                String message = String.format(
                        getString(R.string.error_couldNotCheckForUpdates),
                        domoticz.getErrorMessage(error));
                showSimpleSnackbar(message);
                mSharedPrefs.setUpdateVersionAvailable("");
            }
        });
    }

    private String getServerVersion() {
        domoticz.getVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(String serverVersion) {
                if (!UsefulBits.isEmpty(serverVersion)) {
                    String[] version
                            = serverVersion.split("\\.");

                    // Update version is only revision number
                    String updateVersion =
                            version[0] + "." + mSharedPrefs.getUpdateVersionAvailable();
                }
            }

            @Override
            public void onError(Exception error) {
                String message = String.format(
                        getString(R.string.error_couldNotCheckForUpdates),
                        domoticz.getErrorMessage(error));
                showSimpleSnackbar(message);
            }
        });
        return mSharedPrefs.getUpdateVersionAvailable();
    }

    private void showSimpleSnackbar(String message) {
        CoordinatorLayout fragmentCoordinatorLayout =
                (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        Snackbar.make(fragmentCoordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}