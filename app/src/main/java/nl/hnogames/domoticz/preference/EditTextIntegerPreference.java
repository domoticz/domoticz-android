
package nl.hnogames.domoticz.preference;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class EditTextIntegerPreference extends EditTextPreference {

    private Integer mInteger;

    public EditTextIntegerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
    }

    public EditTextIntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
    }

    public EditTextIntegerPreference(Context context) {
        super(context);
        this.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
    }

    private static Integer parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getText() {
        return mInteger != null ? mInteger.toString() : null;
    }

    @Override
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        mInteger = parseInteger(text);
        persistString(mInteger != null ? mInteger.toString() : null);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }
}