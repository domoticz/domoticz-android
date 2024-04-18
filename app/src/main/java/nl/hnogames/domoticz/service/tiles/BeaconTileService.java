package nl.hnogames.domoticz.service.tiles;

import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.service.WifiReceiver;
import nl.hnogames.domoticz.service.WifiReceiverManager;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class BeaconTileService extends TileService {
    private SharedPrefUtil mSharedPrefUtil;

    @Override
    public void onClick() {
        super.onClick();

        if(mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(this);

        boolean isEnabled = !mSharedPrefUtil.isBeaconEnabled();
        if(isEnabled) {
            if (!AppController.IsPremiumEnabled || !mSharedPrefUtil.isAPKValidated()) {
                UsefulBits.showPremiumToast(this, getString(R.string.beacon));
                return;
            }
        }

        mSharedPrefUtil.setBeaconEnabled(isEnabled);
        if (!isEnabled)
            AppController.getInstance().StopBeaconScanning();
        else {
            try {
                AppController.getInstance().StartBeaconScanning();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        updateTile(isEnabled);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if(mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(this);

        boolean isEnabled = mSharedPrefUtil.isBeaconEnabled();
        updateTile(isEnabled);
    }

    private void updateTile(boolean isEnabled) {
        Tile tile = getQsTile();
        if (isEnabled) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }
}