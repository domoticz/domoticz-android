/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.EventInfo;
import nl.hnogames.domoticzapi.Containers.GraphPointInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.EventReceiver;
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

    private com.fenjuly.mylibrary.SpinnerLoader mSpinner;

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
        } else {
            finish();
        }
    }

    public void finish() {
        this.finish();
    }

    @Override
    @DebugLog
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_graph, null);

        mSpinner = (com.fenjuly.mylibrary.SpinnerLoader)root.findViewById(R.id.spinner);
        mSpinner.animate();

        chart = (LineChart) root.findViewById(R.id.chart);
        xAxis = chart.getXAxis();
        yAxis = chart.getAxisLeft();

        if(mSharedPrefs.darkThemeEnabled()) {
            xAxis.setTextColor(Color.WHITE);
            yAxis.setTextColor(Color.WHITE);
            chart.getLegend().setTextColor(Color.WHITE);
            if (mSpinner != null)
                mSpinner.setPointcolor(ContextCompat.getColor(getContext(), R.color.secondary));
            chart.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));
            chart.setDrawGridBackground(true);
        }
        else{
            chart.setBackgroundColor(Color.WHITE);
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
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((long) value);

                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                int mHours = calendar.get(Calendar.HOUR);
                int mMinutes = calendar.get(Calendar.MINUTE);

                String xValue = "";
                if(mHours <= 0 && mMinutes <= 0)
                    xValue = String.format("%02d", mHours) +":"+String.format("%02d", mMinutes);
                else
                    xValue = mDay+"/"+mMonth+ " " + String.format("%02d", mHours) +":"+String.format("%02d", mMinutes);
                return xValue;
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

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
        mSpinner.setVisibility(View.VISIBLE);
        mSpinner.animate();

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
                            if (columnData == null)
                                finish();
                            else {
                                chart.setData(columnData);
                                chart.invalidate(); // refresh

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chart.setVisibility(View.VISIBLE);
                                        mSpinner.setVisibility(View.GONE);
                                        chart.animateX(1000);

                                        if (getActivity() != null)
                                            getActivity().invalidateOptionsMenu();
                                    }
                                });
                            }
                        } catch (Exception ex) {
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

    private XAxis xAxis;
    private YAxis yAxis;

    @SuppressWarnings("SpellCheckingInspection")
    private LineData generateData(View view) {
        try {
            List<LineDataSet> entries = new ArrayList<LineDataSet>();

            List<Entry> valuest = new ArrayList<>();
            List<Entry> valuestMin = new ArrayList<>();
            List<Entry> valuestMax = new ArrayList<>();

            List<Entry> valuesse = new ArrayList<>();
            List<Entry> valueshu = new ArrayList<>();
            List<Entry> valuesba = new ArrayList<>();
            List<Entry> valuesc = new ArrayList<>();

            List<Entry> valuesv = new ArrayList<>();
            List<Entry> valuesvMin = new ArrayList<>();
            List<Entry> valuesvMax = new ArrayList<>();

            List<Entry> valuessp = new ArrayList<>();
            List<Entry> valuesdi = new ArrayList<>();
            List<Entry> valuesuv = new ArrayList<>();
            List<Entry> valuesu = new ArrayList<>();
            List<Entry> valuesmm = new ArrayList<>();

            List<Entry> valuesco2 = new ArrayList<>();
            List<Entry> valuesco2min = new ArrayList<>();
            List<Entry> valuesco2max = new ArrayList<>();

            ArrayList<String> axisValueX = new ArrayList<>();

            int counter = 0;
            boolean addHumidity = false;
            boolean addBarometer = false;
            boolean addTemperature = false;
            boolean addTemperatureRange = false;
            boolean addSetpoint = false;
            boolean addCounter = false;
            boolean addPercentage = false;
            boolean addPercentageRange = false;
            boolean addSunPower = false;
            boolean addDirection = false;
            boolean addSpeed = false;
            boolean addRain = false;
            boolean addCO2 = false;
            boolean addCO2Min = false;
            boolean addCO2Max = false;
            boolean addUsage = false;
            boolean onlyDate = false;
            Calendar mydate = Calendar.getInstance();

            int stepcounter = 0;
            for (GraphPointInfo g : this.mGraphList) {
                stepcounter++;
                if (stepcounter == this.steps) {
                    stepcounter = 0;

                    try {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            mydate.setTime(sdf.parse(g.getDateTime()));
                        } catch (ParseException e) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            mydate.setTime(sdf.parse(g.getDateTime()));
                        }

                        if (!Float.isNaN(g.getTemperature())) {
                            addTemperature = true;
                            valuest.add(new Entry(mydate.getTimeInMillis(), g.getTemperature()));

                            if (g.hasTemperatureRange())
                            {
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

                        if (g.getPercentage() != null && g.getPercentage().length() > 0) {
                            addPercentage = true;
                            valuesv.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getPercentage())));
                            if(g.hasPercentageRange())
                            {
                                addPercentageRange = true;
                                valuesvMin.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getPercentageMin())));
                                valuesvMax.add(new Entry(mydate.getTimeInMillis(), Float.parseFloat(g.getPercentageMax())));
                            }
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

                        counter++;
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
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);

                if ((addSetpoint && !enableFilters) ||
                        (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_set_point)).getText().toString()))) {
                    dataSet = new LineDataSet(valuesse, ((TextView) view.findViewById(R.id.legend_set_point)).getText().toString()); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.material_pink_600));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    entries.add(dataSet);
                }

                if(addTemperatureRange)
                {
                    dataSet = new LineDataSet(valuestMax, "Max"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(R.color.md_blue_300);
                    entries.add(dataSet);

                    dataSet = new LineDataSet(valuestMin, "Min"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
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
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addBarometer && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_barometer)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesba, ((TextView) view.findViewById(R.id.legend_barometer)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addCounter && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_counter)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesc, ((TextView) view.findViewById(R.id.legend_counter)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_indigo_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addPercentage && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_percentage)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesv, ((TextView) view.findViewById(R.id.legend_percentage)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_yellow_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);

                if(addPercentageRange)
                {
                    dataSet = new LineDataSet(valuesvMax, "Max"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(R.color.md_blue_300);
                    entries.add(dataSet);

                    dataSet = new LineDataSet(valuesvMin, "Min"); // add entries to dataset
                    dataSet.setColor(ContextCompat.getColor(context, R.color.md_blue_50));
                    dataSet.setDrawCircles(false);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet.setFillAlpha(255);
                    dataSet.setFillColor(R.color.white);
                    dataSet.setDrawFilled(true);
                    entries.add(dataSet);
                }
            }

            if ((addDirection && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_direction)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesdi, ((TextView) view.findViewById(R.id.legend_direction)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addSunPower && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_sunpower)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesuv, ((TextView) view.findViewById(R.id.legend_sunpower)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_deep_purple_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addSpeed && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_speed)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuessp, ((TextView) view.findViewById(R.id.legend_speed)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_amber_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addUsage && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_usage)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesu, ((TextView) view.findViewById(R.id.legend_usage)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_orange_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addRain && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_rain)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesmm, ((TextView) view.findViewById(R.id.legend_rain)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_light_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addCO2 && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_co2)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesco2, ((TextView) view.findViewById(R.id.legend_co2)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_blue_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addCO2Min && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_co2min)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesco2min, ((TextView) view.findViewById(R.id.legend_co2min)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.material_light_green_600));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                entries.add(dataSet);
            }

            if ((addCO2Max && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_co2max)).getText().toString()))) {
                LineDataSet dataSet = new LineDataSet(valuesco2max, ((TextView) view.findViewById(R.id.legend_co2max)).getText().toString()); // add entries to dataset
                dataSet.setColor(ContextCompat.getColor(context, R.color.md_red_400));
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
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
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            for(LineDataSet s: entries)
                dataSets.add(s);

            LineData lineChartData = new LineData(dataSets);
            lineChartData.setHighlightEnabled(true);
            lineChartData.setDrawValues(false);

            //ComboLineColumnChartData data = new ComboLineColumnChartData(null, lineChartData);
            //Axis axisX = new Axis().setValues(axisValueX).setHasLines(true);
            //Axis axisY = new Axis().setHasLines(true);
            //axisX.setMaxLabelChars(5);
            //axisX.setName("Date");
            //axisY.setName(axisYLabel);
            //data.setAxisXBottom(axisX);
            //data.setAxisYLeft(axisY);

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
                                    if (columnData == null)
                                        finish();
                                    else {
                                        chart.setData(columnData);
                                        chart.invalidate(); // refresh
                                        chart.setVisibility(View.VISIBLE);
                                        mSpinner.setVisibility(View.GONE);
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