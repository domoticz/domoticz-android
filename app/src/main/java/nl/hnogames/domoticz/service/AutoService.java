package nl.hnogames.domoticz.service;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

public class AutoService extends CarAppService {
    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // For production: use proper host validation
        // For now, allowing all hosts for compatibility
        // Consider using: new HostValidator.Builder(getApplicationContext())
        //     .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
        //     .build();
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                return new AutoMainScreen(getCarContext());
            }
        };
    }
}