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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import nl.hnogames.domoticz.Containers.GraphPointInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.UsefulBits;

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
    private ComboLineColumnChartView chart;
    private View root;
    private Integer[] selectedFilters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Bundle data = getActivity().getIntent().getExtras();
        if (data != null) {
            idx = data.getInt("IDX");
            range = data.getString("RANGE");
            type = data.getString("TYPE");
            String title = data.getString("TITLE");
            if (!UsefulBits.isEmpty(title)) {
                setTitle(title);
            }
            steps = data.getInt("STEPS", 1);
        }
    }

    public void setTitle(String title) {
        axisYLabel = title;
        getActionBar().setTitle(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_graph, null);
        chart = (ComboLineColumnChartView) root.findViewById(R.id.chart);

        getGraphs();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void getGraphs() {
        if (mDomoticz == null)
            mDomoticz = new Domoticz(context, null);

        mDomoticz.getGraphData(idx, range, type, new GraphDataReceiver() {
            @Override
            public void onReceive(ArrayList<GraphPointInfo> grphPoints) {
                mGraphList = grphPoints;
                ComboLineColumnChartData columnData = generateData(root);
                chart.setComboLineColumnChartData(columnData);
                setViewPort(chart);

                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onError(Exception error) {
                // Let's check if were still attached to an activity
                if (isAdded()) {
                    ((GraphActivity) getActivity()).noGraphFound();
                }
            }
        });
    }

    //The viewport adds a margin to the top and bottom
    private void setViewPort(ComboLineColumnChartView chart) {
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.top += (v.height() * 0.20f);
        if (!(v.bottom <= 1))
            v.bottom -= (v.height() * 0.50f);
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
        chart.setViewportCalculationEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public ActionBar getActionBar() {
        return ((AppCompatActivity) context).getSupportActionBar();
    }

    @Override
    public void onConnectionOk() {
        if (getView() != null) {
            getGraphs();
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private ComboLineColumnChartData generateData(View view) {
        List<Line> lines = new ArrayList<>();

        List<PointValue> values = new ArrayList<>();
        List<PointValue> valuesse = new ArrayList<>();
        List<PointValue> valueshu = new ArrayList<>();
        List<PointValue> valuesba = new ArrayList<>();
        List<PointValue> valuesc = new ArrayList<>();
        List<PointValue> valuesv = new ArrayList<>();

        List<PointValue> valuessp = new ArrayList<>();
        List<PointValue> valuesdi = new ArrayList<>();
        List<PointValue> valuesuv = new ArrayList<>();
        List<PointValue> valuesu = new ArrayList<>();
        List<PointValue> valuesmm = new ArrayList<>();

        List<AxisValue> axisValueX = new ArrayList<>();

        int counter = 0;
        boolean addHumidity = false;
        boolean addBarometer = false;
        boolean addTemperature = false;
        boolean addSetpoint = false;
        boolean addCounter = false;
        boolean addPercentage = false;
        boolean addSunPower = false;
        boolean addDirection = false;
        boolean addSpeed = false;
        boolean addRain = false;
        boolean addUsage = false;
        boolean onlyDate = false;
        Calendar mydate = Calendar.getInstance();

        int stepcounter = 0;
        for (GraphPointInfo g : this.mGraphList) {
            stepcounter++;
            if (stepcounter == this.steps) {
                stepcounter = 0;

                try {

                    if (!Float.isNaN(g.getTemperature())) {
                        addTemperature = true;
                        values.add(new PointValue(counter, g.getTemperature()));
                    }

                    if (!Float.isNaN(g.getSetPoint())) {
                        addSetpoint = true;
                        valuesse.add(new PointValue(counter, g.getSetPoint()));
                    }

                    if (g.getBarometer() != null && g.getBarometer().length() > 0) {
                        addBarometer = true;
                        try {
                            valuesba.add(new PointValue(counter, Integer.parseInt(g.getBarometer())));
                        } catch (Exception ex) {
                            valuesba.add(new PointValue(counter, Float.parseFloat(g.getBarometer())));
                        }
                    }

                    if (g.getHumidity() != null && g.getHumidity().length() > 0) {
                        addHumidity = true;
                        try {
                            valueshu.add(new PointValue(counter, Integer.parseInt(g.getHumidity())));
                        } catch (Exception ex) {
                            valuesba.add(new PointValue(counter, Float.parseFloat(g.getHumidity())));
                        }
                    }

                    if (g.getPercentage() != null && g.getPercentage().length() > 0) {
                        addPercentage = true;
                        valuesv.add(new PointValue(counter, Float.parseFloat(g.getPercentage())));
                    }

                    if (g.getCounter() != null && g.getCounter().length() > 0) {
                        addCounter = true;
                        valuesc.add(new PointValue(counter, Float.parseFloat(g.getCounter())));
                    }

                    if (g.getSpeed() != null && g.getSpeed().length() > 0) {
                        addSpeed = true;
                        valuessp.add(new PointValue(counter, Float.parseFloat(g.getSpeed())));
                    }

                    if (g.getDirection() != null && g.getDirection().length() > 0) {
                        addDirection = true;
                        valuesdi.add(new PointValue(counter, Float.parseFloat(g.getDirection())));
                    }

                    if (g.getSunPower() != null && g.getSunPower().length() > 0) {
                        addSunPower = true;
                        valuesuv.add(new PointValue(counter, Float.parseFloat(g.getSunPower())));
                    }

                    if (g.getUsage() != null && g.getUsage().length() > 0) {
                        addUsage = true;
                        valuesu.add(new PointValue(counter, Float.parseFloat(g.getUsage())));
                    }

                    if (g.getRain() != null && g.getRain().length() > 0) {
                        addRain = true;
                        valuesmm.add(new PointValue(counter, Float.parseFloat(g.getRain())));
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        mydate.setTime(sdf.parse(g.getDateTime()));
                    } catch (ParseException e) {
                        onlyDate = true;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        mydate.setTime(sdf.parse(g.getDateTime()));
                    }

                    String label;
                    if (!onlyDate) {
                        label = String.valueOf(mydate.get(Calendar.HOUR_OF_DAY))
                                + ":"
                                + String.valueOf(
                                mydate.get(Calendar.MINUTE));
                    } else {
                        label = (mydate.get(Calendar.MONTH) + 1) + "/"
                                + mydate.get(Calendar.DAY_OF_MONTH);
                    }

                    axisValueX.add(new AxisValue(counter, label
                            .toCharArray()));
                    counter++;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        boolean setCubic = false;

        //setCubic seems bugged in HelloCharts library
        //if(range.equals(Domoticz.Graph.Range.MONTH) || range.equals(Domoticz.Graph.Range.YEAR))
        //    setCubic=true;
        if ((addTemperature && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_temperature)).getText().toString()))) {
            lines.add(new Line(values)
                    .setColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
            if ((addSetpoint && !enableFilters) ||
                    (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_set_point)).getText().toString()))) {
                lines.add(new Line(valuesse)
                        .setColor(ContextCompat.getColor(context, R.color.material_pink_600))
                        .setCubic(setCubic)
                        .setHasLabels(false)
                        .setHasLines(true)
                        .setHasPoints(false));
            }
        }

        if ((addHumidity && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_humidity)).getText().toString()))) {

            lines.add(new Line(valueshu)
                    .setColor(ContextCompat.getColor(context, R.color.material_orange_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addBarometer && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_barometer)).getText().toString()))) {

            lines.add(new Line(valuesba)
                    .setColor(ContextCompat.getColor(context, R.color.material_green_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addCounter && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_counter)).getText().toString()))) {

            lines.add(new Line(valuesc)
                    .setColor(ContextCompat.getColor(context, R.color.material_indigo_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addPercentage && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_percentage)).getText().toString()))) {

            lines.add(new Line(valuesv)
                    .setColor(ContextCompat.getColor(context, R.color.material_yellow_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addDirection && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_direction)).getText().toString()))) {

            lines.add(new Line(valuesdi)
                    .setColor(ContextCompat.getColor(context, R.color.material_green_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addSunPower && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_sunpower)).getText().toString()))) {

            lines.add(new Line(valuesuv)
                    .setColor(ContextCompat.getColor(context, R.color.material_deep_purple_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addSpeed && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_speed)).getText().toString()))) {

            lines.add(new Line(valuessp)
                    .setColor(ContextCompat.getColor(context, R.color.material_amber_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addUsage && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_usage)).getText().toString()))) {

            lines.add(new Line(valuesu)
                    .setColor(ContextCompat.getColor(context, R.color.material_orange_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if ((addRain && !enableFilters) ||
                (filterLabels != null && filterLabels.contains(((TextView) view.findViewById(R.id.legend_rain)).getText().toString()))) {

            lines.add(new Line(valuesmm)
                    .setColor(ContextCompat.getColor(context, R.color.material_light_green_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (lines.size() > 1) {
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
        }

        LineChartData lineChartData = new LineChartData(lines);
        ComboLineColumnChartData data = new ComboLineColumnChartData(null, lineChartData);
        Axis axisX = new Axis().setValues(axisValueX).setHasLines(true);
        Axis axisY = new Axis().setHasLines(true);
        axisX.setMaxLabelChars(5);
        axisX.setName("Date");
        axisY.setName(axisYLabel);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        return data;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (lineLabels != null && lineLabels.size() > 1) {
            inflater.inflate(R.menu.menu_graph_sort, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
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
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                selectedFilters = which;
                                enableFilters = true;

                                if (text != null && text.length > 0) {
                                    filterLabels = new ArrayList<>();

                                    //set filters
                                    for (CharSequence c : text)
                                        filterLabels.add((String) c);

                                    ComboLineColumnChartData columnData = generateData(root);
                                    chart.setComboLineColumnChartData(columnData);
                                    setViewPort(chart);
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