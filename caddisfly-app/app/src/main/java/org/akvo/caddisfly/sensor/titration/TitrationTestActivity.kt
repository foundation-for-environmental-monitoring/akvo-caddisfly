package org.akvo.caddisfly.sensor.titration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.helper.TestConfigHelper.getJsonResult
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.sensor.chamber.ResultFragment
import org.akvo.caddisfly.ui.BaseActivity
import org.akvo.caddisfly.util.toLocalString

class TitrationTestActivity : BaseActivity(), TitrationInputFragment.OnSubmitResultListener {
    private var testInfo: TestInfo? = null
    private val ft = supportFragmentManager.beginTransaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_test)
        if (savedInstanceState == null) {
            testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)
        }
        if (testInfo != null) {
            title = testInfo!!.name!!.toLocalString()
        }
        startManualTest()
    }

    private fun startManualTest() {
        ft.add(
            R.id.fragment_container,
            TitrationInputFragment.newInstance(testInfo), "titrationFragment"
        ).commit()
    }

    override fun onSubmitResult(results: FloatArray) {
        for (i in results.indices) {
            testInfo!!.results!![i].setResult(results[i].toDouble(), 0, 0)
        }
        val resultIntent = Intent()
        val resultsValues = SparseArray<String>()
        for (i in testInfo!!.results!!.indices) {
            val result = testInfo!!.results!![i]
            resultIntent.putExtra(
                result.name?.replace(" ", "_")
                        + testInfo!!.resultSuffix, result.result
            )
            resultIntent.putExtra(
                result.name?.replace(" ", "_")
                        + "_" + SensorConstants.DILUTION
                        + testInfo!!.resultSuffix, testInfo!!.dilution
            )
            resultIntent.putExtra(
                result.name?.replace(" ", "_")
                        + "_" + SensorConstants.UNIT + testInfo!!.resultSuffix,
                testInfo!!.results!![0].unit
            )
            resultsValues.append(result.id, result.result)

            if (result.display == 1 || testInfo!!.results!!.size == 1) {
                resultIntent.putExtra(SensorConstants.VALUE, result.result)
            }
        }
        val resultJson = getJsonResult(testInfo!!, resultsValues, null, -1, null)
        resultIntent.putExtra(SensorConstants.RESULT_JSON, resultJson.toString())
        setResult(Activity.RESULT_OK, resultIntent)

        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(
                R.id.fragment_container,
                ResultFragment.newInstance(testInfo, false), null
            ).commit()
    }

    fun onClickAcceptResult(@Suppress("UNUSED_PARAMETER") view: View?) {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (testInfo != null) {
            title = testInfo!!.name!!.toLocalString()
        }
    }
}