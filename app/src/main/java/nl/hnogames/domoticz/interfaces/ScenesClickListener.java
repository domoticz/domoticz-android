
package nl.hnogames.domoticz.interfaces;

import android.view.View;

@SuppressWarnings("unused")
public interface ScenesClickListener {

    void onSceneClick(int idx, boolean action);

    void onLikeButtonClick(int idx, boolean checked);

    void onLogButtonClick(int idx);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(int position);

    void onTimerButtonClick(int idx);
}