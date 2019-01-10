package org.akvo.caddisfly.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.SparseArray;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

public class RecommendActivity extends BaseActivity {

    private static final String DATE_FORMAT = "dd MMM yyyy HH:mm";
    private static final String url = "https://soilhealth.dac.gov.in/calculator/calculator";

    private final Activity activity = this;
    private final RecommendationInfo recommendationInfo = new RecommendationInfo();
    private TestInfo testInfo;
    private ActivityRecommendBinding b;
    private String printTemplate;
    private String date;

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createWebPrintJob(WebView webView) {

        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = "Fertilizer Recommendation - " + date;

        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

//        PrintAttributes attrib = new PrintAttributes.Builder()
//                .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE)
//                . build();

        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void getRecommendation() {

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.just_a_moment));
        pd.setCancelable(false);
        pd.show();

        WebView webView = new WebView(this);

        webView.getSettings().setJavaScriptEnabled(true);

        String state = getStringExtra("State");
        String district = getStringExtra("District");
        String cropGroup = getStringExtra("Crop Group");
        recommendationInfo.farmerName = getStringExtra("Farmer name");
        recommendationInfo.phoneNumber = getStringExtra("Phone number");
        recommendationInfo.sampleNumber = getStringExtra("Sample number");
        recommendationInfo.villageName = getStringExtra("Village name");
        recommendationInfo.geoLocation = getStringExtra("Geolocation");

        String crop = getIntent().getStringExtra("Crop");

        if (recommendationInfo.farmerName.isEmpty() || recommendationInfo.sampleNumber.isEmpty() ||
                state.isEmpty() || district.isEmpty() || cropGroup.isEmpty() || crop.isEmpty()) {
            Toast.makeText(this,
                    "All farmer and crop details should be filled before requesting a recommendation.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recommendationInfo.nitrogenResult = getStringExtra("Available Nitrogen", "0");
        recommendationInfo.phosphorusResult = getStringExtra("Available Phosphorous", "0");
        recommendationInfo.potassiumResult = getStringExtra("Available Potassium", "0");

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

            boolean timeout = true;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Runnable run = () -> {
                    if (timeout) {
                        pd.dismiss();
                        webView.stopLoading();
                        finish();
                        Toast.makeText(activity, "Connection Timed out", Toast.LENGTH_SHORT).show();
                    }
                };
                Handler myHandler = new Handler(Looper.myLooper());
                myHandler.postDelayed(run, 20000);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                timeout = false;
                pd.dismiss();
                finish();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError error) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), req.getUrl().toString());
            }

            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(js, s -> {

                    String[] values = s.replace("\"", "").split(",");
                    if (values.length > 10) {
                        timeout = false;
                        displayResult(values);
                    }
                });
                (new Handler()).postDelayed(pd::dismiss, 4000);
            }
        });

        webView.loadUrl(url);
    }

    private void displayResult(String[] values) {

        Intent resultIntent = new Intent();

        SparseArray<String> results = new SparseArray<>();
        recommendationInfo.state = values[0];
        recommendationInfo.district = values[1];
        recommendationInfo.crop = values[2];

        for (int i = 0; i < testInfo.getResults().size(); i++) {
            Result result = testInfo.getResults().get(i);
            resultIntent.putExtra(result.getName().replace(" ", "_")
                    + testInfo.getResultSuffix(), values[i + 3]);

            results.append(result.getId(), result.getResult());

            printTemplate = printTemplate.replace("#Value" + i + "#", values[i + 3]);
        }

        preparePrintDocument();

        recommendationInfo.values = Arrays.copyOfRange(values, 3, values.length);

        b.setInfo(recommendationInfo);

        setResult(Activity.RESULT_OK, resultIntent);
    }

    private void preparePrintDocument() {
        date = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
        printTemplate = printTemplate.replace("#Date#", date);
        printTemplate = printTemplate.replace("#FarmerName#", recommendationInfo.farmerName);
        printTemplate = printTemplate.replace("#PhoneNumber#", recommendationInfo.phoneNumber);
        printTemplate = printTemplate.replace("#VillageName#", recommendationInfo.villageName);
        printTemplate = printTemplate.replace("#State#", recommendationInfo.state);
        printTemplate = printTemplate.replace("#District#", recommendationInfo.district);
        printTemplate = printTemplate.replace("#SampleNumber#", recommendationInfo.sampleNumber);
        printTemplate = printTemplate.replace("#Crop#", recommendationInfo.crop);

        if (recommendationInfo.geoLocation != null && !recommendationInfo.geoLocation.isEmpty()) {
            String[] geoValues = recommendationInfo.geoLocation.split(" ");
            for (int i = 0; i < geoValues.length; i++) {
                // Also show unit (m) for last two values
                printTemplate = printTemplate.replace("#Geo" + i + "#",
                        i > 1 ? geoValues[i] + "m" : geoValues[i]);
            }
        }

        printTemplate = printTemplate.replace("#Nitrogen#", recommendationInfo.nitrogenResult);
        printTemplate = printTemplate.replace("#Phosphorus#", recommendationInfo.phosphorusResult);
        printTemplate = printTemplate.replace("#Potassium#", recommendationInfo.potassiumResult);

        printTemplate = printTemplate.replaceAll("#.*?#", "");
    }

    public void onPrintClick(View view) {
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

    public void onSaveClick(View view) {
        finish();
    }

    private String getStringExtra(String key) {
        return getStringExtra(key, "");
    }

    private String getStringExtra(String key, String defaultValue) {
        String value = getIntent().getStringExtra(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }
}
