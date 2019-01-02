package org.akvo.caddisfly.sensor.titration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONObject;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class TitrationTestActivity extends BaseActivity
        implements TitrationInputFragment.OnSubmitResultListener {

    private static final int MANUAL_TEST = 2;
    private TestInfo testInfo;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        setTitle(testInfo.getName());

        startManualTest();
    }

    private void startManualTest() {
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.add(R.id.fragment_container,
                TitrationInputFragment.newInstance(testInfo), "tubeFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (requestCode == MANUAL_TEST) {
                fragmentTransaction.replace(R.id.fragment_container,
                        TitrationInputFragment.newInstance(testInfo), "manualFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onSubmitResult(float result1, float result2) {

        testInfo.getResults().get(0).setResult(result1, 0, 0);
        testInfo.getResults().get(1).setResult(result2, 0, 0);

        Intent resultIntent = new Intent();
        SparseArray<String> results = new SparseArray<>();

        for (int i = 0; i < testInfo.getResults().size(); i++) {
            Result result = testInfo.getResults().get(i);
            resultIntent.putExtra(result.getName().replace(" ", "_")
                    + testInfo.getResultSuffix(), result.getResult());

            resultIntent.putExtra(result.getName().replace(" ", "_")
                    + "_" + SensorConstants.DILUTION
                    + testInfo.getResultSuffix(), testInfo.getDilution());

            resultIntent.putExtra(
                    result.getName().replace(" ", "_")
                            + "_" + SensorConstants.UNIT + testInfo.getResultSuffix(),
                    testInfo.getResults().get(0).getUnit());

            results.append(result.getId(), result.getResult());
        }

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, null);
        resultIntent.putExtra(SensorConstants.RESULT_JSON, resultJson.toString());

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
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
