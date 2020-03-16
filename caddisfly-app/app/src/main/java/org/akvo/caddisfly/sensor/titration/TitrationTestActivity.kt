package org.akvo.caddisfly.sensor.titration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.helper.TestConfigHelper.getJsonResult
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.ui.BaseActivity

class TitrationTestActivity : BaseActivity(), TitrationInputFragment.OnSubmitResultListener {
    private var testInfo: TestInfo? = null
    private var fragmentManager: FragmentManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_test)
        fragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)
        }
        if (testInfo != null) {
            title = testInfo!!.name
        }
        startManualTest()
    }

    private fun startManualTest() {
        val ft = fragmentManager!!.beginTransaction()
        ft.add(R.id.fragment_container,
                        TitrationInputFragment.newInstance(testInfo), "tubeFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            if (requestCode == MANUAL_TEST) {
                fragmentTransaction.replace(R.id.fragment_container,
                                TitrationInputFragment.newInstance(testInfo), "manualFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .addToBackStack(null)
                        .commit()
            }
        } else {
            onBackPressed()
        }
    }

    override fun onSubmitResult(results: FloatArray) {
        for (i in results.indices) {
            testInfo!!.results!![i].setResult(results[i].toDouble(), 0, 0)
        }
        val resultIntent = Intent()
        val resultsValues = SparseArray<String>()
        for (i in testInfo!!.results!!.indices) {
            val result = testInfo!!.results!![i]
            resultIntent.putExtra(result.name?.replace(" ", "_")
                    + testInfo!!.resultSuffix, result.result)
            resultIntent.putExtra(result.name?.replace(" ", "_")
                    + "_" + SensorConstants.DILUTION
                    + testInfo!!.resultSuffix, testInfo!!.dilution)
            resultIntent.putExtra(
                    result.name?.replace(" ", "_")
                            + "_" + SensorConstants.UNIT + testInfo!!.resultSuffix,
                    testInfo!!.results!![0].unit)
            resultsValues.append(result.id, result.result)
        }
        val resultJson = getJsonResult(testInfo!!, resultsValues, null, -1, null)
        resultIntent.putExtra(SensorConstants.RESULT_JSON, resultJson.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val MANUAL_TEST = 2
    }
}