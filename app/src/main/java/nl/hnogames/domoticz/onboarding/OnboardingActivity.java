
package nl.hnogames.domoticz.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingViewModel viewModel;
    private NavController navController;
    private SharedPrefUtil sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = new SharedPrefUtil(this);

        // Apply language preference
        if (!UsefulBits.isEmpty(sharedPrefs.getDisplayLanguage())) {
            UsefulBits.setDisplayLanguage(this, sharedPrefs.getDisplayLanguage());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    public OnboardingViewModel getViewModel() {
        return viewModel;
    }

    public void finishOnboarding(boolean success) {
        Bundle resultData = new Bundle();
        resultData.putBoolean("RESULT", success);
        Intent intent = new Intent();
        intent.putExtras(resultData);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (navController != null && !navController.popBackStack()) {
            // If we can't go back in navigation, finish with no result
            finishOnboarding(false);
        }
    }
}

