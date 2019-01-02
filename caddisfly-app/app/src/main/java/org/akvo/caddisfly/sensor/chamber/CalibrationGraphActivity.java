package org.akvo.caddisfly.sensor.chamber;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;

import java.util.List;

import androidx.annotation.NonNull;

public class CalibrationGraphActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_graph);

        TestInfo testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        List<Calibration> calibrations = testInfo.getCalibrations();
        List<ColorItem> presetCalibrations = testInfo.getPresetColors();

        GraphView graphRed = findViewById(R.id.graphRed);
        GraphView graphGreen = findViewById(R.id.graphGreen);
        GraphView graphBlue = findViewById(R.id.graphBlue);

        LineGraphSeries<DataPoint> seriesRed =
                new LineGraphSeries<>(getDataPoints(calibrations, Color.RED));
        seriesRed.setColor(Color.RED);
        seriesRed.setThickness(4);
        seriesRed.setDrawDataPoints(true);
        seriesRed.setDataPointsRadius(9);
        graphRed.addSeries(seriesRed);

        LineGraphSeries<DataPoint> seriesGreen =
                new LineGraphSeries<>(getDataPoints(calibrations, Color.GREEN));
        seriesGreen.setColor(Color.GREEN);
        seriesGreen.setThickness(4);
        seriesGreen.setDrawDataPoints(true);
        seriesGreen.setDataPointsRadius(9);
        graphGreen.addSeries(seriesGreen);

        LineGraphSeries<DataPoint> seriesBlue =
                new LineGraphSeries<>(getDataPoints(calibrations, Color.BLUE));
        seriesBlue.setColor(Color.BLUE);
        seriesBlue.setThickness(4);
        seriesBlue.setDrawDataPoints(true);
        seriesBlue.setDataPointsRadius(9);
        graphBlue.addSeries(seriesBlue);

        LineGraphSeries<DataPoint> seriesRed2 =
                new LineGraphSeries<>(getPresetDataPoints(presetCalibrations, Color.RED));
        seriesRed2.setColor(Color.BLACK);
        seriesRed2.setThickness(3);
        seriesRed2.setDrawDataPoints(true);
        seriesRed2.setDataPointsRadius(4);
        graphRed.addSeries(seriesRed2);

        LineGraphSeries<DataPoint> seriesGreen2 =
                new LineGraphSeries<>(getPresetDataPoints(presetCalibrations, Color.GREEN));
        seriesGreen2.setColor(Color.BLACK);
        seriesGreen2.setThickness(3);
        seriesGreen2.setDrawDataPoints(true);
        seriesGreen2.setDataPointsRadius(4);
        graphGreen.addSeries(seriesGreen2);

        LineGraphSeries<DataPoint> seriesBlue2 =
                new LineGraphSeries<>(getPresetDataPoints(presetCalibrations, Color.BLUE));
        seriesBlue2.setColor(Color.BLACK);
        seriesBlue2.setThickness(3);
        seriesBlue2.setDrawDataPoints(true);
        seriesBlue2.setDataPointsRadius(4);
        graphBlue.addSeries(seriesBlue2);

        setTitle("Charts");
    }

    @NonNull
    private DataPoint[] getPresetDataPoints(List<ColorItem> colorItems, int color) {
        DataPoint[] dataPoints = new DataPoint[colorItems.size()];
        int value = 0;
        for (int i = 0; i < colorItems.size(); i++) {
            switch (color) {
                case Color.RED:
                    value = colorItems.get(i).getRgb().get(0);
                    break;
                case Color.GREEN:
                    value = colorItems.get(i).getRgb().get(1);
                    break;
                case Color.BLUE:
                    value = colorItems.get(i).getRgb().get(2);
                    break;
            }
            dataPoints[i] = new DataPoint(colorItems.get(i).getValue(), value);
        }
        return dataPoints;
    }

    @NonNull
    private DataPoint[] getDataPoints(List<Calibration> calibrations, int color) {
        DataPoint[] dataPoints = new DataPoint[calibrations.size()];
        int value = 0;
        for (int i = 0; i < calibrations.size(); i++) {
            switch (color) {
                case Color.RED:
                    value = Color.red(calibrations.get(i).color);
                    break;
                case Color.GREEN:
                    value = Color.green(calibrations.get(i).color);
                    break;
                case Color.BLUE:
                    value = Color.blue(calibrations.get(i).color);
                    break;
            }
            dataPoints[i] = new DataPoint(calibrations.get(i).value, value);
        }
        return dataPoints;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
