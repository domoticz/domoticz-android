package nl.hnogames.domoticz.widgets;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ftinc.scoop.Scoop;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.widgets.data.WidgetRepository;
import nl.hnogames.domoticz.widgets.database.WidgetDatabase;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;

/**
 * Widget configuration activity
 * Material Design interface for selecting devices/scenes
 */
public class WidgetConfigActivity extends AppCompatActivity {
    private static final String TAG = "WidgetConfigActivity";

    private int widgetId = INVALID_APPWIDGET_ID;
    private WidgetRepository repository;

    private ListView deviceListView;
    private ListView sceneListView;
    private TextView devicesHeader;
    private TextView scenesHeader;
    private ProgressBar progressBar;
    private Button btnSave;

    private List<DevicesInfo> devices = new ArrayList<>();
    private List<SceneInfo> scenes = new ArrayList<>();
    private int selectedIdx = -1;
    private String selectedName = "";
    private boolean selectedIsScene = false;

    private CheckmarkAdapter deviceAdapter;
    private CheckmarkAdapter sceneAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);

        // Get widget ID
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);
        }

        if (widgetId == INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Initialize repository
        WidgetDatabase db = WidgetDatabase.getInstance(this);
        repository = new WidgetRepository(this, db.widgetDao());

        // Initialize views
        deviceListView = findViewById(R.id.device_list);
        sceneListView = findViewById(R.id.scene_list);
        devicesHeader = findViewById(R.id.devices_header);
        scenesHeader = findViewById(R.id.scenes_header);
        progressBar = findViewById(R.id.progress_bar);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setEnabled(false);
        btnSave.setOnClickListener(v -> saveConfiguration());

        // Load devices and scenes
        loadDevicesAndScenes();
    }

    private void loadDevicesAndScenes() {
        progressBar.setVisibility(View.VISIBLE);

        // Load devices
        StaticHelper.getDomoticz(this).getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                devices = mDevicesInfo != null ? mDevicesInfo : new ArrayList<>();
                Log.d(TAG, "Loaded " + devices.size() + " devices");
                loadScenes();
            }

            @Override
            public void onReceiveDevice(DevicesInfo device) {
                // Not used
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading devices", error);
                Toast.makeText(WidgetConfigActivity.this,
                    "Error loading devices: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                loadScenes();
            }
        }, 0, null);
    }

    private void loadScenes() {
        StaticHelper.getDomoticz(this).getScenes(new ScenesReceiver() {
            @Override
            public void onReceiveScenes(ArrayList<SceneInfo> mScenes) {
                scenes = mScenes != null ? mScenes : new ArrayList<>();
                Log.d(TAG, "Loaded " + scenes.size() + " scenes");
                displayData();
            }

            @Override
            public void onReceiveScene(SceneInfo scene) {
                // Not used
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading scenes", error);
                Toast.makeText(WidgetConfigActivity.this,
                    "Error loading scenes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                displayData();
            }
        });
    }

    private void displayData() {
        progressBar.setVisibility(View.GONE);

        // Display devices
        if (!devices.isEmpty()) {
            devicesHeader.setVisibility(View.VISIBLE);
            List<String> deviceNames = new ArrayList<>();
            for (DevicesInfo device : devices) {
                deviceNames.add(device.getName());
            }
            deviceAdapter = new CheckmarkAdapter(this, deviceNames);
            deviceListView.setAdapter(deviceAdapter);
            setListViewHeightBasedOnItems(deviceListView);

            deviceListView.setOnItemClickListener((parent, view, position, id) -> {
                DevicesInfo device = devices.get(position);
                selectedIdx = device.getIdx();
                selectedName = device.getName();
                selectedIsScene = false;
                btnSave.setEnabled(true);

                Log.d(TAG, "Device selected - Name: " + selectedName + ", idx: " + selectedIdx);

                // Update checkmark display
                deviceAdapter.setSelectedPosition(position);
                if (sceneAdapter != null) {
                    sceneAdapter.setSelectedPosition(-1);
                }

                Log.d(TAG, "Selected device: " + selectedName + " (idx: " + selectedIdx + ")");
            });
        } else {
            devicesHeader.setVisibility(View.GONE);
            deviceListView.setVisibility(View.GONE);
        }

        // Display scenes
        if (!scenes.isEmpty()) {
            scenesHeader.setVisibility(View.VISIBLE);
            List<String> sceneNames = new ArrayList<>();
            for (SceneInfo scene : scenes) {
                sceneNames.add(scene.getName());
            }
            sceneAdapter = new CheckmarkAdapter(this, sceneNames);
            sceneListView.setAdapter(sceneAdapter);
            setListViewHeightBasedOnItems(sceneListView);

            sceneListView.setOnItemClickListener((parent, view, position, id) -> {
                SceneInfo scene = scenes.get(position);
                selectedIdx = scene.getIdx();
                selectedName = scene.getName();
                selectedIsScene = true;
                btnSave.setEnabled(true);

                // Update checkmark display
                sceneAdapter.setSelectedPosition(position);
                if (deviceAdapter != null) {
                    deviceAdapter.setSelectedPosition(-1);
                }

                Log.d(TAG, "Selected scene: " + selectedName + " (idx: " + selectedIdx + ")");
            });
        } else {
            scenesHeader.setVisibility(View.GONE);
            sceneListView.setVisibility(View.GONE);
        }
    }

    private void setListViewHeightBasedOnItems(ListView listView) {
        if (listView.getAdapter() == null) return;

        int totalHeight = 0;
        int itemCount = listView.getAdapter().getCount();

        for (int i = 0; i < itemCount; i++) {
            View listItem = listView.getAdapter().getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        totalHeight += (listView.getDividerHeight() * (itemCount - 1));

        android.view.ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void saveConfiguration() {
        if (selectedIdx == -1) {
            Toast.makeText(this, "Please select a device or scene", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Saving widget config - ID: " + widgetId + ", Device/Scene: " + selectedName +
            " (idx: " + selectedIdx + "), isScene: " + selectedIsScene);

        repository.saveWidgetConfig(
            widgetId,
            selectedIdx,
            selectedName,
            "",
            selectedIsScene,
            "auto",
            "auto",
            null,
            () -> {
                // Update the widget after save is complete
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                DomoticzWidget.updateAppWidget(this, appWidgetManager, widgetId);

                // Return success
                Intent resultValue = new Intent();
                resultValue.putExtra(EXTRA_APPWIDGET_ID, widgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        );
    }

    /**
     * Custom adapter for list items with checkmark indicator
     */
    private static class CheckmarkAdapter extends ArrayAdapter<String> {
        private int selectedPosition = -1;

        public CheckmarkAdapter(@NonNull Context context, @NonNull List<String> items) {
            super(context, R.layout.widget_device_list_item, R.id.device_name, items);
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            ImageView checkIndicator = view.findViewById(R.id.check_indicator);
            if (checkIndicator != null) {
                checkIndicator.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
            }

            return view;
        }
    }
}
