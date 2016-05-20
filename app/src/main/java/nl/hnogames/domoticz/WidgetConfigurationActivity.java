package nl.hnogames.domoticz;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.WidgetsAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Service.WidgetProviderLarge;
import nl.hnogames.domoticz.UI.PasswordDialog;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final int iWelcomeResultCode = 885;
    int mAppWidgetId;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private WidgetsAdapter adapter;
    private SearchView searchViewAction;

    private final int iVoiceAction = -55;
    private final int iQRCodeAction = -66;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configuration);
        setResult(RESULT_CANCELED);

        if (BuildConfig.LITE_VERSION) {
            Toast.makeText(this, getString(R.string.wizard_widgets) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
            this.finish();
        }

        domoticz = new Domoticz(this, null);
        this.setTitle(getString(R.string.pick_device_title));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        //1) Is domoticz connected?
        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            //2) Show list of switches to choose from
            initListViews();
        }
    }

    /* Called when the second activity's finished */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (!res.getBoolean("RESULT", false))
                        this.finish();
                    else {
                        initListViews();
                    }
                    break;
            }
        }
    }

    public void initListViews() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            Log.i(TAG, "Showing switches for widget");
            domoticz.getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(final ArrayList<DevicesInfo> mDevicesInfo) {
                    if (mSharedPrefs.isSpeechEnabled()) {
                        DevicesInfo oVoiceRow = new DevicesInfo();
                        oVoiceRow.setIdx(iVoiceAction);
                        oVoiceRow.setName(WidgetConfigurationActivity.this.getString(R.string.action_speech));
                        mDevicesInfo.add(0, oVoiceRow);
                    }
                    if (mSharedPrefs.isQRCodeEnabled()) {
                        DevicesInfo oQRCodeRow = new DevicesInfo();
                        oQRCodeRow.setIdx(iQRCodeAction);
                        oQRCodeRow.setName(WidgetConfigurationActivity.this.getString(R.string.action_qrcode_scan));
                        mDevicesInfo.add(0, oQRCodeRow);
                    }

                    ListView listView = (ListView) findViewById(R.id.list);
                    adapter = new WidgetsAdapter(WidgetConfigurationActivity.this, domoticz, mDevicesInfo);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final DevicesInfo mDeviceInfo = (DevicesInfo) adapter.getItem(position);
                            if (mDeviceInfo.isProtected()) {
                                PasswordDialog passwordDialog = new PasswordDialog(
                                        WidgetConfigurationActivity.this, domoticz);
                                passwordDialog.show();
                                passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                    @Override
                                    public void onDismiss(String password) {
                                        showAppWidget(mDeviceInfo, password);
                                    }
                                });
                            } else {
                                showAppWidget(mDeviceInfo, null);
                            }
                        }
                    });

                    listView.setAdapter(adapter);
                }

                @Override
                public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(WidgetConfigurationActivity.this, R.string.failed_to_get_switches, Toast.LENGTH_SHORT).show();
                    WidgetConfigurationActivity.this.finish();
                }
            }, 0, null);
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void showAppWidget(DevicesInfo mSelectedSwitch, String password) {
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int idx = mSelectedSwitch.getIdx();

        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);

            if (UsefulBits.isEmpty(mSelectedSwitch.getType())) {
                mSharedPrefs.setWidgetIDX(mAppWidgetId, idx, false, password);
            } else {
                if (mSelectedSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || mSelectedSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
                    mSharedPrefs.setWidgetIDX(mAppWidgetId, idx, true, password);
                } else {
                    mSharedPrefs.setWidgetIDX(mAppWidgetId, idx, false, password);
                }
            }

            Intent startService = new Intent(WidgetConfigurationActivity.this,
                    WidgetProviderLarge.UpdateWidgetService.class);
            startService.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
            startService.setAction("FROM CONFIGURATION ACTIVITY");
            startService(startService);
            setResult(RESULT_OK, startService);

            finish();
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            Log.i(TAG, "I am invalid");
            finish();
        }
    }

    public void Filter(String text) {
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchViewAction = (SearchView) MenuItemCompat
                .getActionView(searchMenuItem);
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}