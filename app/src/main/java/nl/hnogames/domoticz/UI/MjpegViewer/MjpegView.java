package nl.hnogames.domoticz.UI.MjpegViewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by YJ Kim on 2017-02-23.
 */

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
    public final static int SIZE_FIT = 1;

    ;
    public final static int SIZE_FULL = 2;
    private SurfaceHolder mHolder;
    private MjpegViewThread thread = null;
    private State state;
    private Handler handler = null;
    private MjpegInputStream mIn = null;
    private DoRead StreamReader = null;
    private boolean suspending = false;

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        state = State.DISCONNECTED;
        setKeepScreenOn(true);
    }

    private void SetThread() {
        Log.d("State : ", "Set Thread");
        if (thread == null) {
            thread = new MjpegViewThread(mHolder, this);
            thread.displayMode = this.SIZE_FIT;
            thread.mCallback = new MjpegCallback() {
                @Override
                public void onStateChange(int s) {
                    state = State.values()[s];
                    if (state == State.CONNECTION_ERROR) {
                        mIn = null;
                    }
                    alertState();
                }
            };
        }
    }

    public void Stop() {
        Log.d("State : ", "Stop");
        state = State.STOPPING_PROGRESS;
        alertState();

        if (thread != null) {
            thread.StopRunning();
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
            thread = null;
        }
        if (mIn != null) {
            try {
                mIn.close();
            } catch (IOException e) {
            }
        }
        mIn = null;

        try {
            StreamReader.cancel(true);
            suspending = false;
        } catch (Exception e) {
        }

        state = State.DISCONNECTED;
        alertState();
    }

    public void SetDisplayMode(int s) {
        thread.displayMode = s;
    }

    public void Start(String url, Handler parent_handler) {
        handler = parent_handler;
        Stop();
        if (!suspending) {
            SetThread();
            StreamReader = new DoRead();
            StreamReader.execute(url);
        }
    }

    public void Start(String url) {
        Stop();
        if (!suspending) {
            SetThread();
            StreamReader = new DoRead();
            StreamReader.execute(url);
        }
    }

    private void alertState() {
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.obj = state.toString();
            handler.sendMessage(msg);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (thread != null) {
            thread.SetViewSize(width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (thread != null) {
            thread.SetViewSize(getWidth(), getHeight());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    // Mjpeg State
    public enum State {
        DISCONNECTED,
        CONNECTION_PROGRESS,
        CONNECTED,
        CONNECTION_ERROR,
        STOPPING_PROGRESS
    }

    public class DoRead extends AsyncTask<String, Void, Void> {
        public void alertState() {
            if (handler != null) {
                Message msg = handler.obtainMessage();
                msg.obj = state.toString();
                handler.sendMessage(msg);
            }
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                suspending = true;
                state = State.CONNECTION_PROGRESS;
                alertState();

                URL obj = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() != 200) {
                    state = State.CONNECTION_ERROR;
                    alertState();
                    return null;
                }

                // Connection success
                state = State.CONNECTED;
                alertState();

                mIn = new MjpegInputStream(conn.getInputStream());
                thread.setInputStream(mIn);
                return null;
            } catch (Exception e) {
                state = State.CONNECTION_ERROR;
                alertState();

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if (state == State.CONNECTED) {
                thread.start();
                suspending = false;
            }
        }
    }
}


