package nl.hnogames.domoticz.helpers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import nl.hnogames.domoticz.interfaces.OnDiscoveredTagListener;

public class TagDispatcher {
    private static final int DELAY_PRESENCE = 5000;

    private OnDiscoveredTagListener tagDiscoveredListener;
    private boolean handleUnavailableNfc;
    private boolean disableSounds;
    private boolean dispatchOnUiThread;
    private boolean broadcomWorkaround;
    private boolean noReaderMode;
    private boolean disableNdefCheck;
    private Activity activity;

    public TagDispatcher(TagDispatcherBuilder tagDispatcherBuilder) {
        this.activity = tagDispatcherBuilder.activity;
        this.tagDiscoveredListener = tagDispatcherBuilder.tagDiscoveredListener;
        this.handleUnavailableNfc = tagDispatcherBuilder.enableUnavailableNfcUserPrompt;
        this.disableSounds = !tagDispatcherBuilder.enableSounds;
        this.dispatchOnUiThread = tagDispatcherBuilder.enableDispatchingOnUiThread;
        this.broadcomWorkaround = tagDispatcherBuilder.enableBroadcomWorkaround;
        this.noReaderMode = !tagDispatcherBuilder.enableReaderMode;
        this.disableNdefCheck = !tagDispatcherBuilder.enableNdefCheck;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public NfcStatus enableExclusiveNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                if (handleUnavailableNfc) {
                    toastMessage("Please activate NFC and then press back");
                    activity.startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                }
                return NfcStatus.AVAILABLE_DISABLED;
            }
            if (!noReaderMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                enableReaderMode(adapter);
            } else {
                enableForegroundDispatch(adapter);
            }
            return NfcStatus.AVAILABLE_ENABLED;
        }
        if (handleUnavailableNfc) toastMessage("NFC is not available on this device");
        return NfcStatus.NOT_AVAILABLE;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void disableExclusiveNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                disableReaderMode(adapter);
            } else {
                disableForegroundDispatch(adapter);
            }
        }
    }

    public boolean interceptIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            dispatchTag(tag);
            return true;
        } else {
            return false;
        }
    }

    private void dispatchTag(final Tag tag) {
        if (dispatchOnUiThread) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                new Handler(Looper.getMainLooper()).post(() -> tagDiscoveredListener.tagDiscovered(tag));
            } else {
                tagDiscoveredListener.tagDiscovered(tag);
            }

        } else {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... aParams) {
                    tagDiscoveredListener.tagDiscovered(tag);
                    return null;
                }
            }.execute();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void enableReaderMode(NfcAdapter adapter) {
        Bundle options = new Bundle();
        if (broadcomWorkaround) {
            /* This is a work around for some Broadcom chipsets that does
             * the presence check by sending commands that interrupt the
             * processing of the ongoing command.
             */
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, DELAY_PRESENCE);
        }
        NfcAdapter.ReaderCallback callback = tag -> dispatchTag(tag);
        int flags = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B;
        if (disableSounds) {
            flags = flags | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
        }
        if (disableNdefCheck) {
            flags = flags | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        }
        adapter.enableReaderMode(activity, callback, flags, options);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void disableReaderMode(NfcAdapter adapter) {
        adapter.disableReaderMode(activity);
    }

    private void enableForegroundDispatch(NfcAdapter adapter) {
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        enableForegroundDispatch(adapter, intent);
    }

    private void enableForegroundDispatch(NfcAdapter adapter, Intent intent) {
        if (adapter.isEnabled()) {
            PendingIntent tagIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            adapter.enableForegroundDispatch(activity, tagIntent, new IntentFilter[]{tag},
                    new String[][]{new String[]{IsoDep.class.getName()}});
        }
    }

    private void disableForegroundDispatch(NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void toastMessage(String message) {
        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public enum NfcStatus {
        AVAILABLE_ENABLED,
        AVAILABLE_DISABLED,
        NOT_AVAILABLE
    }
}