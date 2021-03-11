package nl.hnogames.domoticz.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class WifiReceiverManager extends Worker {
    private static final String TAG = "WifiReceiverManager";
    public static String workTag = "checkwifi";
    private WorkerParameters params;
    private Context context;

    public WifiReceiverManager(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.params = params;
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("SessionManager", "Starting to kickoff the receiver");
        SharedPrefUtil prefUtil = new SharedPrefUtil(context);
        if (!prefUtil.isWifiEnabled()) {
            return Result.failure();
        }

        try {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest
                    .Builder(WifiReceiver.class)
                    .addTag(WifiReceiver.workTag)
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        try {
            Thread.sleep(30000);
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest
                    .Builder(WifiReceiverManager.class)
                    .addTag(WifiReceiverManager.workTag)
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
            return Result.success();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}