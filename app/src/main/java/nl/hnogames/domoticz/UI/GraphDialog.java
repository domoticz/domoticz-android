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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

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
import nl.hnogames.domoticz.R;

public class GraphDialog {

    private final MaterialDialog.Builder mdb;
    private ArrayList<GraphPointInfo> mGraphList;
    private String axisYLabel = "Temp";
    private String range = "day";
    private int steps = 1;
    private Context mContext;

    public GraphDialog(Context mContext,
                       ArrayList<GraphPointInfo> mGraphList,
                       int layout) {
        this.mGraphList = mGraphList;
        this.mContext=mContext;
        mdb = new MaterialDialog.Builder(mContext);
        //noinspection unused
        boolean wrapInScrollView = true;
        mdb.customView(layout, false)
                .positiveText(android.R.string.ok);
    }

    public void show() {
        mdb.title(axisYLabel);
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        ComboLineColumnChartView chart = (ComboLineColumnChartView) view.findViewById(R.id.chart);
        ComboLineColumnChartData columnData = generateData(view );
        chart.setComboLineColumnChartData(columnData);
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

    @SuppressWarnings("SpellCheckingInspection")
    private ComboLineColumnChartData generateData(View view ) {
        List<Line> lines = new ArrayList<>();

        List<PointValue> values = new ArrayList<>();
        List<PointValue> valueshu = new ArrayList<>();
        List<PointValue> valuesba = new ArrayList<>();
        List<PointValue> valuesc = new ArrayList<>();
        List<PointValue> valuesv = new ArrayList<>();

        List<PointValue> valuessp = new ArrayList<>();
        List<PointValue> valuesdi = new ArrayList<>();
        List<PointValue> valuesuv = new ArrayList<>();

        List<AxisValue> axisValueX = new ArrayList<>();

        int counter = 0;
        boolean addHumidity = false;
        boolean addBarometer = false;
        boolean addTemperature = false;
        boolean addCounter = false;
        boolean addPercentage = false;
        boolean addSunPower = false;
        boolean addDirection = false;
        boolean addSpeed = false;
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
        if (addTemperature) {
            lines.add(new Line(values)
                    .setColor(ContextCompat.getColor(mContext, R.color.md_material_blue_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addHumidity) {
            lines.add(new Line(valueshu)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_orange_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));

        }

        if (addBarometer) {
            lines.add(new Line(valuesba)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_green_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addCounter) {
            lines.add(new Line(valuesc)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_indigo_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addPercentage) {
            lines.add(new Line(valuesv)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_yellow_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (addDirection) {
            lines.add(new Line(valuesdi)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_deep_teal_500))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));

        }

        if (addSunPower) {
            lines.add(new Line(valuesuv)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_deep_purple_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));

        }

        if (addSpeed) {
            lines.add(new Line(valuessp)
                    .setColor(ContextCompat.getColor(mContext, R.color.material_amber_600))
                    .setCubic(setCubic)
                    .setHasLabels(false)
                    .setHasLines(true)
                    .setHasPoints(false));
        }

        if (lines.size() > 1) {
            if (addTemperature) {
                ( view.findViewById(R.id.legend_temperature))
                        .setVisibility(View.VISIBLE);
            }

            if (addHumidity) {
                (view.findViewById(R.id.legend_humidity))
                        .setVisibility(View.VISIBLE);
            }

            if (addBarometer) {
                (view.findViewById(R.id.legend_barometer))
                        .setVisibility(View.VISIBLE);
            }

            if (addCounter) {
                (view.findViewById(R.id.legend_counter))
                        .setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.legend_counter))
                        .setText(axisYLabel);
            }

            if (addPercentage) {
                (view.findViewById(R.id.legend_percentage))
                        .setVisibility(View.VISIBLE);
            }

            if (addDirection) {
                (view.findViewById(R.id.legend_direction))
                        .setVisibility(View.VISIBLE);
            }

            if (addSunPower) {
                (view.findViewById(R.id.legend_sunpower))
                        .setVisibility(View.VISIBLE);
            }

            if (addSpeed) {
                (view.findViewById(R.id.legend_speed))
                        .setVisibility(View.VISIBLE);
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

}