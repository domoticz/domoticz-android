package nl.hnogames.domoticz;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Service.WidgetProviderLarge;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;

/**
 * Created by m.heinis on 12/10/2015.
 */
public class WidgetConfigurationActivity extends AppCompatActivity
{

    private SharedPrefUtil mSharedPrefs;
    private final int iWelcomeResultCode = 885;
    private Domoticz domoticz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configuration);
        setResult(RESULT_CANCELED);

        mSharedPrefs = new SharedPrefUtil(this);
        domoticz = new Domoticz(this);

        this.setTitle("Choose Switch");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

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
            Log.i(this.getClass().getSimpleName(), "Showing switches for widget");
            domoticz.getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(final ArrayList<DevicesInfo> mDevicesInfo) {
                    final ArrayList<DevicesInfo> mDevices = new ArrayList<DevicesInfo>();
                    for (DevicesInfo s : mDevicesInfo) {
                        if (!s.getType().equals(Domoticz.Scene.Type.GROUP) && !s.getType().equals(Domoticz.Scene.Type.SCENE)) {
                            mDevices.add(s);
                        }
                    }
                    Collections.sort(mDevices);

                    String[] listData = processSwitches(mDevices);
                    ListView listView = (ListView) findViewById(R.id.list);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(WidgetConfigurationActivity.this,
                            android.R.layout.simple_list_item_1, android.R.id.text1, listData);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            showAppWidget(mDevices.get(position));
                        }
                    });
                    listView.setAdapter(adapter);
                }

                @Override
                public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(WidgetConfigurationActivity.this, "Failed to get switches", Toast.LENGTH_SHORT).show();
                    WidgetConfigurationActivity.this.finish();
                }
            },0);
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onStop()
    {
        if(domoticz!=null)
            domoticz.Disconnect();

        super.onStop();
    }

    public String[] processSwitches(ArrayList<DevicesInfo> switches) {
        String[] listData = new String[switches.size()];
        int counter = 0;
        for (DevicesInfo s : switches) {

                String log = s.getName();
                listData[counter] = log;
                counter++;

        }
        Arrays.sort(listData);
        return listData;
    }

    int mAppWidgetId;
    private void showAppWidget(DevicesInfo mSelectedSwitch) {
        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        int idx = mSelectedSwitch.getIdx();

        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);

            //AppWidgetProviderInfo providerInfo = AppWidgetManager.getInstance(
            //        getBaseContext()).getAppWidgetInfo(mAppWidgetId);

            mSharedPrefs.setWidgetIDX(mAppWidgetId, idx);
            mSharedPrefs.setWidgetIDforIDX(mAppWidgetId, idx);

            Intent startService = new Intent(WidgetConfigurationActivity.this,
                    WidgetProviderLarge.UpdateWidgetService.class);
            startService.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
            startService.setAction("FROM CONFIGURATION ACTIVITY");
            startService(startService);
            setResult(RESULT_OK, startService);

            finish();
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            Log.i("I am invalid", "I am invalid");
            finish();
        }
    }
}