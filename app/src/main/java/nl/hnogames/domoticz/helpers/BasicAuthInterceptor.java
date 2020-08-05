package nl.hnogames.domoticz.helpers;

import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthInterceptor implements Interceptor {
    String username;
    String password;

    public BasicAuthInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String credentials = username + ":" + password;
        String auth = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Request compressedRequest = chain.request().newBuilder()
                .header("Authorization", "Basic " + auth)
                .build();
        return chain.proceed(compressedRequest);
    }
}