
package nl.hnogames.domoticzapi.Containers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Container for Energy Dashboard configuration
 * Represents the energy dashboard device IDs and settings from Domoticz
 */
public class EnergyDashboardInfo implements Serializable {

    private boolean convertWaterM3ToLiter;
    private boolean displayFlowWithLines;
    private boolean displayOutsideTemp;
    private boolean displayTime;
    private boolean useCustomIcons;

    private String extra1Field;
    private String extra1Icon;
    private String extra2Field;
    private String extra2Icon;
    private String extra3Field;
    private String extra3Icon;

    private int idBatterySoc;
    private int idBatteryWatt;
    private int idExtra1;
    private int idExtra2;
    private int idExtra3;
    private int idGas;
    private int idOutsideTempSensor;
    private int idP1;
    private int idSolar;
    private int idTextSensor;
    private int idWater;

    public EnergyDashboardInfo(JSONObject row) throws JSONException {
        mapFields(row);
    }

    public EnergyDashboardInfo(String json) {
        try {
            mapFields(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mapFields(JSONObject row) throws JSONException {
        // Check if we have the result object containing ESettings
        if (row.has("result")) {
            JSONObject result = row.getJSONObject("result");

            if (result.has("ESettings")) {
                JSONObject eSettings = result.getJSONObject("ESettings");

                // Boolean fields
                if (eSettings.has("ConvertWaterM3ToLiter"))
                    convertWaterM3ToLiter = eSettings.getBoolean("ConvertWaterM3ToLiter");
                if (eSettings.has("DisplayFlowWithLines"))
                    displayFlowWithLines = eSettings.getBoolean("DisplayFlowWithLines");
                if (eSettings.has("DisplayOutsideTemp"))
                    displayOutsideTemp = eSettings.getBoolean("DisplayOutsideTemp");
                if (eSettings.has("DisplayTime"))
                    displayTime = eSettings.getBoolean("DisplayTime");
                if (eSettings.has("UseCustomIcons"))
                    useCustomIcons = eSettings.getBoolean("UseCustomIcons");

                // String fields
                if (eSettings.has("Extra1Field"))
                    extra1Field = eSettings.getString("Extra1Field");
                if (eSettings.has("Extra1Icon"))
                    extra1Icon = eSettings.getString("Extra1Icon");
                if (eSettings.has("Extra2Field"))
                    extra2Field = eSettings.getString("Extra2Field");
                if (eSettings.has("Extra2Icon"))
                    extra2Icon = eSettings.getString("Extra2Icon");
                if (eSettings.has("Extra3Field"))
                    extra3Field = eSettings.getString("Extra3Field");
                if (eSettings.has("Extra3Icon"))
                    extra3Icon = eSettings.getString("Extra3Icon");

                // Integer fields (device IDs)
                if (eSettings.has("idBatterySoc"))
                    idBatterySoc = eSettings.getInt("idBatterySoc");
                if (eSettings.has("idBatteryWatt"))
                    idBatteryWatt = eSettings.getInt("idBatteryWatt");
                if (eSettings.has("idExtra1"))
                    idExtra1 = eSettings.getInt("idExtra1");
                if (eSettings.has("idExtra2"))
                    idExtra2 = eSettings.getInt("idExtra2");
                if (eSettings.has("idExtra3"))
                    idExtra3 = eSettings.getInt("idExtra3");
                if (eSettings.has("idGas"))
                    idGas = eSettings.getInt("idGas");
                if (eSettings.has("idOutsideTempSensor"))
                    idOutsideTempSensor = eSettings.getInt("idOutsideTempSensor");
                if (eSettings.has("idP1"))
                    idP1 = eSettings.getInt("idP1");
                if (eSettings.has("idSolar"))
                    idSolar = eSettings.getInt("idSolar");
                if (eSettings.has("idTextSensor"))
                    idTextSensor = eSettings.getInt("idTextSensor");
                if (eSettings.has("idWater"))
                    idWater = eSettings.getInt("idWater");
            }
        }
    }

    // Getters for boolean fields
    public boolean isConvertWaterM3ToLiter() {
        return convertWaterM3ToLiter;
    }

    public boolean isDisplayFlowWithLines() {
        return displayFlowWithLines;
    }

    public boolean isDisplayOutsideTemp() {
        return displayOutsideTemp;
    }

    public boolean isDisplayTime() {
        return displayTime;
    }

    public boolean isUseCustomIcons() {
        return useCustomIcons;
    }

    // Getters for string fields
    public String getExtra1Field() {
        return extra1Field;
    }

    public String getExtra1Icon() {
        return extra1Icon;
    }

    public String getExtra2Field() {
        return extra2Field;
    }

    public String getExtra2Icon() {
        return extra2Icon;
    }

    public String getExtra3Field() {
        return extra3Field;
    }

    public String getExtra3Icon() {
        return extra3Icon;
    }

    // Getters for integer fields (device IDs)
    public int getIdBatterySoc() {
        return idBatterySoc;
    }

    public int getIdBatteryWatt() {
        return idBatteryWatt;
    }

    public int getIdExtra1() {
        return idExtra1;
    }

    public int getIdExtra2() {
        return idExtra2;
    }

    public int getIdExtra3() {
        return idExtra3;
    }

    public int getIdGas() {
        return idGas;
    }

    public int getIdOutsideTempSensor() {
        return idOutsideTempSensor;
    }

    public int getIdP1() {
        return idP1;
    }

    public int getIdSolar() {
        return idSolar;
    }

    public int getIdTextSensor() {
        return idTextSensor;
    }

    public int getIdWater() {
        return idWater;
    }

    @Override
    public String toString() {
        return "EnergyDashboardInfo{" +
                "idP1=" + idP1 +
                ", idSolar=" + idSolar +
                ", idBatteryWatt=" + idBatteryWatt +
                ", idBatterySoc=" + idBatterySoc +
                ", idGas=" + idGas +
                ", idWater=" + idWater +
                ", idOutsideTempSensor=" + idOutsideTempSensor +
                ", displayTime=" + displayTime +
                ", displayOutsideTemp=" + displayOutsideTemp +
                '}';
    }
}

