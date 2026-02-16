
package nl.hnogames.domoticz.onboarding.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import nl.hnogames.domoticz.R;

public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnGetStarted = view.findViewById(R.id.btn_get_started);
        MaterialButton btnRestoreBackup = view.findViewById(R.id.btn_restore_backup);

        btnGetStarted.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_welcome_to_setup_method)
        );

        btnRestoreBackup.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_welcome_to_import)
        );
    }
}

