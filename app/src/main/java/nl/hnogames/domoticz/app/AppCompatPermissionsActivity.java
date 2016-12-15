package nl.hnogames.domoticz.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;

import java.util.Arrays;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PermissionsUtil;

public class AppCompatPermissionsActivity extends AppCompatActivity implements OnPermissionCallback {

    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionHelper = PermissionHelper.getInstance(this);
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.i("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
        StringBuilder builder = new StringBuilder(permissionName.length);
        if (permissionName.length > 0) {
            for (String permission : permissionName) {
                builder.append(permission).append("\n");
            }
        }

        if (builder.toString().contains("android.permission.READ_PHONE_STATE")) {
            AlertDialog alert = PermissionsUtil.getAlertDialog(this, permissionHelper, this.getString(R.string.permission_title),
                    this.getString(R.string.permission_desc_phone), permissionName);
            if (!alert.isShowing()) {
                alert.show();
            }
        }

        if (builder.toString().contains("android.permission.READ_EXTERNAL_STORAGE") || builder.toString().contains("android.permission.WRITE_EXTERNAL_STORAGE")) {
            AlertDialog alert = PermissionsUtil.getAlertDialog(this, permissionHelper, this.getString(R.string.permission_title),
                    this.getString(R.string.permission_desc_storage), permissionName);
            if (!alert.isShowing()) {
                alert.show();
            }
        }

        if (builder.toString().contains("android.permission.CAMERA")) {
            AlertDialog alert = PermissionsUtil.getAlertDialog(this, permissionHelper, this.getString(R.string.permission_title),
                    this.getString(R.string.permission_desc_camera), permissionName);
            if (!alert.isShowing()) {
                alert.show();
            }
        }

        if (builder.toString().contains("android.permission.RECORD_AUDIO")) {
            AlertDialog alert = PermissionsUtil.getAlertDialog(this, permissionHelper, this.getString(R.string.permission_title),
                    this.getString(R.string.permission_desc_audio), permissionName);
            if (!alert.isShowing()) {
                alert.show();
            }
        }

        if (builder.toString().contains("android.permission.USE_FINGERPRINT")) {
            AlertDialog alert = PermissionsUtil.getAlertDialog(this, permissionHelper, this.getString(R.string.permission_title),
                    this.getString(R.string.permission_desc_finger), permissionName);
            if (!alert.isShowing()) {
                alert.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.i("onPermissionPreGranted", "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        Log.i("NeedExplanation", "Permission( " + permissionName + " ) needs Explanation");
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.i("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.i("onNoPermissionNeeded", "Permission(s) not needed");
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
    }
}
