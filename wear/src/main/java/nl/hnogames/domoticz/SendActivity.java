package nl.hnogames.domoticz;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SendActivity extends DomoticzActivity implements
        DelayedConfirmationView.DelayedConfirmationListener{

    private DelayedConfirmationView delayedConfirmationView;
    private String selectedSwitch = "";

    // Sample dataset for the list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Bundle extras = getIntent().getExtras();
        selectedSwitch = extras.getString("SWITCH", "");

        delayedConfirmationView = (DelayedConfirmationView)findViewById(R.id.delayed_confirm);
        delayedConfirmationView.setListener(this);
        delayedConfirmationView.setImageResource(R.drawable.ic_stop);
        delayedConfirmationView.setTotalTimeMs(3000);
        delayedConfirmationView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        //process message
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.triggered));
        startActivity(intent);

        sendMessage(SEND_SWITCH, selectedSwitch);
        this.finish();
    }

    @Override
    public void onTimerSelected(View view) {
        delayedConfirmationView.reset();
        this.finish();
    }
}