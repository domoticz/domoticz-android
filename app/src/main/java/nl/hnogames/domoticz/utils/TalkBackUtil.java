
package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

public class TalkBackUtil {
    private TextToSpeech oTalkBack;
    private Context mContext;
    private InitListener initListener;

    public void Init(Context context, InitListener i) {
        initListener = i;
        this.mContext = context;
        oTalkBack = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    initListener.onInit(status);
                }
            }
        });
    }

    public void Init(Context context, final Locale l, InitListener i) {
        initListener = i;
        this.mContext = context;
        oTalkBack = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    oTalkBack.setLanguage(l);
                    initListener.onInit(status);
                }
            }
        });
    }

    public void Talk(String text) {
        if (UsefulBits.isEmpty(text) || mContext == null)
            return;

        if (oTalkBack == null)
            return; //should first call init

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String utteranceId = mContext.hashCode() + "";
            oTalkBack.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            oTalkBack.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    public void Stop() {
        if (oTalkBack != null) {
            oTalkBack.stop();
            oTalkBack.shutdown();
        }
    }

    public interface InitListener {
        void onInit(int status);
    }

}
