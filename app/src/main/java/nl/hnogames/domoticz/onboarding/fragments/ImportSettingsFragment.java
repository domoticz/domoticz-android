
package nl.hnogames.domoticz.onboarding.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.onboarding.OnboardingActivity;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class ImportSettingsFragment extends Fragment {

    private final ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getData() != null) {
                        SharedPrefUtil sharedPrefs = new SharedPrefUtil(requireContext());
                        if (sharedPrefs.loadSharedPreferencesFromFile(result.getData().getData())) {
                            Toast.makeText(requireActivity(), R.string.settings_imported,
                                    Toast.LENGTH_LONG).show();
                            ((OnboardingActivity) requireActivity()).finishOnboarding(true);
                        } else {
                            Toast.makeText(requireActivity(), R.string.settings_import_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_import, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnSelectFile = view.findViewById(R.id.btn_select_file);

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, "settings.txt");
            importLauncher.launch(intent);
        });
    }
}

