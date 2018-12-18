package nl.hnogames.domoticz.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.Helpers.DefaultHeadersInterceptor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.platform.Platform;

public class PicassoUtil {
    public static final String TAG = PicassoUtil.class.getSimpleName();
    private static final String BIG_CACHE_PATH = "picasso-big-cache";
    private static final int MIN_DISK_CACHE_SIZE = 16 * 1024 * 1024; // 16MB
    private static final int MAX_DISK_CACHE_SIZE = 512 * 1024 * 1024; // 512MB
    private static final float MAX_AVAILABLE_SPACE_USE_FRACTION = 0.9f;
    private static final float MAX_TOTAL_SPACE_USE_FRACTION = 0.25f;

    private static File createDefaultCacheDir(Context context, String path) {
        File cacheDir = context.getApplicationContext().getExternalCacheDir();
        if (cacheDir == null)
            cacheDir = context.getApplicationContext().getCacheDir();
        File cache = new File(cacheDir, path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    /**
     * Calculates bonded min max cache size. Min value is
     * {@link #MIN_DISK_CACHE_SIZE}
     *
     * @param dir cache dir
     * @return disk space in bytes
     */
    private static long calculateDiskCacheSize(File dir) {
        long size = Math.min(calculateAvailableCacheSize(dir), MAX_DISK_CACHE_SIZE);
        return Math.max(size, MIN_DISK_CACHE_SIZE);
    }

    /**
     * Calculates minimum of available or total fraction of disk space
     *
     * @param dir
     * @return space in bytes
     */
    @SuppressLint("NewApi")
    private static long calculateAvailableCacheSize(File dir) {
        long size = 0;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long totalBytes;
            long availableBytes;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                long blockSize = statFs.getBlockSizeLong();
                availableBytes = statFs.getAvailableBlocksLong() * blockSize;
                totalBytes = statFs.getBlockCountLong() * blockSize;
            } else {
                availableBytes = statFs.getAvailableBytes();
                totalBytes = statFs.getTotalBytes();
            }
            // Target at least 90% of available or 25% of total space
            size = (long) Math.min(availableBytes * MAX_AVAILABLE_SPACE_USE_FRACTION, totalBytes
                    * MAX_TOTAL_SPACE_USE_FRACTION);
        } catch (IllegalArgumentException ignored) {
            // ignored
        }
        return size;
    }

    public Picasso getPicasso(Context context, final String username, final String password) {
        OkHttpClient okHttpClient = providesOkHttpClient(context, new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .request("Request")
                .response("Response")
                .build(), username, password);
        OkHttp3Downloader okHttpDownloader = providesPicassoOkHttpClient(okHttpClient);
        Picasso picasso = providesCustomPicasso(context, okHttpDownloader);
        return picasso;
    }

    public Picasso getPicasso(Context context, final String cookie) {
        OkHttpClient okHttpClient = providesOkHttpClient(context, new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .request("Request")
                .response("Response")
                .build(), cookie);
        OkHttp3Downloader okHttpDownloader = providesPicassoOkHttpClient(okHttpClient);
        Picasso picasso = providesCustomPicasso(context, okHttpDownloader);
        return picasso;
    }

    Picasso providesCustomPicasso(Context context, OkHttp3Downloader okHttpDownloader) {
        return new Picasso.Builder(context)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Log.e(TAG, "onImageLoadFailed: uri: " + uri, exception);
                    }
                })
                .downloader(okHttpDownloader)
                //.memoryCache(new LruCache(context))
                .executor(Executors.newSingleThreadExecutor())//avoid OutOfMemoryError
                .build();
    }

    public OkHttp3Downloader providesPicassoOkHttpClient(OkHttpClient okHttpClient) {
        return new OkHttp3Downloader(okHttpClient);
    }

    public OkHttpClient providesOkHttpClient(Context context, Interceptor loggingInterceptor) {
        File cacheDir = createDefaultCacheDir(context, BIG_CACHE_PATH);
        long cacheSize = calculateDiskCacheSize(cacheDir);
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        SSLSocketFactory sslSocketFactory = null;
        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");

            if (sslContext != null) {
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                sslSocketFactory = sslContext.getSocketFactory();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .addNetworkInterceptor(new DefaultHeadersInterceptor(context))
                .addInterceptor(new DefaultHeadersInterceptor(context))
                .addInterceptor(loggingInterceptor)
                //.cache(new Cache(cacheDir, cacheSize))
                .build();
    }

    public OkHttpClient providesOkHttpClient(Context context, Interceptor loggingInterceptor, String username, String password) {
        File cacheDir = createDefaultCacheDir(context, BIG_CACHE_PATH);
        long cacheSize = calculateDiskCacheSize(cacheDir);
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        SSLSocketFactory sslSocketFactory = null;
        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");

            if (sslContext != null) {
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                sslSocketFactory = sslContext.getSocketFactory();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .addNetworkInterceptor(new DefaultHeadersInterceptor(context, username, password))
                .addInterceptor(new DefaultHeadersInterceptor(context, username, password))
                .addInterceptor(loggingInterceptor)
                //.cache(new Cache(cacheDir, cacheSize))
                .build();
    }

    public OkHttpClient providesOkHttpClient(Context context, Interceptor loggingInterceptor, String cookie) {
        File cacheDir = createDefaultCacheDir(context, BIG_CACHE_PATH);
        long cacheSize = calculateDiskCacheSize(cacheDir);
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        SSLSocketFactory sslSocketFactory = null;
        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");

            if (sslContext != null) {
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                sslSocketFactory = sslContext.getSocketFactory();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .addNetworkInterceptor(new DefaultHeadersInterceptor(context, cookie))
                .addInterceptor(new DefaultHeadersInterceptor(context, cookie))
                .addInterceptor(loggingInterceptor)
                //.cache(new Cache(cacheDir, cacheSize))
                .build();
    }

    @SuppressLint("BadHostnameVerifier")
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}