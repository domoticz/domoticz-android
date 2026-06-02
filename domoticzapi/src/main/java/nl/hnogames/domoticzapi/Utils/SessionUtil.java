
package nl.hnogames.domoticzapi.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class SessionUtil {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String OLD_SESSION_COOKIE = "SID";
    private static final String NEW_SESSION_COOKIE = "DMZSID";
    private static final String COOKIE_EXPIRE_KEY = "CookieExpire";
    private static final String PREF_SESSION_COOKIE = "SID";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionUtil(Context mContext) {
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    public final void clearSessionCookie() {
        editor.putString(PREF_SESSION_COOKIE, "");
        editor.commit();
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     *
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY) &&
                (headers.get(SET_COOKIE_KEY).startsWith(OLD_SESSION_COOKIE) || headers.get(SET_COOKIE_KEY).startsWith(NEW_SESSION_COOKIE))) {
            String cookie = headers.get(SET_COOKIE_KEY);
            String expires = "";

            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                cookie = splitCookie[0];
                editor.putString(PREF_SESSION_COOKIE, cookie);
                editor.commit();

                for (String s : splitCookie) {
                    if (s.indexOf("Expires") > 0) {
                        String[] splitExpires = s.split("=");
                        expires = splitExpires[1];
                        editor.putString(COOKIE_EXPIRE_KEY, expires);
                        editor.commit();
                    }
                }
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     *
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionId = prefs.getString(PREF_SESSION_COOKIE, "");
        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    /**
     * Get session cookie to headers if exists.
     */
    public final String getSessionCookie() {
        return prefs.getString(PREF_SESSION_COOKIE, "");
    }
}
