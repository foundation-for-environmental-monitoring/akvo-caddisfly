/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.chamber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.FragmentRunTestBinding;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SoundUtil;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static org.akvo.caddisfly.common.AppConfig.STOP_ANIMATIONS;

//import timber.log.Timber;

public class BaseRunTest extends Fragment implements RunTest {
    private static final double SHORT_DELAY = 1;
    private final int[] countdown = {0};
    private final ArrayList<ResultDetail> results = new ArrayList<>();
    private final ArrayList<ResultDetail> oneStepResults = new ArrayList<>();
    private final Handler delayHandler = new Handler();
    protected FragmentRunTestBinding binding;
    protected boolean cameraStarted;
    protected int pictureCount = 0;
    private boolean timeDelayEnabled = true;
    private int timeDelay = 0;
    private final Runnable mCountdown = this::setCountDown;
    private final Handler mHandler = new Handler();
    private AlertDialog alertDialogToBeDestroyed;
    private TestInfo mTestInfo;
    private Calibration mCalibration;
    private int dilution = 1;
    private int retryCount = 0;
    private Camera mCamera;
    private OnResultListener mListener;
    private ChamberCameraPreview mCameraPreview;
    private boolean isFlashSet;
    private final Runnable mRunnableCode = () -> {
        if (pictureCount < AppPreferences.getSamplingTimes()) {
            turnFlashOn();
            pictureCount++;
            takePicture();
        } else {
            releaseResources();
        }
    };
    private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            mCamera.startPreview();

            Bitmap bitmap = ImageUtil.getBitmap(data);

            getAnalyzedResult(bitmap);

            if (mTestInfo.getResults().get(0).getTimeDelay() > 0) {
                // test has time delay so take the pictures quickly with short delay
                mHandler.postDelayed(mRunnableCode, (long) (SHORT_DELAY * 1000));
            } else {
                mHandler.postDelayed(mRunnableCode, ChamberTestConfig.DELAY_BETWEEN_SAMPLING * 1000);
            }
        }
    };

    private static String timeConversion(int seconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int minutes = seconds / SECONDS_IN_A_MINUTE;
        seconds -= minutes * SECONDS_IN_A_MINUTE;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes -= hours * MINUTES_IN_AN_HOUR;

        return String.format(Locale.US, "%02d", hours) + ":" +
                String.format(Locale.US, "%02d", minutes) + ":" +
                String.format(Locale.US, "%02d", seconds);
    }

    private void setCountDown() {
        if (!AppPreferences.useCameraAboveMode() && timeDelayEnabled && countdown[0] < timeDelay) {
            binding.timeLayout.setVisibility(View.VISIBLE);

            countdown[0]++;

            if (timeDelay > 10) {
                if ((timeDelay - countdown[0]) < 31) {
                    SoundUtil.playShortResource(getActivity(), R.raw.beep);
                } else if ((timeDelay - countdown[0]) % 15 == 0) {
                    SoundUtil.playShortResource(getActivity(), R.raw.beep);
                }
            }

//            binding.countdownTimer.setProgress(timeDelay - countdown[0], timeDelay);
            binding.textTimeRemaining.setText(timeConversion(timeDelay - countdown[0]));

            delayHandler.removeCallbacksAndMessages(null);
            delayHandler.postDelayed(mCountdown, 1000);
        } else {
            binding.timeLayout.setVisibility(View.GONE);
            binding.layoutWait.setVisibility(View.VISIBLE);
            waitForStillness();
        }
    }

    protected void waitForStillness() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mCalibration != null && getActivity() != null) {

            // disable the key guard when device wakes up and shake alert is displayed
            getActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
    }

    protected void initializeTest() {
        pictureCount = 0;
        results.clear();
        oneStepResults.clear();
        binding.cameraView.setVisibility(View.GONE);
    }

    protected void setupCamera() {
        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new ChamberCameraPreview(getActivity());
        mCamera = mCameraPreview.getCamera();
        mCameraPreview.setupCamera(mCamera);
        binding.cameraView.addView(mCameraPreview);

        binding.cameraView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.cameraView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int parentHeight = ((FrameLayout) binding.cameraView.getParent()).getMeasuredHeight();
                        mCamera = mCameraPreview.getCamera();
                        try {
                            FrameLayout.LayoutParams layoutParams = null;
                            int offset = (parentHeight * AppPreferences.getCameraCenterOffset())
                                    / mCamera.getParameters().getPictureSize().width;

                            layoutParams = (FrameLayout.LayoutParams) binding.circleView.getLayoutParams();

                            Resources r = requireContext().getResources();
                            int offsetPixels = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    offset,
                                    r.getDisplayMetrics()
                            );
                            layoutParams.setMargins(0, 0, 0, offsetPixels);
                            binding.circleView.setLayoutParams(layoutParams);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    protected void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            isFlashSet = false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_run_test,
                container, false);

        binding.waitingProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(requireActivity(),
                        R.color.white), PorterDuff.Mode.SRC_IN);

        if (STOP_ANIMATIONS) {
            binding.waitingProgressBar.setVisibility(View.GONE);
        }

        pictureCount = 0;

        if (getArguments() != null) {
            mTestInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);
        }

        if (mTestInfo != null) {
            mTestInfo.setDilution(dilution);
        }

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(mTestInfo);

        binding.setVm(model);

        initializeTest();

        countdown[0] = 0;

        if (mCalibration != null && getActivity() != null) {
            start();
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof OnResultListener) {
            mListener = (OnResultListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void takePicture() {

        if (!cameraStarted) {
            return;
        }

        SoundUtil.playShortResource(getActivity(), R.raw.beep);

        mCamera.startPreview();
        turnFlashOn();

        mCamera.takePicture(null, null, mPicture);

    }

    /**
     * Get the test result by analyzing the bitmap.
     *
     * @param bitmap the bitmap of the photo taken during analysis
     */
    private void getAnalyzedResult(@NonNull Bitmap bitmap) {

        bitmap = ImageUtil.rotateImage(requireActivity(), bitmap);

        Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        //Extract the color from the photo which will be used for comparison
        ColorInfo photoColor;
        if (croppedBitmap != null) {

            if (mTestInfo.getResults().get(0).getGrayScale()) {
                croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
            }

            photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                    ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

            if (mCalibration != null) {
                mCalibration.color = photoColor.getColor();
                mCalibration.date = new Date().getTime();
            }

            ResultDetail resultDetail = SwatchHelper.analyzeColor(mTestInfo.getSwatches().size(),
                    photoColor, mTestInfo.getSwatches());

            ResultDetail oneStepResultDetail = SwatchHelper.analyzeColor(mTestInfo.getOneStepSwatches().size(),
                    photoColor, mTestInfo.getOneStepSwatches());

            resultDetail.setBitmap(bitmap);
            resultDetail.setCroppedBitmap(croppedBitmap);
            resultDetail.setDilution(dilution);
            resultDetail.setQuality(photoColor.getQuality());

//            Timber.d("Result is: " + String.valueOf(resultDetail.getResult()));

            results.add(resultDetail);
            oneStepResults.add(oneStepResultDetail);

            if (mListener != null && pictureCount >= AppPreferences.getSamplingTimes()) {
                // ignore the first two results
                for (int i = 0; i < ChamberTestConfig.SKIP_SAMPLING_COUNT; i++) {
                    if (results.size() > 1) {
                        results.remove(0);
                        oneStepResults.remove(0);
                    }
                }

                releaseResources();
                mListener.onResult(results, oneStepResults, mCalibration, Activity.RESULT_OK);
            }
        }
    }

    @Override
    public void setCalibration(Calibration item) {
        mCalibration = item;
    }

    @Override
    public void setDilution(int dilution) {
        this.dilution = dilution;
    }

    @Override
    public void start() {
        // If the test has a time delay config then use that otherwise use standard delay
        if (timeDelayEnabled && mTestInfo.getResults().get(0).getTimeDelay() > 10 && retryCount < 1) {
            timeDelay = (int) Math.max(SHORT_DELAY, mTestInfo.getResults().get(0).getTimeDelay());

            binding.timeLayout.setVisibility(View.VISIBLE);

            setCountDown();
        } else {
            waitForStillness();
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
        initializeTest();
        countdown[0] = 0;
    }

    @Override
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public void setSkipTimeDelay(boolean value) {
        timeDelayEnabled = !value;
    }

    protected void startRepeatingTask() {
        mRunnableCode.run();
    }

    private void stopRepeatingTask() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnableCode);
        }
    }

    protected void startTest() {
        if (!cameraStarted) {

            setupCamera();

            cameraStarted = true;

            SoundUtil.playShortResource(getActivity(), R.raw.futurebeep2);

            int initialDelay = 0;

            //If the test has a time delay config then use that otherwise use standard delay
            if (mTestInfo.getResults().get(0).getTimeDelay() < 5) {
                initialDelay = ChamberTestConfig.DELAY_INITIAL + ChamberTestConfig.DELAY_BETWEEN_SAMPLING;
            }

            binding.layoutWait.setVisibility(View.VISIBLE);

            delayHandler.removeCallbacksAndMessages(null);
            delayHandler.postDelayed(mRunnableCode, initialDelay * 1000);
        }
    }

    /**
     * Turn flash off.
     */
    protected void turnFlashOff() {
        if (!isFlashSet) return;
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            String flashMode = Camera.Parameters.FLASH_MODE_OFF;
            parameters.setFlashMode(flashMode);
            mCamera.setParameters(parameters);
        } catch (Exception ignored) {
        }
        isFlashSet = false;
    }

    /**
     * Turn flash on.
     */
    protected void turnFlashOn() {
        if (isFlashSet) return;
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(AppPreferences.getFlashMode());
            mCamera.setParameters(parameters);
        } catch (Exception ignored) {
        }
        isFlashSet = true;
    }

    protected void releaseResources() {

        stopRepeatingTask();

        if (mCamera != null) {
            turnFlashOff();
            stopPreview();
            mCamera.setPreviewCallback(null);
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCamera.release();
            mCamera = null;
        }
        if (mCameraPreview != null) {
            mCameraPreview.destroyDrawingCache();
        }

        delayHandler.removeCallbacksAndMessages(null);

        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }

        cameraStarted = false;
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    protected void showError(String message,
                             @SuppressWarnings("SameParameterValue") final Bitmap bitmap,
                             Activity activity) {

        stopScreenPinning(activity);

        releaseResources();

        SoundUtil.playShortResource(getActivity(), R.raw.err);

        alertDialogToBeDestroyed = AlertUtil.showError(activity,
                R.string.error, message, bitmap, R.string.ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    activity.setResult(Activity.RESULT_CANCELED);

                    stopScreenPinning(getActivity());

//                    activity.finish();

                    mListener.onResult(results, oneStepResults, mCalibration, Activity.RESULT_CANCELED);
                }, null, null
        );
    }

    private void stopScreenPinning(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                activity.stopLockTask();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseResources();
    }

    public interface OnResultListener {
        void onResult(ArrayList<ResultDetail> results, ArrayList<ResultDetail> oneStepResults,
                      Calibration calibration, int cancelled);
    }
}
