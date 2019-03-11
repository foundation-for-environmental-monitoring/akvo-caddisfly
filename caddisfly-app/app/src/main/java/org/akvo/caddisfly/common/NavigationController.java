package org.akvo.caddisfly.common;

import android.content.Context;
import android.content.Intent;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestSampleType;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.TestActivity;
import org.akvo.caddisfly.ui.TestListActivity;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import static org.akvo.caddisfly.common.ConstantKey.IS_INTERNAL;

/**
 * A utility class that handles navigation.
 */
public class NavigationController {

    Context context;

    public NavigationController(Context context) {
        this.context = context;
    }

//    public void navigateToTest(String uuid) {
//
//        final TestListViewModel viewModel =
//                ViewModelProviders.of((FragmentActivity) context).get(TestListViewModel.class);
//
//        TestInfo testInfo = viewModel.getTestInfo(uuid);
//
//        final Intent intent = new Intent(context, TestActivity.class);
//        intent.putExtra(IS_INTERNAL, true);
//        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
//        context.startActivity(intent);
//
//    }

    public void navigateToTestType(TestType testType, TestSampleType testSampleType) {

        final TestListViewModel viewModel =
                ViewModelProviders.of((FragmentActivity) context).get(TestListViewModel.class);

        Intent intent;
        List<TestInfo> tests = viewModel.getTests(testType, testSampleType);
        if (tests.size() == 1) {
            intent = new Intent(context, TestActivity.class);
            intent.putExtra(ConstantKey.TEST_INFO, tests.get(0));
        } else {
            intent = new Intent(context, TestListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ConstantKey.TYPE, testType);
            intent.putExtra(ConstantKey.SAMPLE_TYPE, testSampleType);
        }

        intent.putExtra(IS_INTERNAL, true);
        context.startActivity(intent);

    }
}
