package nl.hnogames.domoticz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class UpdateActivity extends AppCompatActivity {

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
}