package org.akvo.caddisfly.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.ActivityRecommendBinding;
import org.akvo.caddisfly.model.RecommendationInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AssetsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

public class RecommendActivity extends BaseActivity {

    public static final String DATE_FORMAT = "dd MMM yyyy HH:mm";
    private static ArrayList<PrintJob> mPrintJobs = new ArrayList<>();
    final Activity activity = this;
    WebView webView;
    TestInfo testInfo;
    RecommendationInfo recommendationInfo = new RecommendationInfo();
    ActivityRecommendBinding b;
    String printTemplate;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        setTitle("Fertilizer Recommendation");

        b = DataBindingUtil.setContentView(this, R.layout.activity_recommend);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        testInfo = bundle.getParcelable(ConstantKey.TEST_INFO);

        printTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/recommendation_template.html");

        getRecommendation();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
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

        printWebView.loadDataWithBaseURL(null, printTemplate, "text/HTML", "UTF-8", null);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = "Fertilizer Recommendation - " + date;

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
        pd.setMessage(getString(R.string.just_a_moment));
        pd.setCancelable(false);
        pd.show();

        webView = new WebView(this);

        webView.getSettings().setJavaScriptEnabled(true);

        String state = getIntent().getStringExtra("State");
        String district = getIntent().getStringExtra("District");
        String cropGroup = getIntent().getStringExtra("Crop Group");
        recommendationInfo.farmerName = getIntent().getStringExtra("Farmer name");
        recommendationInfo.phoneNumber = getIntent().getStringExtra("Phone number");
        recommendationInfo.sampleNumber = getIntent().getStringExtra("Sample number");
        recommendationInfo.villageName = getIntent().getStringExtra("Village name");
        recommendationInfo.geoLocation = getIntent().getStringExtra("Geolocation");

        String crop = getIntent().getStringExtra("Crop");

        if (state == null || district == null || cropGroup == null || crop == null) {
            Toast.makeText(this,
                    "State, District, Crop Group and Crop details required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recommendationInfo.nitrogenResult = getIntent().getStringExtra("Available Nitrogen");
        recommendationInfo.phosphorusResult = getIntent().getStringExtra("Available Phosphorous");
        recommendationInfo.potassiumResult = getIntent().getStringExtra("Available Potassium");
        if (recommendationInfo.nitrogenResult == null) {
            recommendationInfo.nitrogenResult = "0";
        }

        if (recommendationInfo.phosphorusResult == null) {
            recommendationInfo.phosphorusResult = "0";
        }

        if (recommendationInfo.potassiumResult == null) {
            recommendationInfo.potassiumResult = "0";
        }

        final String js = "javascript:document.getElementById('State_Code').value='" + state + "';StateChange();" +
                "javascript:document.getElementById('District_CodeDDL').value='" + district + "';DistrictChange('" + district + "');" +
                "javascript:document.getElementById('N').value='" + recommendationInfo.nitrogenResult + "';" +
                "javascript:document.getElementById('P').value='" + recommendationInfo.phosphorusResult + "';" +
                "javascript:document.getElementById('K').value='" + recommendationInfo.potassiumResult + "';" +
                "document.getElementsByClassName('myButton')[0].click();" +
                "javascript:document.getElementById('Group_Code').value='" + cropGroup + "';Crop(" + cropGroup + ");" +
                "javascript:document.getElementById('Crop_Code').value='" + crop + "';Variety(" + crop + ");" +
                "javascript:document.getElementById('AddCrop').click();" +
                "(function() { " +
                "return " +
                "document.getElementById('State_Code').options[document.getElementById('State_Code').selectedIndex].text + ',' +" +
                "document.getElementById('District_CodeDDL').options[document.getElementById('District_CodeDDL').selectedIndex].text + ',' +" +
                "document.getElementById('Crop_Code').options[document.getElementById('Crop_Code').selectedIndex].text + ',' +" +
                "document.getElementById('C1F1').options[document.getElementById('C1F1').selectedIndex].text + ',' +" +
                "document.getElementById('Comb1_Fert1_Rec_dose1').value + ',' +" +
                "document.getElementById('C1F2').options[document.getElementById('C1F2').selectedIndex].text + ',' +" +
                "document.getElementById('Comb1_Fert2_Rec_dose1').value + ',' +" +
                "document.getElementById('C1F3').options[document.getElementById('C1F3').selectedIndex].text + ',' +" +
                "document.getElementById('Comb1_Fert3_Rec_dose1').value + ',' +" +
                "document.getElementById('C2F1').options[document.getElementById('C2F1').selectedIndex].text + ',' +" +
                "document.getElementById('Comb2_Fert1_Rec_dose1').value + ',' +" +
                "document.getElementById('C2F2').options[document.getElementById('C2F2').selectedIndex].text + ',' +" +
                "document.getElementById('Comb2_Fert2_Rec_dose1').value + ',' +" +
                "document.getElementById('C2F3').options[document.getElementById('C2F3').selectedIndex].text + ',' +" +
                "document.getElementById('Comb2_Fert3_Rec_dose1').value;" +
                "})();";

        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(js, s -> {

                    String[] values = s.replace("\"", "").split(",");

                    SparseArray<String> results = new SparseArray<>();
                    recommendationInfo.state = values[0];
                    recommendationInfo.district = values[1];
                    recommendationInfo.crop = values[2];
                    date = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
                    printTemplate = printTemplate.replace("{Date}", date);
                    printTemplate = printTemplate.replace("{State}", recommendationInfo.state);
                    printTemplate = printTemplate.replace("{District}", recommendationInfo.district);
                    printTemplate = printTemplate.replace("{Crop}", recommendationInfo.crop);
                    printTemplate = printTemplate.replace("{Nitrogen}", recommendationInfo.nitrogenResult);
                    printTemplate = printTemplate.replace("{Phosphorus}", recommendationInfo.phosphorusResult);
                    printTemplate = printTemplate.replace("{Potassium}", recommendationInfo.potassiumResult);
                    printTemplate = printTemplate.replace("{FarmerName}", recommendationInfo.farmerName);
                    printTemplate = printTemplate.replace("{PhoneNumber}", recommendationInfo.phoneNumber);
                    printTemplate = printTemplate.replace("{VillageName}", recommendationInfo.villageName);
                    printTemplate = printTemplate.replace("{Geolocation}", recommendationInfo.geoLocation);

                    printTemplate = printTemplate.replace("{SampleNumber}", recommendationInfo.sampleNumber);
                    for (int i = 0; i < testInfo.getResults().size(); i++) {
                        Result result = testInfo.getResults().get(i);
                        resultIntent.putExtra(result.getName().replace(" ", "_")
                                + testInfo.getResultSuffix(), values[i + 3]);

                        results.append(result.getId(), result.getResult());

                        printTemplate = printTemplate.replace("{Value" + i + "}", values[i + 3]);
                    }

                    recommendationInfo.values = Arrays.copyOfRange(values, 3, values.length);

                    b.setInfo(recommendationInfo);

                    setResult(Activity.RESULT_OK, resultIntent);
                });
                (new Handler()).postDelayed(pd::dismiss, 3000);
            }
        });

        webView.loadUrl(url);
    }

    public void onSaveClick(View view) {
        finish();
    }
}
