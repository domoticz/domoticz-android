
package nl.hnogames.domoticz;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.utils.UsefulBits;


public class QRCodeCaptureActivity extends AppCompatAssistActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            setFullscreenFlags();
        }

        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    private void setSystemUiFlags(int flags) {
        int systemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        systemUiVisibility |= flags;
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    private void setFullscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int fullscreenFlags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN;

            setSystemUiFlags(fullscreenFlags);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("SCANNER", rawResult.getText()); // Prints scan results
        Log.v("SCANNER", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        finishWithResult(UsefulBits.ByteArrayToHexString(rawResult.getText().getBytes()));
    }

    private void finishWithResult(String QRCode) {
        Bundle conData = new Bundle();
        conData.putString("QRCODE", QRCode);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }
}