package nl.hnogames.domoticz.Welcome;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import nl.hnogames.domoticz.R;

public class WelcomePage1 extends Fragment {

    public static WelcomePage1 newInstance() {
        return new WelcomePage1();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome1, container, false);

        int mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        ImageView myImageView = (ImageView) v.findViewById(R.id.logo_domoticz);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        myFadeInAnimation.setDuration(mShortAnimationDuration);
        myImageView.startAnimation(myFadeInAnimation);

        return v;
    }
}