package org.akvo.caddisfly.sensor.titration.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextSwitcher;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;
import org.akvo.caddisfly.sensor.titration.camera.TitrationCameraManager;
import org.akvo.caddisfly.sensor.titration.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.titration.models.DecodeData;
import org.akvo.caddisfly.sensor.titration.models.TimeDelayDetail;
import org.akvo.caddisfly.sensor.titration.widget.FinderPatternIndicatorView;

import timber.log.Timber;

public final class TitrationTestHandler extends Handler {
    // Message types
    public static final int START_PREVIEW_MESSAGE = 1;
    public static final int DECODE_IMAGE_CAPTURED_MESSAGE = 2;
    public static final int DECODE_SUCCEEDED_MESSAGE = 4;
    public static final int DECODE_FAILED_MESSAGE = 5;
    public static final int EXPOSURE_OK_MESSAGE = 6;
    public static final int CHANGE_EXPOSURE_MESSAGE = 7;
    public static final int SHADOW_QUALITY_OK_MESSAGE = 8;
    //    public static final int CALIBRATION_DONE_MESSAGE = 10;
    public static final int IMAGE_SAVED_MESSAGE = 11;
    public static final DecodeData mDecodeData = new DecodeData();
    private static final int SHADOW_QUALITY_FAILED_MESSAGE = 9;
    private static final int DECODE_IMAGE_CAPTURE_FAILED_MESSAGE = 3;
    private static State mState;
    private static int decodeFailedCount = 0;
    private static int successCount = 0;
    private static int nextPatch;
    private static int numPatches;
    private static boolean captureNextImage;
    // camera manager instance
    private final TitrationCameraManager mCameraOpsManager;
    // finder pattern indicator view
    private final FinderPatternIndicatorView mFinderPatternIndicatorView;
    private final TitrationMeasureListener mListener;
    private final Context context;
    // decode processor instance
    private DecodeProcessor mDecodeProcessor;
    private TitrationMeasureFragment mFragment;
    private TextSwitcher mTextSwitcher;
    private int shadowQualityFailedCount = 0;
    //    private int tiltFailedCount = 0;
    private int distanceFailedCount = 0;
    private String currentMessage = "";
    private String currentShadowMessage = "";
    private String newMessage = "";
    private String defaultMessage;
    private int mQualityScore = 0;
    //    private long startTimeMillis;
    private int currentTestStage = 1;
    private TimeDelayDetail mPatchTimeDelaysUnfiltered;

    TitrationTestHandler(Context context1, Context context, TitrationCameraManager cameraOpsManager,
                         FinderPatternIndicatorView finderPatternIndicatorView, TestInfo testInfo) {

        mListener = (TitrationMeasureListener) context1;
        this.mCameraOpsManager = cameraOpsManager;
        this.mFinderPatternIndicatorView = finderPatternIndicatorView;

        // create instance of the decode processor
        if (mDecodeProcessor == null) {
            mDecodeProcessor = new DecodeProcessor(this);
        }
        mDecodeData.setTestInfo(testInfo);
        this.context = context;
    }

    public static DecodeData getDecodeData() {
        return mDecodeData;
    }

    public void setTextSwitcher(TextSwitcher textSwitcher) {
        this.mTextSwitcher = textSwitcher;
    }

    public void setFragment(TitrationMeasureFragment fragment) {
        mFragment = fragment;
    }

    public void setTestData(TimeDelayDetail timeDelay) {
        mPatchTimeDelaysUnfiltered = timeDelay;
    }

    public void setStatus(State state) {
        mState = state;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case START_PREVIEW_MESSAGE:
                Timber.d("START_PREVIEW_MESSAGE received in striptest handler");
                // start the image capture request.
//                mCameraOpsManager.startAutofocus();
                successCount = 0;
                mQualityScore = 0;

//                startTimeMillis = System.currentTimeMillis();
                nextPatch = 0;
                numPatches = 1;

                captureNextImage = false;

                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case DECODE_IMAGE_CAPTURED_MESSAGE:
                Timber.d("DECODE_IMAGE_CAPTURED_MESSAGE received in striptest handler");

                captureNextImage = true;

                // set message shown if there are no problems
                if (mState.equals(State.MEASURE)) {
                    defaultMessage = context.getString(R.string.ready_for_photo);
                } else {
                    defaultMessage = context.getString(R.string.checking_image_quality);
                }

                // start trying to find finder patterns on decode thread
                mDecodeProcessor.startFindPossibleCenters();
                break;

            case DECODE_IMAGE_CAPTURE_FAILED_MESSAGE:
                if (TitrationMeasureActivity.DEBUG) {
                    Timber.d("DECODE_IMAGE_CAPTURE_FAILED_MESSAGE received in titration test handler");
                    mDecodeData.clearData();
                    mFinderPatternIndicatorView.clearAll();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }
                break;

            case DECODE_SUCCEEDED_MESSAGE:
                if (TitrationMeasureActivity.DEBUG) {
                    Timber.d("DECODE_SUCCEEDED_MESSAGE received in titration test handler");
                    FinderPatternInfo fpInfo = mDecodeData.getPatternInfo();
                    if (fpInfo != null) {
                        Timber.d("found codes:" + fpInfo.getBottomLeft().toString() + "," +
                                fpInfo.getBottomRight().toString() + "," +
                                fpInfo.getTopLeft().toString() + "," +
                                fpInfo.getTopRight().toString() + ",");
                    }
                }

                boolean showTiltMessage = false;
                boolean showDistanceMessage = false;
                decodeFailedCount = 0;

//                if (mDecodeData.getTilt() != DecodeProcessor.NO_TILT) {
//                    tiltFailedCount = Math.min(8, tiltFailedCount + 1);
//                    if (tiltFailedCount > 4) showTiltMessage = true;
//                    else {
//                        if (tiltFailedCount < 4) showTiltMessage = false;
//                    }
//                } else {
//                    tiltFailedCount = Math.max(0, tiltFailedCount - 1);
//                }

                if (!mDecodeData.getDistanceOk()) {
                    distanceFailedCount = Math.min(8, distanceFailedCount + 1);
                    if (distanceFailedCount > 4) showDistanceMessage = true;
                    else {
                        if (distanceFailedCount < 4) showDistanceMessage = false;
                    }
                } else {
                    distanceFailedCount = Math.max(0, distanceFailedCount - 1);
                }

                if (showDistanceMessage) {
                    newMessage = context.getString(R.string.move_camera_closer);
                }
//
//                if (showTiltMessage) {
//                    newMessage = context.getString(R.string.tilt_camera_in_direction);
//                    showDistanceMessage = false;
//                } else {
//                }

                if (!showTiltMessage && !showDistanceMessage) {
                    newMessage = defaultMessage;
                }

                if (!newMessage.equals(currentMessage)) {
                    mTextSwitcher.setText(newMessage);
                    currentMessage = newMessage;
                }

                // show patterns
                mFinderPatternIndicatorView.showPatterns(mDecodeData.getFinderPatternsFound(),
                        mDecodeData.getTilt(), showTiltMessage, showDistanceMessage, mDecodeData.getDecodeWidth(), mDecodeData.getDecodeHeight());

                // move on to exposure quality check

                // if tilt or distance are not ok, start again
                if (mDecodeData.getPatternInfo().getTopRight().getY() == 0 ||
                        mDecodeData.getTilt() != DecodeProcessor.NO_TILT || !mDecodeData.getDistanceOk()) {
                    mDecodeData.clearData();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                    break;
                }

                // if we are here, all is well and we can proceed
                mDecodeProcessor.startExposureQualityCheck();

                break;

            case DECODE_FAILED_MESSAGE:

                Timber.d("DECODE_FAILED_MESSAGE received in titration test handler");

                decodeFailedCount++;
                mDecodeData.clearData();
                if (decodeFailedCount > 5) {
                    mFinderPatternIndicatorView.clearAll();
                }
                mQualityScore *= 0.9;
                mFragment.showQuality(mQualityScore);
                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case CHANGE_EXPOSURE_MESSAGE:
                Timber.d("exposure - CHANGE_EXPOSURE_MESSAGE received in striptest handler, with argument:%s", message.arg1);

                int direction = message.arg1;

                // change exposure
                mCameraOpsManager.changeExposure(direction);
                mDecodeData.clearData();
                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case EXPOSURE_OK_MESSAGE:
                Timber.d("exposure - EXPOSURE_OK_MESSAGE received in striptest handler");

                // if we arrive here, we both have loaded a valid calibration card file,
                // and the exposure is ok. So we can proceed to the next step: checking shadows.
//                mDecodeProcessor.startShadowQualityCheck();

                if (mState.equals(State.MEASURE) && captureNextImage) {
                    captureNextImage = false;
                    mDecodeProcessor.storeImageData(mPatchTimeDelaysUnfiltered.getTimeDelay());

//                    mDecodeData.clearData();
//                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }

                break;

            case SHADOW_QUALITY_FAILED_MESSAGE:
                Timber.d("SHADOW_QUALITY_FAILED_MESSAGE received in striptest handler");

                shadowQualityFailedCount = Math.min(8, shadowQualityFailedCount + 1);
                String newShadowMessage;
                if (shadowQualityFailedCount > 4) {
                    newShadowMessage = context.getString(R.string.avoid_shadows_on_card);
                } else {
                    newShadowMessage = "";
                }

                if (!currentShadowMessage.equals(newShadowMessage)) {
                    mTextSwitcher.setText(newShadowMessage);
                    currentShadowMessage = newShadowMessage;
                }

                mFinderPatternIndicatorView.showShadow(mDecodeData.getShadowPoints(),
                        mDecodeData.getPercentageShadow(), mDecodeData.getCardToImageTransform());

                // start another decode image capture request
                mDecodeData.clearData();
                mCameraOpsManager.setDecodeImageCaptureRequest();

                break;

            case SHADOW_QUALITY_OK_MESSAGE:
                Timber.d("SHADOW_QUALITY_OK_MESSAGE received in striptest handler");

                shadowQualityFailedCount = Math.max(0, shadowQualityFailedCount - 1);
                if (shadowQualityFailedCount > 4) {
                    newShadowMessage = context.getString(R.string.avoid_shadows_on_card);
                } else {
                    newShadowMessage = "";
                }

                if (!currentShadowMessage.equals(newShadowMessage)) {
                    mTextSwitcher.setText(newShadowMessage);
                    currentShadowMessage = newShadowMessage;
                }

                mFinderPatternIndicatorView.showShadow(mDecodeData.getShadowPoints(),
                        mDecodeData.getPercentageShadow(), mDecodeData.getCardToImageTransform());

                int quality = qualityPercentage(mDecodeData.getDeltaEStats());
                if (mFragment != null) {
                    mFragment.showQuality(quality);
                    if (mState == State.MEASURE && quality > Constants.CALIB_PERCENTAGE_LIMIT) {
                        successCount++;
                        mFragment.setProgress(successCount);
                    }
                }

                if (mState.equals(State.MEASURE) && captureNextImage) {
                    captureNextImage = false;
                    mDecodeProcessor.storeImageData(mPatchTimeDelaysUnfiltered.getTimeDelay());

//                    mDecodeData.clearData();
//                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }

                break;

            case IMAGE_SAVED_MESSAGE:
                mListener.playSound();
                if (nextPatch < numPatches - 1) {
                    nextPatch++;

                    // start another decode image capture request
                    mDecodeData.clearData();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                } else {
                    int totalTestStages = 1;
                    if (currentTestStage < totalTestStages) {
                        // if all are stages are not completed then show next instructions
                        currentTestStage++;
                        mListener.moveToInstructions(currentTestStage);
                    } else {

                        //TODO: remove debug stuff
                        //                       mDecodeData.saveImage();

                        // we are done
                        mListener.moveToResults();
                    }
                }
                break;
            default:
        }
    }

    private int qualityPercentage(float[] deltaEStats) {
        // we consider anything lower than 2.5 to be good.
        // anything higher than 4.5 is red.
        double score = 0;
        if (deltaEStats != null) {
            score = Math.round(100.0 * (1 - Math.min(1.0, (Math.max(0, deltaEStats[1] - 2.5) / 2.0))));
        }

        // if the quality is improving, we show a high number quickly, if it is
        // going down, we go down more slowly.
        if (score > mQualityScore) {
            mQualityScore = (int) Math.round((mQualityScore + score) / 2.0);
        } else {
            mQualityScore = (int) Math.round((5 * mQualityScore + score) / 6.0);
        }
        return mQualityScore;
    }

//    private int timeElapsedSeconds() {
//        return (int) Math.floor((Constants.MEASURE_TIME_COMPENSATION_MILLIS + System.currentTimeMillis() - startTimeMillis) / 1000);
//    }

    public void quitSynchronously() {
        mDecodeProcessor.stopDecodeThread();
    }

    public enum State {
        PREPARE, MEASURE
    }
}