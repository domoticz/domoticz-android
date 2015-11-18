package nl.hnogames.domoticz.Utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import nl.hnogames.domoticz.R;

@SuppressWarnings("unused")
public class VolleyUtil {

    public static final String VOLLEY_ERROR_HANDSHAKE_FAILED = "Handshake failed";
    public static final String VOLLEY_ERROR_TIMEOUT_ERROR = "com.android.volley.TimeoutError";
    private static final String TAG = VolleyUtil.class.getSimpleName();
    private Context mContext;

    public VolleyUtil(Context mContext) {
        this.mContext = mContext;
    }

    public String getVolleyErrorMessage(VolleyError volleyError) {

        String errorMessage = "Unhandled error";

        if (volleyError instanceof AuthFailureError) {
            Log.e(TAG, "Authentication failure");
            errorMessage = mContext.getString(R.string.error_authentication);

        } else if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
            Log.e(TAG, "Timeout or no connection");
            String detail;

            if (volleyError.getCause() != null) detail = volleyError.getCause().getMessage();
            else {
                detail = volleyError.toString();
            }
            errorMessage = mContext.getString(R.string.error_timeout) + "\n" + detail;

        } else if (volleyError instanceof ServerError) {
            Log.e(TAG, "Server error");
            errorMessage = mContext.getString(R.string.error_server);

        } else if (volleyError instanceof NetworkError) {
            Log.e(TAG, "Network error");

            NetworkResponse networkResponse = volleyError.networkResponse;
            if (networkResponse != null) {
                Log.e("Status code", String.valueOf(networkResponse.statusCode));
                errorMessage = String.format(mContext.getString(R.string.error_network), networkResponse.statusCode);
            }
        } else if (volleyError instanceof ParseError) {
            Log.e(TAG, "Parse failure");
            errorMessage = mContext.getString(R.string.error_parse);
        }

        return errorMessage;
    }
}