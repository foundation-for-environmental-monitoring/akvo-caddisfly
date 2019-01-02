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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class CalibrationGraphActivity extends BaseActivity {

    private GraphView graphRed;
    private GraphView graphGreen;
    private GraphView graphBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_graph);

        TestInfo testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        List<Calibration> calibrations = testInfo.getCalibrations();
        List<ColorItem> presetCalibrations = testInfo.getReferenceColors();
        List<Calibration> oneStepCalibrations = testInfo.getOneStepCalibrations();

        graphRed = findViewById(R.id.graphRed);
        graphGreen = findViewById(R.id.graphGreen);
        graphBlue = findViewById(R.id.graphBlue);

        if (presetCalibrations.size() > 0) {
            addPresetSeries(presetCalibrations);
            addCalibrationSeries(calibrations);
        } else {
            addCalibrationSeries(calibrations);
            addPresetSeries(presetCalibrations);
        }

        addOneStepSeries(oneStepCalibrations);

        setTitle("Charts");
    }

    private void addOneStepSeries(List<Calibration> oneStepCalibrations) {
        LineGraphSeries<DataPoint> seriesRed =
                new LineGraphSeries<>(getDataPoints(oneStepCalibrations, Color.RED));
        seriesRed.setColor(Color.MAGENTA);
        seriesRed.setThickness(3);
        seriesRed.setDrawDataPoints(true);
        seriesRed.setDataPointsRadius(4);
        graphRed.addSeries(seriesRed);

        LineGraphSeries<DataPoint> seriesGreen =
                new LineGraphSeries<>(getDataPoints(oneStepCalibrations, Color.GREEN));
        seriesGreen.setColor(Color.MAGENTA);
        seriesGreen.setThickness(3);
        seriesGreen.setDrawDataPoints(true);
        seriesGreen.setDataPointsRadius(4);
        graphGreen.addSeries(seriesGreen);

        LineGraphSeries<DataPoint> seriesBlue =
                new LineGraphSeries<>(getDataPoints(oneStepCalibrations, Color.BLUE));
        seriesBlue.setColor(Color.MAGENTA);
        seriesBlue.setThickness(3);
        seriesBlue.setDrawDataPoints(true);
        seriesBlue.setDataPointsRadius(4);
        graphBlue.addSeries(seriesBlue);
    }

    private void addCalibrationSeries(List<Calibration> calibrations) {
        LineGraphSeries<DataPoint> seriesRed =
                new LineGraphSeries<>(getDataPoints(calibrations, Color.RED));
        seriesRed.setColor(Color.RED);
        seriesRed.setThickness(4);
        seriesRed.setDrawDataPoints(true);
        seriesRed.setDataPointsRadius(9);
        graphRed.addSeries(seriesRed);

        LineGraphSeries<DataPoint> seriesGreen =
                new LineGraphSeries<>(getDataPoints(calibrations, Color.GREEN));
        seriesGreen.setColor(Color.RED);
        seriesGreen.setThickness(4);
        seriesGreen.setDrawDataPoints(true);
        seriesGreen.setDataPointsRadius(9);
        graphGreen.addSeries(seriesGreen);

        LineGraphSeries<DataPoint> seriesBlue =
                new LineGraphSeries<>(getDataPoints(calibrations, Color.BLUE));
        seriesBlue.setColor(Color.RED);
        seriesBlue.setThickness(4);
        seriesBlue.setDrawDataPoints(true);
        seriesBlue.setDataPointsRadius(9);
        graphBlue.addSeries(seriesBlue);
    }

    private void addPresetSeries(List<ColorItem> presetCalibrations) {
        LineGraphSeries<DataPoint> seriesRed =
                new LineGraphSeries<>(getPresetDataPoints(presetCalibrations, Color.RED));
        seriesRed.setColor(Color.BLACK);
        seriesRed.setThickness(3);
        seriesRed.setDrawDataPoints(true);
        seriesRed.setDataPointsRadius(4);
        graphRed.addSeries(seriesRed);

        LineGraphSeries<DataPoint> seriesGreen =
                new LineGraphSeries<>(getPresetDataPoints(presetCalibrations, Color.GREEN));
        seriesGreen.setColor(Color.BLACK);
        seriesGreen.setThickness(3);
        seriesGreen.setDrawDataPoints(true);
        seriesGreen.setDataPointsRadius(4);
        graphGreen.addSeries(seriesGreen);

        LineGraphSeries<DataPoint> seriesBlue =
                new LineGraphSeries<>(getPresetDataPoints(presetCalibrations, Color.BLUE));
        seriesBlue.setColor(Color.BLACK);
        seriesBlue.setThickness(3);
        seriesBlue.setDrawDataPoints(true);
        seriesBlue.setDataPointsRadius(4);
        graphBlue.addSeries(seriesBlue);
    }

    @NonNull
    private DataPoint[] getPresetDataPoints(List<ColorItem> colorItems, int color) {
        DataPoint[] dataPoints = new DataPoint[colorItems.size()];
        int value = 0;
        for (int i = 0; i < colorItems.size(); i++) {
            if (colorItems.get(i).getRgbInt() == 0) {
                return new DataPoint[0];
            } else {
                switch (color) {
                    case Color.RED:
                        value = Color.red(colorItems.get(i).getRgbInt());
                        break;
                    case Color.GREEN:
                        value = Color.green(colorItems.get(i).getRgbInt());
                        break;
                    case Color.BLUE:
                        value = Color.blue(colorItems.get(i).getRgbInt());
                        break;
                }
            }
            dataPoints[i] = new DataPoint(colorItems.get(i).getValue(), value);
        }
        return dataPoints;
    }

    @NonNull
    private DataPoint[] getDataPoints(List<Calibration> calibrations, int color) {
        List<DataPoint> dataPoints = new ArrayList<>();
        int value = 0;
        for (int i = 0; i < calibrations.size(); i++) {
            if (calibrations.get(i).color != 0) {
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
                dataPoints.add(new DataPoint(calibrations.get(i).value, value));
            }
        }
        return dataPoints.toArray(new DataPoint[0]);
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
