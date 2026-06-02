
package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import nl.hnogames.domoticz.R;

public class AnimationUtil {

    private static Animation enterFromAbove(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.enter_from_above);
    }

    private static Animation exitToAbove(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.exit_to_above);
    }

    private static Animation enterFromRight(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
    }

    private static Animation exitToRight(Context mContext) {
        return AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    /**
     * Returns the default opening log row animation
     *
     * @param context Context
     * @return the animation
     */
    public static Animation getLogRowAnimationOpen(Context context) {
        return enterFromRight(context);
    }

    /**
     * Returns the default closing log row animation
     *
     * @param context Context
     * @return the animation
     */
    public static Animation getLogRowAnimationClose(Context context) {
        return exitToRight(context);
    }
}