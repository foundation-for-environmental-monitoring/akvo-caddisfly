package org.akvo.caddisfly.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.ActivityRecommendBinding;
import org.akvo.caddisfly.model.RecommendationInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.PrintQueueSingleton;

import java.util.ArrayList;

public class RecommendActivity extends BaseActivity {

    private static ArrayList<PrintJob> mPrintJobs = new ArrayList<>();
    final Activity activity = this;
    WebView webView;
    TestInfo testInfo;
    RecommendationInfo recommendationInfo = new RecommendationInfo();
    ActivityRecommendBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        setTitle("Fertilizer Recommendation");

        b = DataBindingUtil.setContentView(this, R.layout.activity_recommend);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        testInfo = bundle.getParcelable(ConstantKey.TEST_INFO);

        getRecommendation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_print, menu);
        return true;
    }

    public void onPrint(MenuItem item) {
        doWebViewPrint();
    }

    private void doWebViewPrint() {
        WebView printWebView = new WebView(this);
        printWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageFinished(WebView view, String url) {
                createWebPrintJob(view);
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body><h1>Soil Health Card</h1><p>To be implemented</p></body></html>";
        printWebView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.appName) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

//        PrintAttributes attrib = new PrintAttributes.Builder()
//                .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE)
//                . build();

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
        mPrintJobs.add(printJob);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void getRecommendation() {

        Intent resultIntent = new Intent();

        String url = "https://soilhealth.dac.gov.in/calculator/calculator";

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Sending dummy result...");
        pd.setCancelable(false);
        pd.show();

        webView = new WebView(this);

        webView.getSettings().setJavaScriptEnabled(true);

        String state = getIntent().getStringExtra("State");
        String district = getIntent().getStringExtra("District");
        recommendationInfo.nitrogenResult = getIntent().getStringExtra("Available Nitrogen");
        recommendationInfo.phosphorusResult = getIntent().getStringExtra("Available Phosphorous");
        recommendationInfo.potassiumResult = getIntent().getStringExtra("Available Potassium");

        final String js = "javascript:document.getElementById('State_Code').value='" + state + "';StateChange();" +
                "javascript:document.getElementById('District_CodeDDL').value='" + district + "';DistrictChange('" + district + "');" +
                "javascript:document.getElementById('N').value='" + recommendationInfo.nitrogenResult + "';" +
                "javascript:document.getElementById('P').value='" + recommendationInfo.phosphorusResult + "';" +
                "javascript:document.getElementById('K').value='" + recommendationInfo.potassiumResult + "';" +
                "document.getElementsByClassName('myButton')[0].click();" +
                "javascript:document.getElementById('Group_Code').value='1';Crop(1);" +
                "javascript:document.getElementById('Crop_Code').value='3';Variety(3);" +
                "javascript:document.getElementById('AddCrop').click();" +
                "(function() { " +
                "return document.getElementById('C1F1').options[document.getElementById('C1F1').selectedIndex].text + ',' +" +
                "document.getElementById('Comb1_Fert1_Rec_dose1').value  + ',' +" +
                "document.getElementById('C1F2').options[document.getElementById('C1F2').selectedIndex].text + ',' +" +
                "document.getElementById('Comb1_Fert2_Rec_dose1').value  + ',' +" +
                "document.getElementById('C1F3').options[document.getElementById('C1F3').selectedIndex].text + ',' +" +
                "document.getElementById('Comb1_Fert3_Rec_dose1').value  + ',' +" +
                "document.getElementById('C2F1').options[document.getElementById('C2F1').selectedIndex].text + ',' +" +
                "document.getElementById('Comb2_Fert1_Rec_dose1').value  + ',' +" +
                "document.getElementById('C2F2').options[document.getElementById('C2F2').selectedIndex].text + ',' +" +
                "document.getElementById('Comb2_Fert2_Rec_dose1').value  + ',' +" +
                "document.getElementById('C2F3').options[document.getElementById('C2F3').selectedIndex].text + ',' +" +
                "document.getElementById('Comb2_Fert3_Rec_dose1').value;" +
                "})();";

        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            public void onPageFinished(WebView view, String url) {
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, s -> {

                        String[] values = s.replace("\"", "").split(",");

                        SparseArray<String> results = new SparseArray<>();

                        for (int i = 0; i < testInfo.getResults().size(); i++) {
                            Result result = testInfo.getResults().get(i);
                            resultIntent.putExtra(result.getName().replace(" ", "_")
                                    + testInfo.getResultSuffix(), values[i]);

                            results.append(result.getId(), result.getResult());
                        }

                        recommendationInfo.values = values;
                        recommendationInfo.state = "Maharashtra";
                        recommendationInfo.district = "Osmanabad";
                        recommendationInfo.crop = "Rice";
                        b.setInfo(recommendationInfo);

                        setResult(Activity.RESULT_OK, resultIntent);

                        pd.dismiss();
//                        (new Handler()).postDelayed(() -> {
//                            pd.dismiss();
//                            finish();
//                        }, 1000);
//                        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

        webView.loadUrl(url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        response -> {
                            resultIntent.putExtra(SensorConstants.VALUE, response.toString());
                            setResult(Activity.RESULT_OK, resultIntent);

                            (new Handler()).postDelayed(() -> {
                                pd.dismiss();
                                finish();
                            }, 3000);
                        },
                        error -> {
                            // TODO: Handle error
                        });

        PrintQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void onSaveClick(View view) {
        finish();
    }
}
