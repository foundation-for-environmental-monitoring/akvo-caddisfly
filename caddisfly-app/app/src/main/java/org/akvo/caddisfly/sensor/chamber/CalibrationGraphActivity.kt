package org.akvo.caddisfly.sensor.chamber

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.model.ColorItem
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.ui.BaseActivity

class CalibrationGraphActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration_graph)
        val testInfo: TestInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)!!
        val calibrations = testInfo.calibrations
        val presetCalibrations = testInfo.presetColors
        val graphRed = findViewById<GraphView>(R.id.graphRed)
        val graphGreen = findViewById<GraphView>(R.id.graphGreen)
        val graphBlue = findViewById<GraphView>(R.id.graphBlue)
        val seriesRed = LineGraphSeries(getDataPoints(calibrations, Color.RED))
        seriesRed.color = Color.RED
        seriesRed.thickness = 4
        seriesRed.isDrawDataPoints = true
        seriesRed.dataPointsRadius = 9f
        graphRed.addSeries(seriesRed)
        val seriesGreen = LineGraphSeries(getDataPoints(calibrations, Color.GREEN))
        seriesGreen.color = Color.GREEN
        seriesGreen.thickness = 4
        seriesGreen.isDrawDataPoints = true
        seriesGreen.dataPointsRadius = 9f
        graphGreen.addSeries(seriesGreen)
        val seriesBlue = LineGraphSeries(getDataPoints(calibrations, Color.BLUE))
        seriesBlue.color = Color.BLUE
        seriesBlue.thickness = 4
        seriesBlue.isDrawDataPoints = true
        seriesBlue.dataPointsRadius = 9f
        graphBlue.addSeries(seriesBlue)
        val seriesRed2 = LineGraphSeries(getPresetDataPoints(presetCalibrations!!, Color.RED))
        seriesRed2.color = Color.BLACK
        seriesRed2.thickness = 3
        seriesRed2.isDrawDataPoints = true
        seriesRed2.dataPointsRadius = 4f
        graphRed.addSeries(seriesRed2)
        val seriesGreen2 = LineGraphSeries(getPresetDataPoints(presetCalibrations, Color.GREEN))
        seriesGreen2.color = Color.BLACK
        seriesGreen2.thickness = 3
        seriesGreen2.isDrawDataPoints = true
        seriesGreen2.dataPointsRadius = 4f
        graphGreen.addSeries(seriesGreen2)
        val seriesBlue2 = LineGraphSeries(getPresetDataPoints(presetCalibrations, Color.BLUE))
        seriesBlue2.color = Color.BLACK
        seriesBlue2.thickness = 3
        seriesBlue2.isDrawDataPoints = true
        seriesBlue2.dataPointsRadius = 4f
        graphBlue.addSeries(seriesBlue2)
        title = "Charts"
    }

    private fun getPresetDataPoints(colorItems: List<ColorItem>, color: Int): Array<DataPoint?> {
        val dataPoints = arrayOfNulls<DataPoint>(colorItems.size)
        var value = 0
        for (i in colorItems.indices) {
            when (color) {
                Color.RED -> value = colorItems[i].rgb[0]
                Color.GREEN -> value = colorItems[i].rgb[1]
                Color.BLUE -> value = colorItems[i].rgb[2]
            }
            dataPoints[i] = DataPoint(colorItems[i].value!!, value.toDouble())
        }
        return dataPoints
    }

    private fun getDataPoints(calibrations: List<Calibration>, color: Int): Array<DataPoint?> {
        val dataPoints = arrayOfNulls<DataPoint>(calibrations.size)
        var value = 0
        for (i in calibrations.indices) {
            when (color) {
                Color.RED -> value = Color.red(calibrations[i].color)
                Color.GREEN -> value = Color.green(calibrations[i].color)
                Color.BLUE -> value = Color.blue(calibrations[i].color)
            }
            dataPoints[i] = DataPoint(calibrations[i].value, value.toDouble())
        }
        return dataPoints
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}