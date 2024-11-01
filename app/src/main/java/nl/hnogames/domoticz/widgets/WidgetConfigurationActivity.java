package nl.hnogames.domoticz.widgets;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.adapters.WidgetsAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.ui.PasswordDialog;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.widgets.database.WidgetContract;
import nl.hnogames.domoticz.widgets.database.WidgetDbHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private static final int BUTTON_TOGGLE = 1;
    private static final int BUTTON_ONOFF = 2;
    private static final int BUTTON_BLINDS = 3;
    private final String TAG = this.getClass().getSimpleName();
    private final int iWelcomeResultCode = 885;
    private final int iVoiceAction = -55;
    private final int iQRCodeAction = -66;
    public CoordinatorLayout coordinatorLayout;
    int mAppWidgetId;
    private SharedPrefUtil mSharedPrefs;
    private WidgetsAdapter adapter;
    private SearchView searchViewAction;
    private Toolbar toolbar;
    private WidgetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        Scoop.getInstance().apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.widget_configuration);
        setResult(RESULT_CANCELED);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        mDbHelper = new WidgetDbHelper(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initListViews() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            Log.i(TAG, "Showing switches for widget");
            StaticHelper.getDomoticz(WidgetConfigurationActivity.this).getDevices(new DevicesReceiver() {
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

                    ListView listView = findViewById(R.id.list);
                    adapter = new WidgetsAdapter(WidgetConfigurationActivity.this, StaticHelper.getDomoticz(WidgetConfigurationActivity.this), mDevicesInfo);
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                            UsefulBits.showSnackbarWithAction(WidgetConfigurationActivity.this, coordinatorLayout, getString(R.string.wizard_widgets) + " " + getString(R.string.premium_feature), Snackbar.LENGTH_LONG, null,
                                    v -> UsefulBits.openPremiumAppStore(WidgetConfigurationActivity.this, IsPremiumEnabled -> recreate()), getString(R.string.upgrade));
                            return;
                        }

                        final DevicesInfo mDeviceInfo = (DevicesInfo) adapter.getItem(position);
                        if (mDeviceInfo.isProtected()) {
                            PasswordDialog passwordDialog = new PasswordDialog(
                                    WidgetConfigurationActivity.this, StaticHelper.getDomoticz(WidgetConfigurationActivity.this));
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(String password) {
                                    if (mDeviceInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                                        showSelectorDialog(mDeviceInfo, password);
                                    else
                                        getBackground(mDeviceInfo, password, null);
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                        } else {
                            if (mDeviceInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                                showSelectorDialog(mDeviceInfo, null);
                            else
                                getBackground(mDeviceInfo, null, null);
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

    private void showSelectorDialog(final DevicesInfo selector, final String pass) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback((dialog, view, which, text) -> getBackground(selector, pass, String.valueOf(text)))
                .show();
    }

    private int getWidgetLayout(String background, DevicesInfo mSelectedSwitch) {
        int layout = R.layout.widget_layout;
        String backgroundWidget = String.valueOf(background);

        //default layouts
        if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_dark))) {
            layout = R.layout.widget_layout_dark;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_light))) {
            layout = R.layout.widget_layout;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_light))) {
            layout = R.layout.widget_layout_transparent;
        } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_dark))) {
            layout = R.layout.widget_layout_transparent_dark;
        }

        try {
            int withButtons = withButtons(mSelectedSwitch);
            if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_dark))) {
                if (withButtons == BUTTON_ONOFF)
                    layout = R.layout.widget_layout_buttons_dark;
                if (withButtons == BUTTON_BLINDS)
                    layout = R.layout.widget_layout_blinds_dark;
            } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_light))) {
                if (withButtons == BUTTON_ONOFF)
                    layout = R.layout.widget_layout_buttons;
                if (withButtons == BUTTON_BLINDS)
                    layout = R.layout.widget_layout_blinds;
            } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_light))) {
                if (withButtons == BUTTON_ONOFF)
                    layout = R.layout.widget_layout_buttons_transparent;
                if (withButtons == BUTTON_BLINDS)
                    layout = R.layout.widget_layout_blinds_transparent;
            } else if (backgroundWidget.equals(getApplicationContext().getString(R.string.widget_transparent_dark))) {
                if (withButtons == BUTTON_ONOFF)
                    layout = R.layout.widget_layout_buttons_transparent_dark;
                if (withButtons == BUTTON_BLINDS)
                    layout = R.layout.widget_layout_blinds_transparent_dark;
            }

        } catch (Exception ex) { /*if this crashes we use the default layouts */ }

        return layout;
    }

    private void getBackground(final DevicesInfo mSelectedSwitch, final String password, final String value) {
        new MaterialDialog.Builder(this)
                .title(this.getString(R.string.widget_background))
                .items(new String[]{this.getString(R.string.widget_dark), this.getString(R.string.widget_light), this.getString(R.string.widget_transparent_dark), this.getString(R.string.widget_transparent_light)})
                .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
                    showAppWidget(mSelectedSwitch, password, value, getWidgetLayout(String.valueOf(text), mSelectedSwitch));
                    return true;
                })
                .positiveText(R.string.ok)
                .show();
    }

    private void showAppWidget(DevicesInfo mSelectedSwitch, String password, String value, int layoutId) {
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int idx = mSelectedSwitch.getIdx();

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            ContentValues values = new ContentValues();
            if (UsefulBits.isEmpty(mSelectedSwitch.getType())) {
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX, idx);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE, false);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD, password);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID, layoutId);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_VALUE, value);
            } else {
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX, idx);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE, mSelectedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || mSelectedSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE));
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD, password);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID, layoutId);
                values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_VALUE, value);
            }

            mDbHelper.saveWidgetConfiguration(mAppWidgetId, values);
            Intent startService = new Intent(WidgetConfigurationActivity.this, WidgetProviderLarge.UpdateWidgetService.class);
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

    private int withButtons(DevicesInfo s) {
        int withButton = 0;
        if (s != null) {
            if (s.getSwitchTypeVal() == 0 &&
                    (UsefulBits.isEmpty(s.getSwitchType()))) {
                switch (s.getType()) {
                    case DomoticzValues.Scene.Type.SCENE:
                        withButton = BUTTON_TOGGLE;
                        break;
                    case DomoticzValues.Scene.Type.GROUP:
                        withButton = BUTTON_ONOFF;
                        break;
                }
            } else {
                switch (s.getSwitchTypeVal()) {
                    case DomoticzValues.Device.Type.Value.ON_OFF:
                    case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                    case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    case DomoticzValues.Device.Type.Value.SELECTOR:
                    case DomoticzValues.Device.Type.Value.DIMMER:
                        //if (mSharedPrefs.showSwitchesAsButtons())
                        withButton = BUTTON_ONOFF;
                        //else
                        //    withButton = BUTTON_TOGGLE;
                        break;

                    case DomoticzValues.Device.Type.Value.X10SIREN:
                    case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                    case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                    case DomoticzValues.Device.Type.Value.DOORBELL:
                        withButton = BUTTON_TOGGLE;
                        break;


                    case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                    case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                        withButton = BUTTON_BLINDS;
                        break;

                    case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                        withButton = BUTTON_ONOFF;
                        break;

                    case DomoticzValues.Device.Type.Value.BLINDS:
                        if (DomoticzValues.canHandleStopButton(s))
                            withButton = BUTTON_BLINDS;
                        else
                            withButton = BUTTON_ONOFF;
                        break;
                }
            }
        }
        return withButton;
    }
}
