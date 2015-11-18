package nl.hnogames.domoticz.Welcome;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.hnogames.domoticz.R;

public class WelcomePage2 extends Fragment {

    public static final WelcomePage2 newInstance() {
        return new WelcomePage2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome2, container, false);

        return v;
    }
}