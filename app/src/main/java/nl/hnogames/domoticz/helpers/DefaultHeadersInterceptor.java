package nl.hnogames.domoticz.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DefaultHeadersInterceptor implements Interceptor {
    private Context mContext;
    private boolean useBasicAuth = false;
    private String sCookie;
    private boolean useCookie = false;

    public DefaultHeadersInterceptor(Context context) {
        this.mContext = context;
    }

    public DefaultHeadersInterceptor(Context context, String cookie) {
        this.mContext = context;
        useCookie = true;
        this.sCookie = cookie;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        builder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Connection", "keep-alive")
                //.header("Accept-Language", "en-GB,en;q=0.5")
                //.header("Upgrade-Insecure-Requests", "1")
                .header("Accept-Encoding", "gzip, deflate, br")
                //.header("X-Client-Id", getPackageName() + "-" + getVersionName() + "-" + getVersionCode() + "-" + getBuildType())
                .header("User-Agent", getPackageName() + "-" + getVersionName() + "-" + getVersionCode() + "-" + getBuildType());
        //if (useBasicAuth)
        //    builder.header("Authorization", Credentials.basic(sUsername, sPassword));
        if (useCookie)
            builder.header("Cookie", sCookie);
        return chain.proceed(builder.build());
    }

    private String getPackageName() {
        return mContext.getPackageName();
    }

    private String getVersionName() {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "na";
    }

    private String getVersionCode() {
        try {
            return String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "na";
    }

    private String getBuildType() {
        boolean isDebuggable = (0 != (mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        if (isDebuggable) {
            return "debug";
        } else {
            return "release";
        }
    }
}