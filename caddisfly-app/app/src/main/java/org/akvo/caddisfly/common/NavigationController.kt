package org.akvo.caddisfly.common

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import org.akvo.caddisfly.model.TestSampleType
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.ui.TestListActivity
import org.akvo.caddisfly.viewmodel.TestListViewModel

/**
 * A utility class that handles navigation.
 */
class NavigationController(private val context: Context) {
    fun navigateToTestType(testType: TestType?, testSampleType: TestSampleType?, runTest: Boolean) {
        val viewModel = ViewModelProvider((context as FragmentActivity)).get(TestListViewModel::class.java)
        val intent: Intent
        val tests = viewModel.getTests(testType, testSampleType)
        if (tests.size == 1) {
            intent = Intent(context, TestActivity::class.java)
            intent.putExtra(ConstantKey.TEST_INFO, tests[0])
        } else {
            intent = Intent(context, TestListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(ConstantKey.TYPE, testType)
            intent.putExtra(ConstantKey.SAMPLE_TYPE, testSampleType)
        }
        if (runTest) {
            intent.putExtra(ConstantKey.RUN_TEST, true)
        }
        intent.putExtra(ConstantKey.IS_INTERNAL, true)
        context.startActivity(intent)
    }

}