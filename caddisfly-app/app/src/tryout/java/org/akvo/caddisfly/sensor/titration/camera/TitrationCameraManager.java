package org.akvo.caddisfly.sensor.titration.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.striptest.utils.MessageUtils;
import org.akvo.caddisfly.sensor.titration.ui.TitrationTestHandler;
import org.akvo.caddisfly.util.CameraPreview;
import org.akvo.caddisfly.util.ImageUtil;

public class TitrationCameraManager {

    private Handler mCameraHandler;

    private Camera mCamera;

    private TitrationTestHandler titrationTestHandler;

    //debug code
    private byte[] bytes;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] imageData, Camera camera) {

            if (bytes != null && bytes.length > 0 && AppPreferences.isTestMode()) {
                // Use test image if we are in test mode
                TitrationTestHandler.mDecodeData.setDecodeImageByteArray(bytes);

            } else {
//                ImageUtil.saveImageBytes(context, camera, imageData, FileHelper.FileType.TEST_IMAGE,
//                        String.valueOf(Calendar.getInstance().getTimeInMillis()));

                // store image for later use
                TitrationTestHandler.mDecodeData.setDecodeImageByteArray(imageData);
            }
            MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.DECODE_IMAGE_CAPTURED_MESSAGE, 0);
        }
    };


    public TitrationCameraManager(Context context, String name) {
        if (AppPreferences.isTestMode()) {
            bytes = ImageUtil.loadImageBytes(name, FileHelper.FileType.TEST_IMAGE);
        }
    }

    public CameraPreview initCamera(Context context) {
        startCameraThread();

        // open the camera and create a preview surface for it
        CameraPreview cameraPreview = new CameraPreview(context, Camera.Parameters.FLASH_MODE_OFF);
        mCamera = cameraPreview.getCamera();
        return cameraPreview;
    }

    public void setTitrationTestHandler(TitrationTestHandler titrationTestHandler) {
        this.titrationTestHandler = titrationTestHandler;
    }

    public void setDecodeImageCaptureRequest() {
        if (mCameraHandler != null && mCamera != null) {
            try {
                mCameraHandler.post(() -> {
                    if (mCamera != null) {
                        mCamera.setOneShotPreviewCallback(previewCallback);
                    }
                });
            } catch (Exception ignored) {
                // do nothing
            }
        }
    }

    public void changeExposure(int exposureChange) {
        int expComp = mCamera.getParameters().getExposureCompensation();
        int newSetting = expComp + exposureChange;

        // if we are within bounds, change the capture request
        if (newSetting != expComp &&
                newSetting <= mCamera.getParameters().getMaxExposureCompensation() &&
                newSetting >= mCamera.getParameters().getMinExposureCompensation()) {
            mCamera.stopPreview();
            Camera.Parameters cameraParam = mCamera.getParameters();
            cameraParam.setExposureCompensation(newSetting);
            mCamera.setParameters(cameraParam);
            mCamera.startPreview();
        }
    }

    public void stopCamera() {
        mCamera = null;
        mCameraHandler = null;
    }

    private void startCameraThread() {
        HandlerThread mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }
}
