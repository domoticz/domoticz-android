
package nl.hnogames.domoticz.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class WearUsefulBits {

    public static boolean isEmpty(String string) {
        //noinspection SimplifiableIfStatement
        if (string != null)
            return string.equalsIgnoreCase("")
                    || string.isEmpty()
                    || string.length() <= 0;
        else return true;
    }

    public static boolean isEmpty(CharSequence charSequence) {
        //noinspection SimplifiableIfStatement
        if (charSequence != null)
            return charSequence.length() <= 0;
        else return true;
    }

    public static String newLine() {
        return System.getProperty("line.separator");
    }

    /**
     * @param text to be validated
     * @return if the text is base 64 encoded or not
     */
    public static boolean isBase64Encoded(String text) {
        return text.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");
    }

    /**
     * @param text input value
     * @return decode base 64 text
     */
    public static String decodeBase64(String text) {
        byte[] data = Base64.decode(text, Base64.DEFAULT);
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * @param text input value
     * @return encode base 64 text
     */
    public static String encodeBase64(String text) {
        return Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }
}