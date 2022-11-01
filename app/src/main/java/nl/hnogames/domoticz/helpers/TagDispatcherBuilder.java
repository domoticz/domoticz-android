package nl.hnogames.domoticz.helpers;


import android.app.Activity;

import nl.hnogames.domoticz.interfaces.OnDiscoveredTagListener;

/**
 * TagDispatcherBuilder helps create a TagDispatcher using the builder pattern.
 * <p>
 * TagDispatcherBuilder will return a TagDispatcher after calling the
 * {@link #build()}. Depending on your preferences you can call the methods in
 * the builder to change the default behavior.
 */
public class TagDispatcherBuilder {
    Activity activity;
    OnDiscoveredTagListener tagDiscoveredListener;
    boolean enableUnavailableNfcUserPrompt;
    boolean enableSounds;
    boolean enableDispatchingOnUiThread;
    boolean enableBroadcomWorkaround;
    boolean enableReaderMode;
    boolean enableNdefCheck;

    public TagDispatcherBuilder(final Activity activity,
                                final OnDiscoveredTagListener tagDiscoveredListener) {
        this.activity = activity;
        this.tagDiscoveredListener = tagDiscoveredListener;
        this.enableUnavailableNfcUserPrompt = true;
        this.enableSounds = true;
        this.enableDispatchingOnUiThread = true;
        this.enableBroadcomWorkaround = true;
        this.enableReaderMode = true;
        this.enableNdefCheck = true;
    }

    public TagDispatcherBuilder enableUnavailableNfcUserPrompt(boolean enableUnavailableNfcUserPrompt) {
        this.enableUnavailableNfcUserPrompt = enableUnavailableNfcUserPrompt;
        return this;
    }

    public TagDispatcherBuilder enableSounds(boolean enableSounds) {
        this.enableSounds = enableSounds;
        return this;
    }

    public TagDispatcherBuilder enableDispatchingOnUiThread(boolean enableDispatchingOnUiThread) {
        this.enableDispatchingOnUiThread = enableDispatchingOnUiThread;
        return this;
    }

    public TagDispatcherBuilder enableBroadcomWorkaround(boolean enableBroadcomWorkaround) {
        this.enableBroadcomWorkaround = enableBroadcomWorkaround;
        return this;
    }

    public TagDispatcherBuilder enableReaderMode(boolean enableReaderMode) {
        this.enableReaderMode = enableReaderMode;
        return this;
    }

    public TagDispatcherBuilder enableNdefCheck(boolean enableNdefCheck) {
        this.enableNdefCheck = enableNdefCheck;
        return this;
    }

    public TagDispatcher build() {
        return new TagDispatcher(this);
    }
}