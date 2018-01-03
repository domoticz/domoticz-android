/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.GraphPointInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.GraphDataReceiver;


public class Graph extends Fragment implements DomoticzFragmentListener {

    @SuppressWarnings("unused")
    private static final String TAG = Graph.class.getSimpleName();

    private Context context;
    private Domoticz mDomoticz;

    private int idx = 0;
    private String range = "day";
    private String type = "temp";
    private int steps = 1;
    private String axisYLabel = "Temp";

    private boolean enableFilters = false;
    private List<String> lineLabels;
    private List<String> filterLabels;

    private ArrayList<GraphPointInfo> mGraphList;
    private LineChart chart;
    private View root;
    private Integer[] selectedFilters;
    private SharedPrefUtil mSharedPrefs;

    @Override
    public void onConnectionFailed() {
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        mDomoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        mSharedPrefs = new SharedPrefUtil(context);
    }

    @Override
    @DebugLog
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Bundle data = getActivity().getIntent().getExtras();
        if (data != null) {
            idx = data.getInt("IDX");
            range = data.getString("RANGE");
            type = data.getString("TYPE");
            axisYLabel = data.getString("TITLE");
            steps = data.getInt("STEPS", 1);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    @DebugLog
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_graph, null);

        chart = root.findViewById(R.id.chart);
        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisLeft();

        if (mSharedPrefs.darkThemeEnabled()) {
            xAxis.setTextColor(Color.WHITE);
            yAxis.setTextColor(Color.WHITE);
            chart.getLegend().setTextColor(Color.WHITE);
            //chart.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));
            chart.setDrawGridBackground(true);
        } else {
            //chart.setBackgroundColor(Color.WHITE);
            chart.setDrawGridBackground(true);
        }

        chart.getDescription().setEnabled(false);
        xAxis.setDrawGridLines(false); // no grid lines
        chart.getAxisRight().setEnabled(false); // no right axis
        chart.setDragDecelerationFrictionCoef(0.9f);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        if (range.equals("day")) {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) value);
                    return String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.MINUTE));
                }
            });
        } else {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) value);

                    //int mYear = calendar.get(Calendar.YEAR);
                    int mMonth = calendar.get(Calendar.MONTH) + 1;
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    int mHours = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinutes = calendar.get(Calendar.MINUTE);

                    String xValue;
                    if (mHours <= 0 && mMinutes <= 0)
                        xValue = String.format(Locale.getDefault(),"%02d", mHours) + ":" + String.format(Locale.getDefault(),"%02d", mMinutes);
                    else
                        xValue = mDay + "/" + mMonth + " " + String.format(Locale.getDefault(),"%02d", mHours) + ":" + String.format(Locale.getDefault(),"%02d", mMinutes);
                    return xValue;
                }
            });
        }

        xAxis.setLabelRotationAngle(90);
        xAxis.setLabelCount(15);
        getGraphs();
        return root;
    }

    @Override
    @DebugLog
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void getGraphs() {
        chart.setVisibility(View.GONE);
        new Thread() {
            @Override
            public void run() {
                if (mDomoticz == null)
                    mDomoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());

                mDomoticz.getGraphData(idx, range, type, new GraphDataReceiver() {
                    @Override
                    @DebugLog
                    public void onReceive(ArrayList<GraphPointInfo> grphPoints) {
                        try {
                            mGraphList = grphPoints;
                            LineData columnData = generateData(root);
                            if (columnData != null)
                            {
                                chart.setData(columnData);
                                chart.invalidate(); // refresh

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chart.setVisibility(View.VISIBLE);
                                        chart.animateX(1000);

                                        if (getActivity() != null)
                                            getActivity().invalidateOptionsMenu();
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            if(ex.getMessage() != null)
                                Log.e(this.getClass().getSimpleName(), ex.getMessage());
                        }
                    }

                    @Override
                    @DebugLog
                    public void onError(Exception ex) {
                        // Let's check if were still attached to an activity
                        if (isAdded()) {
                            ((GraphActivity) getActivity()).noGraphFound();
                        }
                    }
                });
            }
        }.start();
    }

    public ActionBar getActionBar() {
        try {
            return ((AppCompatActivity) getActivity().getApplicationContext()).getSupportActionBar();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    @DebugLog
    public void onConnectionOk() {
        if (getView() != null) {
            getGraphs();
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private LineData generateData(View view) {
        try {
            List<LineDataSet> entries = new ArrayList<>();

            List<Entry> valuest = new ArrayList<>();
            List<Entry> valuestMin = new ArrayList<>();
            List<Entry> valuestMax = new ArrayList<>();

            List<Entry> valuesse = new ArrayList<>();
            List<Entry> valueshu = new ArrayList<>();
            List<Entry> valuesba = new ArrayList<>();
            List<Entry> valuesc = new ArrayList<>();

            List<Entry> valuesv = new ArrayList<>();
            List<Entry> valuesv2 = new ArrayList<>();
            List<Entry> valuesvMin = new ArrayList<>();
            List<Entry> valuesvMax = new ArrayList<>();

            List<Entry> valueeu = new ArrayList<>();
            List<Entry> valueeg = new ArrayList<>();

            List<Entry> valuessp = new ArrayList<>();
            List<Entry> valuesdi = new ArrayList<>();
            List<Entry> valuesuv = new ArrayList<>();
            List<Entry> valuesu = new ArrayList<>();
            List<Entry> valuesmm = new ArrayList<>();

            List<Entry> valuesco2 = new ArrayList<>();
            List<Entry> valuesco2min = new ArrayList<>();
            List<Entry> valuesco2max = new ArrayList<>();

            List<Entry> valuesLux = new ArrayList<>();
            List<Entry> valuesLuxmin = new ArrayList<>();
            List<Entry> valuesLuxmax = new ArrayList<>();
            List<Entry> valuesLuxAvg = new ArrayList<>();

            boolean addHumidity = false;
            boolean addBarometer = false;
            boolean addTemperature = false;
            boolean addTemperatureRange = false;
            boolean addSetpoint = false;
            boolean addCounter = false;
            boolean addPercentage = false;
            boolean addSecondPercentage = false;
            boolean addPercentageRange = false;
            boolean addSunPower = false;
            boolean addDirection = false;
            boolean addSpeed = false;
            boolean addRain = false;
            boolean addCO2 = false;
            boolean addCO2Min = false;
            boolean addCO2Max = false;
            boolean addUsage = false;
            boolean addPowerUsage = false;
            boolean addPowerDelivery = false;
            boolean addLux = false;
            boolean addLuxMin = false;
            boolean addLuxMax = false;
            boolean addLuxAvg = false;

            Calendar mydate = Calendar.getInstance();

            int stepcounter = 0;
            for (GraphPointInfo g : this.mGraphList) {
                stepcounter++;
                if (stepcounter == this.steps) {
                    stepcounter = 0;

                    try {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            mydate.setTime(sdf.parse(g.getDateTime()));
                        } catch (ParseException e) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            mydate.setTime(sdf.parse(g.getDateTime()));
                        }

                        if (!Float.isNaN(g.getTemperature())) {
                            addTemperature = true;
                            valuest.add(new Entry(mydate.getTimeInMillis(), g.getTemperature()));

                            if (g.hasTemperatureRange()) {
                                addTemperatureRange = true;
                                valuestMax.add(new Entry(mydate.getTimeInMillis(), g.getTemperatureMax()));
                                valuestMin.add(new Entry(mydate.getTimeInMillis(), g.getTemperatureMin()));
                            }
                        }

                        if (!Float.isNaN(g.getSetPoint())) {
                            addSetpoint = true;
                            valuesse.add(new Entry(mydate.getTimeInMillis(), g.getSetPoint()));
                        }

                        if (g.getBarometer() != null && g.getBarometer().length() > 0) {
                            addBarometer = true;
                            try {
                                valuesba.add(new Entry(mydate.getTimeInMillis(), Integer.parseInt(g.getBarometer())));
                            } catch (Exception ex) {
                                valuesba.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getBarometer())));
                            }
                        }

                        if (g.getHumidity() != null && g.getHumidity().length() > 0) {
                            addHumidity = true;
                            try {
                                valueshu.add(new Entry(mydate.getTimeInMillis(), Integer.parseInt(g.getHumidity())));
                            } catch (Exception ex) {
                                valuesba.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getHumidity())));
                            }
                        }

                        if (g.getValue() != null && g.getValue().length() > 0) {
                            addPercentage = true;
                            valuesv.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getValue())));
                            if (g.hasValueRange()) {
                                addPercentageRange = true;
                                valuesvMin.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getValueMin())));
                                valuesvMax.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getValueMax())));
                            }
                        }

                        if (g.getSecondValue() != null && g.getSecondValue().length() > 0) {
                            addSecondPercentage = true;
                            valuesv2.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getSecondValue())));
                        }

                        if (g.getPowerDelivery() != null && g.getPowerDelivery().length() > 0) {
                            addPowerDelivery = true;
                            valueeg.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getPowerDelivery())));
                        }

                        if (g.getPowerUsage() != null && g.getPowerUsage().length() > 0) {
                            addPowerUsage = true;
                            valueeu.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getPowerUsage())));
                        }

                        if (g.getCounter() != null && g.getCounter().length() > 0) {
                            addCounter = true;
                            valuesc.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getCounter())));
                        }

                        if (g.getSpeed() != null && g.getSpeed().length() > 0) {
                            addSpeed = true;
                            valuessp.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getSpeed())));
                        }

                        if (g.getDirection() != null && g.getDirection().length() > 0) {
                            addDirection = true;
                            valuesdi.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getDirection())));
                        }

                        if (g.getSunPower() != null && g.getSunPower().length() > 0) {
                            addSunPower = true;
                            valuesuv.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getSunPower())));
                        }

                        if (g.getUsage() != null && g.getUsage().length() > 0) {
                            addUsage = true;
                            valuesu.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getUsage())));
                        }

                        if (g.getRain() != null && g.getRain().length() > 0) {
                            addRain = true;
                            valuesmm.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getRain())));
                        }

                        if (g.getCo2() != null && g.getCo2().length() > 0) {
                            addCO2 = true;
                            valuesco2.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getCo2())));
                        }

                        if (g.getCo2Min() != null && g.getCo2Min().length() > 0) {
                            addCO2Min = true;
                            valuesco2min.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getCo2Min())));
                        }

                        if (g.getCo2Max() != null && g.getCo2Max().length() > 0) {
                            addCO2Max = true;
                            valuesco2max.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getCo2Max())));
                        }

                        if (g.getLux() != null && g.getLux().length() > 0) {
                            addLux = true;
                            valuesLux.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getLux())));
                        }

                        if (g.getLuxMin() != null && g.getLuxMin().length() > 0) {
                            addLuxMin = true;
                            valuesLuxmin.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getLuxMin())));
                        }

                        if (g.getLuxMax() != null && g.getLuxMax().length() > 0) {
                            addLuxMax = true;
                            valuesLuxmax.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getLuxMax())));
                        }

                        if (g.getLuxAvg() != null && g.getLuxAvg().length() > 0) {
                            addLuxAvg = true;
                            valuesLuxAvg.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getLuxAvg())));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if ((addTemperature && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_temperature)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuest, ((TextView) view.findViewById(R.id.legend_temperature)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_blue_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);

                if ((addSetpoint && !enableFilters) ||
                        (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_set_point)).getText().toString()))) {
                    dataSet = new LineDataSet(valuesse, ((TextView) view.findViewById(R.id.legend_set_point)).getText().toString()); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.material_pink_600));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    entries.add(dataSet);
                }

                if (addTemperatureRange) {
                    dataSet = new LineDataSet(valuestMax, "Max"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(R.color.md_blue_300);
                    entries.add(dataSet);

                    dataSet = new LineDataSet(valuestMin, "Min"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    dataSet.setFillAlpha(255);
                    dataSet.setFillColor(R.color.white);
                    dataSet.setDrawFilled(true);
                    entries.add(dataSet);
                }
            }

            if ((addHumidity && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_humidity)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valueshu, ((TextView) view.findViewById(R.id.legend_humidity)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_orange_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addBarometer && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_barometer)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesba, ((TextView) view.findViewById(R.id.legend_barometer)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addCounter && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_counter)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesc, ((TextView) view.findViewById(R.id.legend_counter)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_indigo_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addPowerUsage && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_powerusage)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valueeu, ((TextView) view.findViewById(R.id.legend_powerusage)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_yellow_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }
            if ((addPowerDelivery && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_powerdeliv)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valueeg, ((TextView) view.findViewById(R.id.legend_powerdeliv)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_deep_purple_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addPercentage && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_percentage)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesv, ((TextView) view.findViewById(R.id.legend_percentage)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_yellow_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);

                if (addPercentageRange) {
                    dataSet = new LineDataSet(valuesvMax, "Max"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(R.color.md_blue_300);
                    entries.add(dataSet);

                    dataSet = new LineDataSet(valuesvMin, "Min"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                    dataSet.setFillAlpha(255);
                    dataSet.setFillColor(R.color.white);
                    dataSet.setDrawFilled(true);
                    entries.add(dataSet);
                }
            }

            if ((addSecondPercentage && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_percentage2)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesv2, ((TextView) view.findViewById(R.id.legend_percentage2)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_orange_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addDirection && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_direction)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesdi, ((TextView) view.findViewById(R.id.legend_direction)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addSunPower && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_sunpower)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesuv, ((TextView) view.findViewById(R.id.legend_sunpower)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_deep_purple_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addSpeed && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_speed)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuessp, ((TextView) view.findViewById(R.id.legend_speed)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_amber_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addUsage && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_usage)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesu, ((TextView) view.findViewById(R.id.legend_usage)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_orange_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addRain && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_rain)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesmm, ((TextView) view.findViewById(R.id.legend_rain)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_light_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addCO2 && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_co2)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesco2, ((TextView) view.findViewById(R.id.legend_co2)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_blue_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addCO2Min && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_co2min)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesco2min, ((TextView) view.findViewById(R.id.legend_co2min)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_light_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addCO2Max && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_co2max)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesco2max, ((TextView) view.findViewById(R.id.legend_co2max)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.md_red_400));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addLux && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_Lux)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesLux, ((TextView) view.findViewById(R.id.legend_Lux)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_blue_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addLuxMin && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_Luxmin)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesLuxmin, ((TextView) view.findViewById(R.id.legend_Luxmin)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_light_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addLuxMax && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_Luxmax)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesLuxmax, ((TextView) view.findViewById(R.id.legend_Luxmax)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.md_red_400));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if ((addLuxAvg && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_LuxAvg)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesLuxAvg, ((TextView) view.findViewById(R.id.legend_LuxAvg)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.md_yellow_400));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                entries.add(dataSet);
            }

            if (entries.size() > 1) {
                if (addTemperature) {
                    (view.findViewById(R.id.legend_temperature))
                            .setVisibility(View.VISIBLE);
                    if (addSetpoint) {
                        (view.findViewById(R.id.legend_set_point))
                                .setVisibility(View.VISIBLE);
                    }
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_temperature)).getText());

                }

                if (addHumidity) {
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_humidity)).getText());
                    (view.findViewById(R.id.legend_humidity))
                            .setVisibility(View.VISIBLE);
                }

                if (addBarometer) {
                    (view.findViewById(R.id.legend_barometer))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_barometer)).getText());
                }

                if (addCounter) {
                    (view.findViewById(R.id.legend_counter))
                            .setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.legend_counter))
                            .setText(axisYLabel);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_counter)).getText());
                }

                if (addPercentage) {
                    (view.findViewById(R.id.legend_percentage))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_percentage)).getText());
                }

                if (addSecondPercentage) {
                    (view.findViewById(R.id.legend_percentage2))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_percentage2)).getText());
                }

                if (addDirection) {
                    (view.findViewById(R.id.legend_direction))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_direction)).getText());
                }

                if (addSunPower) {
                    (view.findViewById(R.id.legend_sunpower))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_sunpower)).getText());
                }

                if (addSpeed) {
                    (view.findViewById(R.id.legend_speed))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_speed)).getText());
                }

                if (addUsage) {
                    (view.findViewById(R.id.legend_usage))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_usage)).getText());
                }

                if (addPowerDelivery) {
                    (view.findViewById(R.id.legend_powerdeliv))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_powerdeliv)).getText());
                }

                if (addPowerUsage) {
                    (view.findViewById(R.id.legend_powerusage))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_powerusage)).getText());
                }

                if (addRain) {
                    (view.findViewById(R.id.legend_rain))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_rain)).getText());
                }

                if (addCO2) {
                    (view.findViewById(R.id.legend_co2))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_co2)).getText());
                }

                if (addCO2Min) {
                    (view.findViewById(R.id.legend_co2min))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_co2min)).getText());
                }

                if (addCO2Max) {
                    (view.findViewById(R.id.legend_co2max))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_co2max)).getText());
                }

                if (addLux) {
                    (view.findViewById(R.id.legend_Lux))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_Lux)).getText());
                }

                if (addLuxMin) {
                    (view.findViewById(R.id.legend_Luxmin))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_Luxmin)).getText());
                }

                if (addLuxMax) {
                    (view.findViewById(R.id.legend_Luxmax))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_Luxmax)).getText());
                }

                if (addLuxAvg) {
                    (view.findViewById(R.id.legend_LuxAvg))
                            .setVisibility(View.VISIBLE);
                    addLabelFilters((String) ((TextView) view.findViewById(R.id.legend_LuxAvg)).getText());
                }
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.addAll(entries);

            LineData lineChartData = new LineData(dataSets);
            lineChartData.setHighlightEnabled(true);
            lineChartData.setDrawValues(false);

            return lineChartData;
        } catch (Exception ex) {
            return null;
        }
    }

    private void addLabelFilters(String label) {
        if (!enableFilters) {
            if (!UsefulBits.isEmpty(label)) {
                if (lineLabels == null)
                    lineLabels = new ArrayList<>();
                if (!lineLabels.contains(label))
                    lineLabels.add(label);
            }
        }
    }

    @Override
    @DebugLog
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (lineLabels != null && lineLabels.size() > 1) {
            inflater.inflate(R.menu.menu_graph_sort, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @DebugLog
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                String[] items = new String[lineLabels.size()];
                lineLabels.toArray(items);

                new MaterialDialog.Builder(context)
                        .title(context.getString(R.string.filter))
                        .items(items)
                        .itemsCallbackMultiChoice(selectedFilters, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            @DebugLog
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                selectedFilters = which;
                                enableFilters = true;

                                if (text != null && text.length > 0) {
                                    filterLabels = new ArrayList<>();

                                    //set filters
                                    for (CharSequence c : text)
                                        filterLabels.add((String) c);

                                    LineData columnData = generateData(root);
                                    if (columnData != null)
                                    {
                                        chart.setData(columnData);
                                        chart.invalidate(); // refresh
                                        chart.setVisibility(View.VISIBLE);
                                        chart.animateX(1000);
                                        if (getActivity() != null)
                                            getActivity().invalidateOptionsMenu();
                                    }
                                } else {
                                    enableFilters = false;
                                    Toast.makeText(context, context.getString(R.string.filter_graph_empty), Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        })
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .show();
                return true;
            default:
                break;
        }

        return false;
    }
}