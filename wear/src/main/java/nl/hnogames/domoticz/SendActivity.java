
package nl.hnogames.domoticz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.wear.activity.ConfirmationActivity;
import androidx.wear.widget.CircularProgressLayout;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SendActivity extends DomoticzActivity implements androidx.wear.widget.CircularProgressLayout.OnTimerFinishedListener, View.OnClickListener {

    private androidx.wear.widget.CircularProgressLayout circularProgress;
    private String selectedSwitch = "";

    // Sample dataset for the list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            selectedSwitch = extras.getString("SWITCH", "");

        circularProgress = findViewById(R.id.delayed_confirm);
        circularProgress.setOnTimerFinishedListener(this);
        circularProgress.setOnClickListener(this);
        circularProgress.setTotalTime(3000);
        circularProgress.startTimer();
    }

    @Override
    public void onTimerFinished(CircularProgressLayout layout) {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.triggered));
        startActivity(intent);
        sendMessage(SEND_SWITCH, selectedSwitch);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if (view.equals(circularProgress)) {
            // User canceled, abort the action
            circularProgress.stopTimer();
            this.finish();
        }
    }
}