package nl.hnogames.domoticz.helpers;

import java.io.IOException;

import okhttp3.Credentials;
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
        String credential = Credentials.basic(username, password);
        Request compressedRequest = chain.request().newBuilder()
                .header("Authorization", credential)
                .build();
        return chain.proceed(compressedRequest);
    }
}