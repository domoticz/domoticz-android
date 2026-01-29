
package nl.hnogames.domoticz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.ftinc.scoop.Scoop;

import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.preference.PreferenceFragment;
import nl.hnogames.domoticz.ui.ScoopSettingsActivity;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class SettingsActivity extends AppCompatPermissionsActivity {
    private final int THEME_CHANGED = 555;
    private final int EXPORT_SETTINGS = 666;
    private final int IMPORT_SETTINGS = 777;
    private Toolbar toolbar;
    private PreferenceFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(this);

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishWithResult();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
    }

    public void reloadSettings() {
        recreate();
    }

    public void openThemePicker() {
        startActivityForResult(ScoopSettingsActivity.createIntent(this, getString(R.string.config_theme)), THEME_CHANGED);
    }

    public void exportSettings() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("plain/txt");
        intent.putExtra(Intent.EXTRA_TITLE, "settings.txt");
        startActivityForResult(intent, EXPORT_SETTINGS);
    }

    public void importSettings() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "settings.txt");
        startActivityForResult(intent, IMPORT_SETTINGS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == THEME_CHANGED) {
            // Restart the app from MainActivity when theme changes
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        if (requestCode == EXPORT_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        if ((new SharedPrefUtil(this)).saveSharedPreferencesToFile(data.getData()))
                            fragment.showSnackbar(getString(R.string.settings_exported));
                        else
                            fragment.showSnackbar(getString(R.string.settings_export_failed));
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
        if (requestCode == IMPORT_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        if ((new SharedPrefUtil(this)).loadSharedPreferencesFromFile(data.getData()))
                            fragment.showSnackbar(getString(R.string.settings_imported));
                        else
                            fragment.showSnackbar(getString(R.string.settings_import_failed));
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private void finishWithResult() {
        Bundle conData = new Bundle();
        conData.putBoolean("RESULT", true);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}