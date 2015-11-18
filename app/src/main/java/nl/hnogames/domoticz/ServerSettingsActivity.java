package nl.hnogames.domoticz;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import nl.hnogames.domoticz.Welcome.WelcomePage3;

public class ServerSettingsActivity extends ActionBarActivity {

    private static final int WELCOME_WIZARD = 1;
    private static final int SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment serverSettings = WelcomePage3.newInstance(SETTINGS);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, serverSettings)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}