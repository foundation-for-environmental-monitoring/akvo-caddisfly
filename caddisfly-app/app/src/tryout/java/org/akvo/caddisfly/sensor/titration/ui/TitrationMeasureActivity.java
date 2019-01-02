package org.akvo.caddisfly.sensor.titration.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.utils.MessageUtils;
import org.akvo.caddisfly.sensor.titration.camera.TitrationCameraManager;
import org.akvo.caddisfly.sensor.titration.models.TimeDelayDetail;
import org.akvo.caddisfly.sensor.titration.widget.FinderPatternIndicatorView;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.CameraPreview;

import java.lang.ref.WeakReference;

import timber.log.Timber;

@SuppressWarnings("deprecation")
public class TitrationMeasureActivity extends BaseActivity implements TitrationMeasureListener,
        CameraPreview.OnSurfaceChangedListener {

    public static final boolean DEBUG = false;
    // a handler to handle the state machine of the preview, capture, decode, fullCapture cycle
    private TitrationTestHandler titrationtestHandler;
    private FinderPatternIndicatorView mFinderPatternIndicatorView;
    @Nullable
    private WeakReference<Camera> wrCamera;
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private FrameLayout previewLayout;
    //    private SoundPoolPlayer sound;
    private WeakReference<TitrationMeasureActivity> mActivity;
    private TestInfo testInfo;
    private TitrationMeasureFragment titrationMeasureFragment;
    // CameraOperationsManager wraps the camera API
    private TitrationCameraManager mCameraOpsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        sound = new SoundPoolPlayer(this);

        setContentView(R.layout.activity_titration_measure);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFinderPatternIndicatorView = findViewById(R.id.finder_indicator);

        mActivity = new WeakReference<>(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        if (testInfo != null && testInfo.getUuid() != null) {
            setTitle(testInfo.getName());
        } else {
            finish();
        }

        if (mCameraOpsManager == null) {
            mCameraOpsManager = new TitrationCameraManager(this, testInfo.getName());
        }

        // create titrationTestHandler
        // as this handler is created on the current thread, it is part of the UI thread.
        // So we don't want to do actual work on it - just coordinate.
        // The camera and the decoder get their own thread.
        if (titrationtestHandler == null) {
            titrationtestHandler = new TitrationTestHandler(this, getApplicationContext(),
                    mCameraOpsManager, mFinderPatternIndicatorView, testInfo);
        }

        mCameraOpsManager.setTitrationTestHandler(titrationtestHandler);

        titrationtestHandler.setStatus(TitrationTestHandler.State.MEASURE);

        TimeDelayDetail timeDelay = new TimeDelayDetail(1, 0);
        titrationtestHandler.setTestData(timeDelay);

        // initialize camera and start camera preview
        startCameraPreview();
    }

    private void startCameraPreview() {
        previewLayout = findViewById(R.id.camera_preview);
        mCameraPreview = mCameraOpsManager.initCamera(this);

        mCamera = mCameraPreview.getCamera();
        if (mCamera == null) {
            Toast.makeText(this.getApplicationContext(), "Could not instantiate the camera",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            try {
                wrCamera = new WeakReference<>(mCamera);
                previewLayout.removeAllViews();
                if (mCameraPreview != null) {
                    previewLayout.addView(mCameraPreview);
                } else {
                    finish();
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    // started from within camera preview
    public void initPreviewFragment() {
        try {
            if (titrationMeasureFragment == null) {
                titrationMeasureFragment = TitrationMeasureFragment.newInstance(titrationtestHandler);
                titrationtestHandler.setFragment(titrationMeasureFragment);
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.layout_cameraPlaceholder, titrationMeasureFragment
                ).commit();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void moveToInstructions(int testStage) {
    }

    @Override
    public void moveToStripMeasurement() {

        if (titrationMeasureFragment == null) {
            titrationMeasureFragment = TitrationMeasureFragment.newInstance(titrationtestHandler);
        }

        getSupportFragmentManager().beginTransaction().replace(
                R.id.layout_cameraPlaceholder, titrationMeasureFragment).commit();
        titrationtestHandler.setStatus(TitrationTestHandler.State.MEASURE);
        titrationMeasureFragment.clearProgress();
        titrationMeasureFragment.setMeasureText();

        // hand over to state machine
        MessageUtils.sendMessage(titrationtestHandler, TitrationTestHandler.START_PREVIEW_MESSAGE, 0);
    }

    @Override
    public void moveToResults() {
        // move to results activity
        Intent resultIntent = new Intent(getIntent());
        resultIntent.setClass(this, ResultActivity.class);
        resultIntent.putExtra(ConstantKey.TEST_INFO, testInfo);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(resultIntent);
        ResultActivity.setDecodeData(TitrationTestHandler.getDecodeData());
        finish();
    }

    @Override
    public void playSound() {

    }

    @Override
    public void updateTimer(int value) {

    }

    @Override
    public void showTimer() {

    }

    @Override
    public void onPause() {
        releaseResources();
        if (!isFinishing()) {
            finish();
        }
        super.onPause();
    }

    private void releaseResources() {
        if (mCamera != null) {
//            mCameraOpsManager.stopAutofocus();
            mCameraOpsManager.stopCamera();
            mCamera.setOneShotPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (titrationtestHandler != null) {
            titrationtestHandler.quitSynchronously();
            titrationtestHandler = null;
        }

        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }
        if (wrCamera != null) {
            wrCamera.clear();
            wrCamera = null;
        }

        if (mCameraPreview != null && previewLayout != null) {
//            previewLayout.removeView(mCameraPreview);
            mCameraPreview = null;
        }
    }

    /**
     * Store previewLayout info in global properties for later use.
     * w: actual size of the preview window
     * h: actual size of the preview window
     * previewImageWidth: size of image returned from camera
     * previewImageHeight: size of image returned from camera
     */
    public void setPreviewProperties(int w, int h, int previewImageWidth, int previewImageHeight) {
        if (mCamera != null && mCameraPreview != null) {
            TitrationTestHandler.mDecodeData.setPreviewWidth(w);
            TitrationTestHandler.mDecodeData.setPreviewHeight(h);
            TitrationTestHandler.mDecodeData.setDecodeWidth(previewImageWidth);
            TitrationTestHandler.mDecodeData.setDecodeHeight(previewImageHeight);

            mFinderPatternIndicatorView.setMeasure(w, h, previewImageWidth, previewImageHeight);
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

    @Override
    public void onSurfaceChanged(int w, int h, int previewWidth, int previewHeight) {
        setPreviewProperties(w, h, previewWidth, previewHeight);
        initPreviewFragment();
    }
}
