
package nl.hnogames.domoticz;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.ftinc.scoop.Scoop;

import java.util.Timer;
import java.util.TimerTask;

import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.fragments.Camera;

public class CameraActivity extends AppCompatAssistActivity {
    private Toolbar toolbar;
    private Timer cameraRefreshTimer = null;
    private Camera camera;
    private int cameraIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            this.finish();

        if (bundle != null) {
            //noinspection SpellCheckingInspection
            cameraIdx = bundle.getInt("CAMERAIDX");

            //noinspection SpellCheckingInspection
            String title = bundle.getString("CAMERATITLE");
            this.setTitle(title);

            camera = new Camera();
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportFragmentManager().beginTransaction().replace(R.id.main,
                    camera).commit();
            camera.setImage(cameraIdx);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (cameraRefreshTimer != null)
            getMenuInflater().inflate(R.menu.menu_camera_pause, menu);
        else
            getMenuInflater().inflate(R.menu.menu_camera, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void stopCameraTimer() {
        if (cameraRefreshTimer != null) {
            cameraRefreshTimer.cancel();
            cameraRefreshTimer.purge();
            cameraRefreshTimer = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            this.finish();
            return true;
        } else if (itemId == R.id.action_camera_pause) {
            stopCameraTimer();
            invalidateOptionsMenu();
            return true;
        } else if (itemId == R.id.action_camera_play) {
                if (cameraRefreshTimer == null) {
                    cameraRefreshTimer = new Timer("camera", true);
                    cameraRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    camera.setImage(cameraIdx);
                                }
                            });
                        }
                    }, 0, 1000);//schedule in 2 seconds
            }
            invalidateOptionsMenu();//set pause button
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        stopCameraTimer();
        super.onDestroy();
    }
}