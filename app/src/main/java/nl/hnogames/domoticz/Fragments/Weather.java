package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.WeatherAdapter;
import nl.hnogames.domoticz.Containers.Language;
import nl.hnogames.domoticz.Containers.WeatherInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.WeatherClickListener;
import nl.hnogames.domoticz.Interfaces.WeatherReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.WeatherInfoDialog;
import nl.hnogames.domoticz.Utils.AnimationUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;

public class Weather extends DomoticzRecyclerFragment implements DomoticzFragmentListener, WeatherClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Weather.class.getSimpleName();
    private Context mContext;
    private WeatherAdapter adapter;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private ArrayList<WeatherInfo> mWeatherInfoList;

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processWeather();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_weather);
        initAnimation();
    }

    @Override
    public void Filter(String text) {
        filter = text;
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConnectionOk() {
        super.showSpinner(true);
        processWeather();
    }

    private void processWeather() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        mDomoticz.getWeathers(new WeatherReceiver() {

            @Override
            public void onReceiveWeather(ArrayList<WeatherInfo> mWeatherInfos) {
                mWeatherInfoList = mWeatherInfos;
                if (getView() != null) {
                    successHandling(mWeatherInfos.toString(), false);
                    createListView(mWeatherInfos);
                }
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    private void initAnimation() {
        animShow = AnimationUtil.getLogRowAnimationOpen(mContext);
        animHide = AnimationUtil.getLogRowAnimationClose(mContext);
    }

    private void createListView(ArrayList<WeatherInfo> mWeatherInfos) {
        adapter = new WeatherAdapter(mContext, mDomoticz, getServerUtil(), mWeatherInfos, this);
        SlideInBottomAnimationAdapter alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
        gridView.setAdapter(alphaSlideIn);

        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processWeather();
            }
        });
        super.showSpinner(false);
        this.Filter(filter);
    }

    private void showInfoDialog(final WeatherInfo mWeatherInfo) {
        WeatherInfoDialog infoDialog = new WeatherInfoDialog(
                mContext,
                mWeatherInfo,
                R.layout.dialog_weather);
        infoDialog.setWeatherInfo(mWeatherInfo);
        infoDialog.show();
        infoDialog.onDismissListener(new WeatherInfoDialog.DismissListener() {
            @Override
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged)
                    changeFavorite(mWeatherInfo, isFavorite);
            }
        });
    }

    private void changeFavorite(final WeatherInfo mWeatherInfo, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mWeatherInfo.getIdx() + " favorite to " + isFavorite);

        if (isFavorite)
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mWeatherInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        else
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mWeatherInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = Domoticz.Device.Favorite.ON;
        else jsonAction = Domoticz.Device.Favorite.OFF;

        mDomoticz.setAction(mWeatherInfo.getIdx(),
                jsonUrl,
                jsonAction,
                0,
                null,
                new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        successHandling(result, false);
                        mWeatherInfo.setFavoriteBoolean(isFavorite);
                    }

                    @Override
                    public void onError(Exception error) {
                        errorHandling(error);
                    }
                });
    }

    @Override
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLogClick(final WeatherInfo weather, final String range) {
        final String graphType = weather.getTypeImg()
                .toLowerCase()
                .replace("temperature", "temp")
                .replace("visibility", "counter");

        JSONObject language = null;
        Language languageObj = new SharedPrefUtil(mContext).getSavedLanguage();
        if (languageObj != null) language = languageObj.getJsonObject();
        String graphDialogTitle;
        if (language != null) {
            graphDialogTitle = language.optString(weather.getType(), graphType);
        } else {
            graphDialogTitle = weather.getType();
        }

        Intent intent = new Intent(mContext, GraphActivity.class);
        intent.putExtra("IDX", weather.getIdx());
        intent.putExtra("RANGE", range);
        intent.putExtra("TYPE", graphType);
        intent.putExtra("TITLE", graphDialogTitle.toUpperCase());
        intent.putExtra("STEPS", 4);
        startActivity(intent);
    }

    @Override
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getWeather(idx), checked);
    }

    @Override
    public void onItemClicked(View v, int position) {
        LinearLayout extra_panel = (LinearLayout) v.findViewById(R.id.extra_panel);
        if (extra_panel != null) {
            if (extra_panel.getVisibility() == View.VISIBLE) {
                extra_panel.startAnimation(animHide);
                extra_panel.setVisibility(View.GONE);
            } else {
                extra_panel.setVisibility(View.VISIBLE);
                extra_panel.startAnimation(animShow);
            }

            if (extra_panel != lExtraPanel) {
                if (lExtraPanel != null) {
                    if (lExtraPanel.getVisibility() == View.VISIBLE) {
                        lExtraPanel.startAnimation(animHide);
                        lExtraPanel.setVisibility(View.GONE);
                    }
                }
            }

            lExtraPanel = extra_panel;
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        showInfoDialog(adapter.filteredData.get(position));
        return true;
    }

    private WeatherInfo getWeather(int idx) {
        WeatherInfo clickedWeather = null;
        for (WeatherInfo mWeatherInfo : mWeatherInfoList) {
            if (mWeatherInfo.getIdx() == idx) {
                clickedWeather = mWeatherInfo;
            }
        }
        return clickedWeather;
    }
}