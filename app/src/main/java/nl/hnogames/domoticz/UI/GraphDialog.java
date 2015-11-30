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

package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.view.View;

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
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import nl.hnogames.domoticz.Containers.GraphPointInfo;
import nl.hnogames.domoticz.R;

public class GraphDialog {

    private final MaterialDialog.Builder mdb;
    private ArrayList<GraphPointInfo> mGraphList;
    private String axisYLabel = "Temp";
    private String range = "day";
    private int steps = 1;

    public GraphDialog(Context mContext,
                       ArrayList<GraphPointInfo> mGraphList,
                       int layout) {
        this.mGraphList = mGraphList;

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        mdb.customView(layout, false)
                .positiveText(android.R.string.ok);
    }

    public void show() {
        mdb.title(axisYLabel);
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        ComboLineColumnChartView chart = (ComboLineColumnChartView) view.findViewById(R.id.chart);
        ComboLineColumnChartData columndata = generateData();
        chart.setComboLineColumnChartData(columndata);
        setViewPort(chart);
        md.show();
    }

    public void setTitle(String title) {
        axisYLabel = title;
        mdb.title(title);
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setSteps(int steps) {
        this.steps = steps;
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

    private ComboLineColumnChartData generateData() {
        List<Line> lines = new ArrayList<Line>();

        List<PointValue> values = new ArrayList<>();
        List<PointValue> valueshu = new ArrayList<>();
        List<PointValue> valuesba = new ArrayList<>();
        List<PointValue> valuesc = new ArrayList<>();
        List<PointValue> valuesv = new ArrayList<>();

        List<AxisValue> axisValueX = new ArrayList<>();

        int counter = 0;
        boolean addHumidity = false;
        boolean addBarometer = false;
        boolean addTemperature = false;
        boolean addCounter = false;
        boolean addPercentage = false;
        boolean onlyDate = false;
        Calendar mydate = Calendar.getInstance();

        int stepcounter = 0;
        for (GraphPointInfo g : this.mGraphList) {
            stepcounter++;
            if (stepcounter == this.steps) {
                stepcounter = 0;
                try {
                    if (g.getTemperature() > 0) {
                        addTemperature = true;
                        values.add(new PointValue(counter, g.getTemperature()));
                    }
                    if (g.getBarometer() != null && g.getBarometer().length() > 0) {
                        addBarometer = true;
                        valuesba.add(new PointValue(counter, Integer.parseInt(g.getBarometer())));
                    }
                    if (g.getHumidity() != null && g.getHumidity().length() > 0) {
                        addHumidity = true;
                        valueshu.add(new PointValue(counter, Integer.parseInt(g.getHumidity())));
                    }
                    if (g.getPercentage() != null && g.getPercentage().length() > 0) {
                        addPercentage = true;
                        valuesv.add(new PointValue(counter, Float.parseFloat(g.getPercentage())));
                    }
                    if (g.getCounter() != null && g.getCounter().length() > 0) {
                        addCounter = true;
                        valuesc.add(new PointValue(counter, Float.parseFloat(g.getCounter())));
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        mydate.setTime(sdf.parse(g.getDateTime()));
                    } catch (ParseException e) {
                        onlyDate = true;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        mydate.setTime(sdf.parse(g.getDateTime()));
                    }

                    String label = "";
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
        if (addTemperature) {
            lines.add(new Line(values)
                    .setColor(ChartUtils.COLOR_BLUE)
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addHumidity) {
            lines.add(new Line(valueshu)
                    .setColor(ChartUtils.COLOR_RED)
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addBarometer) {
            lines.add(new Line(valuesba)
                    .setColor(ChartUtils.COLOR_GREEN)
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addCounter) {
            lines.add(new Line(valuesc)
                    .setColor(ChartUtils.COLOR_ORANGE)
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addPercentage) {
            lines.add(new Line(valuesv)
                    .setColor(ChartUtils.COLOR_VIOLET)
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
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

}