package nl.hnogames.domoticz.Welcome;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.R;

public class WelcomeViewActivity extends FragmentActivity
        implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final int WELCOME_WIZARD = 1;
    @SuppressWarnings("unused")
    private static final int SETTINGS = 2;

    private final List<Fragment> fList = new ArrayList<>();
    private WelcomePageAdapter mAdapter;
    private ViewPager mPager;
    private TextView buttonPrev, buttonNext;
    private RelativeLayout navigation;
    private Integer[] background_colors;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setUpBackgroundColors();
        buildLayout();
    }

    public void finishWithResult(boolean success) {
        Bundle conData = new Bundle();
        conData.putBoolean("RESULT", success);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() > 0) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            disableFinishButton(false);
        } else {
            finishWithResult(false);
        }
    }

    private void setUpBackgroundColors() {
        Integer color1 = getResources().getColor(R.color.welcome1_background);
        Integer color2 = getResources().getColor(R.color.welcome2_background);
        Integer color3 = getResources().getColor(R.color.welcome3_background);
        Integer color4 = getResources().getColor(R.color.welcome4_background);

        background_colors = new Integer[]{color1, color2, color3, color4};
    }

    private void buildLayout() {
        List<Fragment> fragments = getFragments();
        mAdapter = new WelcomePageAdapter(getFragmentManager(), fragments);

        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(mAdapter);

        navigation = (RelativeLayout) findViewById(R.id.navigation);
        navigation.setBackgroundResource(R.color.welcome1_background);

        CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        float radius = mIndicator.getRadius();
        mIndicator.setRadius(radius + 4);
        mIndicator.setFillColor(getResources().getColor(android.R.color.darker_gray));
        mIndicator.setOnPageChangeListener(this);

        buttonPrev = (TextView) findViewById(R.id.btn_prev);
        buttonPrev.setText(getString(R.string.welcome_button_previous).toUpperCase());
        buttonPrev.setOnClickListener(this);

        buttonNext = (TextView) findViewById(R.id.btn_next);
        buttonNext.setText(getString(R.string.welcome_button_next).toUpperCase());
        buttonNext.setTextColor(getResources().getColor(R.color.white));
        buttonNext.setOnClickListener(this);
    }

    private List<Fragment> getFragments() {
        fList.add(WelcomePage1.newInstance());
        fList.add(WelcomePage2.newInstance());
        fList.add(WelcomePage3.newInstance(WELCOME_WIZARD));
        fList.add(WelcomePage4.newInstance());
        return fList;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                if (mPager.getCurrentItem() < fList.size() - 1) {
                    // Go to next page
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                } else {
                    // Last page, end wizard
                    endWelcomeWizard();
                }
                break;

            case R.id.btn_prev:
                if (mPager.getCurrentItem() != 0) {
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                    disableFinishButton(false);
                }
                break;
        }
    }

    public void disableFinishButton(boolean disable) {
        if (disable) buttonNext.setVisibility(View.INVISIBLE);
        else buttonNext.setVisibility(View.VISIBLE);
    }

    private void endWelcomeWizard() {
        finishWithResult(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position < (mAdapter.getCount() - 1) && position < (background_colors.length - 1)) {
            mPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, background_colors[position], background_colors[position + 1]));
        } else {
            mPager.setBackgroundColor(background_colors[background_colors.length - 1]);
        }
    }

    @Override
    public void onPageSelected(int position) {
        pageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void pageSelected(int position) {
        if (position == 0) {
            // First page
            navigation.setBackgroundResource(R.color.welcome1_background);
            buttonPrev.setVisibility(View.INVISIBLE);
        } else if (position == fList.size() - 1) {
            // Last page
            buttonNext.setText(getString(R.string.welcome_button_finish).toUpperCase());
        } else {
            // Everything in between
            navigation.setBackgroundResource(R.color.default_background_color_light);
            buttonPrev.setVisibility(View.VISIBLE);
            buttonNext.setText(getString(R.string.welcome_button_next).toUpperCase());
            buttonNext.setTextColor(getResources().getColor(R.color.light_gray));
            disableFinishButton(false);
        }
    }

}