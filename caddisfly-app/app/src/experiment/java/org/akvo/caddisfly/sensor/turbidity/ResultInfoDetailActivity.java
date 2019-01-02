package org.akvo.caddisfly.sensor.turbidity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseActivity;

import androidx.appcompat.app.ActionBar;

/**
 * An activity representing a single ResultInfo detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ResultInfoListActivity}.
 */
public class ResultInfoDetailActivity extends BaseActivity {

    public static final String ARG_ITEM_ID = "item_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultinfo_detail);
//        Toolbar toolbar = findViewById(R.id.detail_toolbar);
//        setSupportActionBar(toolbar);

        setTitle(R.string.result);

        WebView webView = findViewById(R.id.webView);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            webView.loadUrl("file:///" + getIntent().getStringExtra(ARG_ITEM_ID)
                    + "/result.html");
        }
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
