package nl.hnogames.domoticz.widgets;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

/**
 * Widget configuration activity
 * Material Design interface for selecting devices/scenes
 */
public class WidgetConfigActivity extends AppCompatActivity {
    private static final String TAG = "WidgetConfigActivity";

    private int widgetId = INVALID_APPWIDGET_ID;
    private WidgetRepository repository;

    private ListView itemListView;
    private ProgressBar progressBar;
    private Button btnSave;
    private EditText itemSearchEdit;

    private List<DevicesInfo> allItems = new ArrayList<>();
    private List<DevicesInfo> filteredItems = new ArrayList<>();
    private int selectedIdx = -1;
    private String selectedName = "";
    private boolean selectedIsScene = false;

    private CheckmarkAdapter itemAdapter;

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
        itemListView = findViewById(R.id.item_list);
        progressBar = findViewById(R.id.progress_bar);
        btnSave = findViewById(R.id.btn_save);
        itemSearchEdit = findViewById(R.id.item_search);

        btnSave.setEnabled(false);
        btnSave.setOnClickListener(v -> saveConfiguration());

        // Load devices and scenes
        loadDevicesAndScenes();
    }

    private void loadDevicesAndScenes() {
        progressBar.setVisibility(View.VISIBLE);

        // Load all devices (including scenes and groups)
        StaticHelper.getDomoticz(this).getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                allItems = mDevicesInfo != null ? mDevicesInfo : new ArrayList<>();
                Log.d(TAG, "Loaded " + allItems.size() + " items (devices, scenes, and groups)");
                displayData();
            }

            @Override
            public void onReceiveDevice(DevicesInfo device) {
                // Not used
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading items", error);
                Toast.makeText(WidgetConfigActivity.this,
                    "Error loading items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                displayData();
            }
        }, 0, null);
    }

    private void displayData() {
        progressBar.setVisibility(View.GONE);

        // Initialize filtered list
        filteredItems = new ArrayList<>(allItems);

        if (!allItems.isEmpty()) {
            itemSearchEdit.setVisibility(View.VISIBLE);

            updateItemList();

            // Set up search functionality
            itemSearchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterItems(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            itemListView.setOnItemClickListener((parent, view, position, id) -> {
                DevicesInfo item = filteredItems.get(position);
                selectedIdx = item.getIdx();
                selectedName = item.getName();

                // Check if it's a scene or group
                String type = item.getType();
                selectedIsScene = (type != null &&
                    (type.equals(DomoticzValues.Scene.Type.SCENE) ||
                     type.equals(DomoticzValues.Scene.Type.GROUP)));

                btnSave.setEnabled(true);

                // Update checkmark display
                itemAdapter.setSelectedPosition(position);

                Log.d(TAG, "Selected item: " + selectedName + " (idx: " + selectedIdx +
                    ", isScene: " + selectedIsScene + ")");
            });
        } else {
            itemSearchEdit.setVisibility(View.GONE);
            itemListView.setVisibility(View.GONE);
        }
    }

    private void filterItems(String query) {
        filteredItems.clear();
        if (query.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (DevicesInfo item : allItems) {
                if (item.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        updateItemList();
    }

    private void updateItemList() {
        List<String> itemNames = new ArrayList<>();
        for (DevicesInfo item : filteredItems) {
            itemNames.add(item.getName());
        }
        itemAdapter = new CheckmarkAdapter(this, itemNames);
        itemListView.setAdapter(itemAdapter);
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
