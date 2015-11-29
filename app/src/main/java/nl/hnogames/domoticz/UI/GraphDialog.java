package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.view.View;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import nl.hnogames.domoticz.Containers.GraphPointInfo;
import nl.hnogames.domoticz.R;

public class GraphDialog {

    private final MaterialDialog.Builder mdb;
    private ArrayList<GraphPointInfo> mGraphList;

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
        mdb.title("Graph");
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        ComboLineColumnChartView chart = (ComboLineColumnChartView) view.findViewById(R.id.chart);
        ComboLineColumnChartData columndata = generateData();
        chart.setComboLineColumnChartData(columndata);

        md.show();
    }

    private ComboLineColumnChartData generateData() {
        List<Line> lines = new ArrayList<Line>();
        List<PointValue> values = new ArrayList<PointValue>();
        List<PointValue> valueshu = new ArrayList<PointValue>();
        List<PointValue> valuesba = new ArrayList<PointValue>();
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> valuesC = new ArrayList<SubcolumnValue>();
        List<AxisValue> axisValueX = new ArrayList<>();

        int counter = 0;
        boolean addHumidity = false;
        boolean addBarometer = false;
        boolean addTemperature = false;
        boolean onlyDate = false;

        for (GraphPointInfo g : this.mGraphList) {
            int interval = 0;
            try {
                if (g.getTemperature() > 0) {
                    addTemperature = true;
                }
                if (g.getBarometer() != null && g.getBarometer().length() > 0 && Integer.parseInt(g.getBarometer()) > 0) {
                    addBarometer = true;
                    valuesba.add(new PointValue(counter, Integer.parseInt(g.getBarometer())));
                }
                if (g.getHumidity() != null && g.getHumidity().length() > 0 && Integer.parseInt(g.getHumidity()) > 0) {
                    addHumidity = true;
                    valueshu.add(new PointValue(counter, Integer.parseInt(g.getHumidity())));
                }

                Calendar mydate = Calendar.getInstance();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    mydate.setTime(sdf.parse(g.getDateTime()));
                } catch (ParseException e) {
                    onlyDate = true;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    mydate.setTime(sdf.parse(g.getDateTime()));
                }

                values.add(new PointValue(counter, g.getTemperature()));
                values.add(new PointValue(counter, g.getTemperature()));
                valuesC.add(new SubcolumnValue(
                        g.getTemperature(),
                        ChartUtils.COLOR_BLUE));

                columns.add(new Column(valuesC));
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

        if (addTemperature) {
            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_BLUE);
            line.setCubic(false);
            line.setHasLabels(false);
            line.setHasLines(true);
            line.setHasPoints(false);
            lines.add(line);
        }

        if (addHumidity) {
            Line line = new Line(valueshu);
            line.setColor(ChartUtils.COLOR_RED);
            line.setCubic(false);
            line.setHasLabels(false);
            line.setHasLines(true);
            line.setHasPoints(false);
            lines.add(line);
        }

        if (addBarometer) {
            Line line = new Line(valuesba);
            line.setColor(ChartUtils.COLOR_GREEN);
            line.setCubic(false);
            line.setHasLabels(false);
            line.setHasLines(true);
            line.setHasPoints(false);
            lines.add(line);
        }

        LineChartData lineChartData = new LineChartData(lines);
        ColumnChartData columnChartData = new ColumnChartData(columns);
        Axis axisX = new Axis().setValues(axisValueX).setHasLines(true);
        Axis axisY = new Axis().setHasLines(true);

        axisX.setMaxLabelChars(5);
        axisX.setName("Date");
        axisY.setName("Temp");
        ComboLineColumnChartData data = new ComboLineColumnChartData(null,
                lineChartData);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        return data;
    }
}