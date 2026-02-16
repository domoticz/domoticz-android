
package nl.hnogames.domoticzapi.Containers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class SunRiseInfo implements Serializable {
    private String AstrTwilightEnd;
    private String AstrTwilightStart;
    private String CivTwilightEnd;
    private String CivTwilightStart;
    private String DayLength;
    private String NautTwilightEnd;
    private String NautTwilightStart;
    private String ServerTime;
    private String SunAtSouth;
    private String Sunrise;
    private String Sunset;

    public SunRiseInfo(JSONObject row) throws JSONException {
        if (row.has("AstrTwilightEnd"))
            AstrTwilightEnd = (row.getString("AstrTwilightEnd"));
        if (row.has("AstrTwilightStart"))
            AstrTwilightStart = (row.getString("AstrTwilightStart"));
        if (row.has("CivTwilightEnd"))
            CivTwilightEnd = (row.getString("CivTwilightEnd"));
        if (row.has("CivTwilightStart"))
            CivTwilightStart = (row.getString("CivTwilightStart"));
        if (row.has("DayLength"))
            DayLength = (row.getString("DayLength"));
        if (row.has("NautTwilightEnd"))
            NautTwilightEnd = (row.getString("NautTwilightEnd"));
        if (row.has("NautTwilightStart"))
            NautTwilightStart = (row.getString("NautTwilightStart"));
        if (row.has("ServerTime"))
            ServerTime = (row.getString("ServerTime"));
        if (row.has("SunAtSouth"))
            SunAtSouth = (row.getString("SunAtSouth"));
        if (row.has("Sunrise"))
            Sunrise = (row.getString("Sunrise"));
        if (row.has("Sunset"))
            Sunset = (row.getString("Sunset"));
    }

    public String getAstrTwilightEnd() {
        return AstrTwilightEnd;
    }

    public void setAstrTwilightEnd(String astrTwilightEnd) {
        AstrTwilightEnd = astrTwilightEnd;
    }

    public String getAstrTwilightStart() {
        return AstrTwilightStart;
    }

    public void setAstrTwilightStart(String astrTwilightStart) {
        AstrTwilightStart = astrTwilightStart;
    }

    public String getCivTwilightEnd() {
        return CivTwilightEnd;
    }

    public void setCivTwilightEnd(String civTwilightEnd) {
        CivTwilightEnd = civTwilightEnd;
    }

    public String getCivTwilightStart() {
        return CivTwilightStart;
    }

    public void setCivTwilightStart(String civTwilightStart) {
        CivTwilightStart = civTwilightStart;
    }

    public String getDayLength() {
        return DayLength;
    }

    public void setDayLength(String dayLength) {
        DayLength = dayLength;
    }

    public String getNautTwilightEnd() {
        return NautTwilightEnd;
    }

    public void setNautTwilightEnd(String nautTwilightEnd) {
        NautTwilightEnd = nautTwilightEnd;
    }

    public String getNautTwilightStart() {
        return NautTwilightStart;
    }

    public void setNautTwilightStart(String nautTwilightStart) {
        NautTwilightStart = nautTwilightStart;
    }

    public String getServerTime() {
        return ServerTime;
    }

    public void setServerTime(String serverTime) {
        ServerTime = serverTime;
    }

    public String getSunAtSouth() {
        return SunAtSouth;
    }

    public void setSunAtSouth(String sunAtSouth) {
        SunAtSouth = sunAtSouth;
    }

    public String getSunrise() {
        return Sunrise;
    }

    public void setSunrise(String sunrise) {
        Sunrise = sunrise;
    }

    public String getSunset() {
        return Sunset;
    }

    public void setSunset(String sunset) {
        Sunset = sunset;
    }
}