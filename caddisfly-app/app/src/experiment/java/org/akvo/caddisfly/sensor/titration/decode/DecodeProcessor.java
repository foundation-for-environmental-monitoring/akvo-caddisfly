package org.akvo.caddisfly.sensor.titration.decode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;

import org.akvo.caddisfly.sensor.qrdetector.BitMatrix;
import org.akvo.caddisfly.sensor.qrdetector.BitMatrixCreator;
import org.akvo.caddisfly.sensor.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;
import org.akvo.caddisfly.sensor.striptest.utils.MessageUtils;
import org.akvo.caddisfly.sensor.titration.TitrationConstants;
import org.akvo.caddisfly.sensor.titration.models.DecodeData;
import org.akvo.caddisfly.sensor.titration.qrdetector.FinderPatternFinder;
import org.akvo.caddisfly.sensor.titration.ui.TitrationTestHandler;

import java.util.List;

import androidx.annotation.Nullable;

public class DecodeProcessor {

    public static final int NO_TILT = -1;
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_0 = 0;
    // holds reference to the titrationTestHandler, which we need to pass messages
    private TitrationTestHandler titrationTestHandler;

    /********************************** check exposure ******************************************/
    private final Runnable runExposureQualityCheck = () -> {
        try {
            checkExposureQuality();
        } catch (Exception e) {
            if (titrationTestHandler != null) {
                MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.CHANGE_EXPOSURE_MESSAGE, 2);
            }
        }
    };
    /*********************************** check shadow quality ***********************************/
    private final Runnable runShadowQualityCheck = () -> {
        try {
            checkShadowQuality();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
        }
    };
    /*********************************** store data *********************************************/
    private final Runnable runStoreData = () -> {
        try {
            storeData();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
        }
    };
    private HandlerThread mDecodeThread;
    private Handler mDecodeHandler;
    // instance of BitMatrixCreator
    private BitMatrixCreator mBitMatrixCreator;
    /******************************************* find possible centers **************************/
    private final Runnable runFindPossibleCenters = () -> {
        try {
            findPossibleCenters();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
            //throw new RuntimeException("Can't start finding centers");
        }
    };
    private int mCurrentDelay;

    public DecodeProcessor(TitrationTestHandler titrationTestHandler) {
        mDecodeThread = new HandlerThread("DecodeProcessor");
        mDecodeThread.start();
        mDecodeHandler = new Handler(mDecodeThread.getLooper());
        this.titrationTestHandler = titrationTestHandler;
    }

    private static float distance(double x1, double y1, double x2, double y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    //method to calculate the amount of perspective, based on the difference of distances at the top and sides
    // horizontal and vertical are according to calibration card in landscape view
    @Nullable
    private static float[] getTilt(@Nullable FinderPatternInfo info) {
        if (info == null) {
            return null;
        }

        // compute distances
        // in info, we have topLeft, topRight, bottomLeft, bottomRight
        float hDistanceTop = distance(info.getBottomLeft().getX(), info.getBottomLeft().getY(),
                info.getTopLeft().getX(), info.getTopLeft().getY());
        float hDistanceBottom = distance(info.getBottomRight().getX(), info.getBottomRight().getY(),
                info.getTopRight().getX(), info.getTopRight().getY());
        float vDistanceLeft = distance(info.getBottomLeft().getX(), info.getBottomLeft().getY(),
                info.getBottomRight().getX(), info.getBottomRight().getY());
        float vDistanceRight = distance(info.getTopRight().getX(), info.getTopRight().getY(),
                info.getTopLeft().getX(), info.getTopLeft().getY());

        // return ratio of horizontal distances top and bottom and ratio of vertical distances left and right
        return new float[]{hDistanceTop / hDistanceBottom, vDistanceLeft / vDistanceRight};
    }

    @SuppressWarnings("SameParameterValue")
    private static float capValue(float val, float min, float max) {
        if (val > max) {
            return max;
        }
        return val < min ? min : val;
    }

    /**
     * Converts YUV420 NV21 to Y888 (RGB8888). The grayscale image still holds 3 bytes on the pixel.
     *
     * @param data   byte array on YUV420 NV21 format.
     * @param width  pixels width
     * @param height pixels height
     */
    public static int[] applyGrayScale(byte[] data, int width, int height) {

        int pixelCount = width * height;
        int[] pixels = new int[pixelCount];
        int p;
        int size = width * height;
        for (int i = 0; i < size; i++) {
            p = data[i] & 0xFF;
            pixels[i] = 0xff000000 | p << 16 | p << 8 | p;
        }

        return pixels;
    }

    public static int maxY(DecodeData decodeData, FinderPattern fp) {
        // Compute expected area finder pattern
        float x = fp.getX();
        float y = fp.getY();

        float size = fp.getEstimatedModuleSize();
        int halfWidth = (int) (size * 3.5); // one finder pattern has size 7
        int xTopLeft = Math.max((int) (x - halfWidth), 0);
        int yTopLeft = Math.max((int) (y - halfWidth), 0);
        int xBotRight = Math.min((int) (x + halfWidth), decodeData.getDecodeWidth() - 1);
        int yBotRight = Math.min((int) (y + halfWidth), decodeData.getDecodeHeight() - 1);
        int maxY = 0;

        int rowStride = decodeData.getDecodeWidth();
        byte[] yDataArray = decodeData.getDecodeImageByteArray();

        // iterate over all points and get max Y value
        int i;
        int j;
        int yVal;
        for (j = yTopLeft; j <= yBotRight; j++) {
            int offset = j * rowStride;
            for (i = xTopLeft; i <= xBotRight; i++) {
                yVal = yDataArray[offset + i] & 0xff;
                if (yVal > maxY) {
                    maxY = yVal;
                }
            }
        }
        return maxY;
    }

    public void startFindPossibleCenters() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runFindPossibleCenters);
            mDecodeHandler.post(runFindPossibleCenters);
        }
//        else {
        // TODO find out how we gracefully get out in this case
        // throw new RuntimeException("can't find possible centers");
//        }
    }

    private void findPossibleCenters() {
        List<FinderPattern> possibleCenters;
        FinderPatternInfo patternInfo;
        BitMatrix bitMatrix;

        DecodeData decodeData = TitrationTestHandler.getDecodeData();

        final int decodeHeight = decodeData.getDecodeHeight();
        final int decodeWidth = decodeData.getDecodeWidth();

        if (mBitMatrixCreator == null) {
            mBitMatrixCreator = new BitMatrixCreator(decodeWidth, decodeHeight);
        }

        // create a black and white bit matrix from our data. Cut out the part that interests us
        try {
            bitMatrix = BitMatrixCreator.createBitMatrix(decodeData.getDecodeImageByteArray(),
                    decodeWidth, decodeWidth, decodeHeight, 0, 0,
                    Math.round(decodeWidth * 1),
                    decodeHeight);
        } catch (Exception e) {
            MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.DECODE_FAILED_MESSAGE, 0);
            return;
        }

        // if we have valid data, try to find the finder patterns
        if (bitMatrix != null && decodeWidth > 0 && decodeHeight > 0) {
            FinderPatternFinder finderPatternFinder = new FinderPatternFinder(bitMatrix);

//            Map<DecodeHintType, ?> hints = new HashMap<>();
//
//            hints.put(DecodeHintType.TRY_HARDER, null);

            try {
                patternInfo = finderPatternFinder.find(null);
                possibleCenters = finderPatternFinder.getPossibleCenters();

                //We only get four finder patterns back. If one of them is very small, we know we have
                // picked up noise and we should break early.
                for (int i = 0; i < possibleCenters.size(); i++) {
                    if (possibleCenters.get(i).getEstimatedModuleSize() < 2) {
                        decodeData.setPatternInfo(null);
                        MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.DECODE_FAILED_MESSAGE, 0);
                        return;
                    }
                }

            } catch (Exception ignored) {
                // patterns where not detected.
                decodeData.setPatternInfo(null);
                MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.DECODE_FAILED_MESSAGE, 0);
                return;
            }

            // compute and store tilt and distance check
//            decodeData.setTilt(getDegrees(getTilt(patternInfo)));
            decodeData.setDistanceOk(distanceOk(patternInfo, decodeHeight));

            // store finder patterns
            decodeData.setPatternInfo(patternInfo);

            decodeData.setFinderPatternsFound(possibleCenters);

            // store decode image size
//            decodeData.setDecodeSize(new Size(decodeWidth, decodeHeight));

            // send the message that the decoding was successful
            MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.DECODE_SUCCEEDED_MESSAGE, 0);
        }
    }

    /********************************* utility methods ******************************************/
    public void stopDecodeThread() {
        mDecodeThread.quitSafely();
        try {
            mDecodeThread.join();
            mDecodeThread = null;
            mDecodeHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean distanceOk(@Nullable FinderPatternInfo info, int decodeHeight) {
        if (info != null && info.getBottomRight().getY() > 0) {
            int cardWidth = (int) Math.abs((info.getBottomRight().getY() - info.getTopLeft().getY()));
            return !(cardWidth < decodeHeight * .64);
        }

        return true;
    }

    private int getDegrees(float[] tiltValues) {
        int degrees;

        // if the horizontal tilt is too large, indicate it
        if (Math.abs(tiltValues[0] - 1) > TitrationConstants.MAX_TILT_DIFF) {
            degrees = tiltValues[0] - 1 < 0 ? -DEGREES_90 : DEGREES_90;
            return degrees;
        }

        // if the vertical tilt is too large, indicate it
        if (Math.abs(tiltValues[1] - 1) > TitrationConstants.MAX_TILT_DIFF) {
            degrees = tiltValues[1] - 1 < 0 ? DEGREES_180 : DEGREES_0;
            return degrees;
        }
        // we don't have a tilt problem
        return NO_TILT;
    }

    public void startExposureQualityCheck() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runExposureQualityCheck);
            mDecodeHandler.post(runExposureQualityCheck);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    /*
     * checks exposure of image, by looking at the Y value of the white. It should be as high as
     * possible, without being overexposed.
     */
    private void checkExposureQuality() {

        DecodeData decodeData = TitrationTestHandler.getDecodeData();

        int top = (int) decodeData.getPatternInfo().getTopLeft().getY();
        int bottom = (int) decodeData.getPatternInfo().getBottomLeft().getY();
        int left = (int) decodeData.getPatternInfo().getBottomLeft().getX();
        int right = (int) decodeData.getPatternInfo().getTopRight().getX();

        int measureCount = 0;

        if ((right - left > 700) && (bottom - top > 500)) {
            byte[] iDataArray = decodeData.getDecodeImageByteArray();
            int width = decodeData.getDecodeWidth();
            int height = decodeData.getDecodeHeight();

            int[] pixels = applyGrayScale(iDataArray, width, height);

            Bitmap tempImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
            Bitmap totalImage = Bitmap.createBitmap(tempImage, left, top,
                    Math.abs(right - left), Math.abs(bottom - top), null, false);
            tempImage.recycle();

            int measureLine = (int) (totalImage.getHeight() * 0.7);

            int col = 30;
            while (col < totalImage.getWidth() - 30) {
                int measurePixel = totalImage.getPixel(col, measureLine);
                int measurePixelCompare = totalImage.getPixel(col - 15, measureLine);
                if (Math.abs(Color.red(measurePixel) - Color.red(measurePixelCompare)) > 50) {
                    if (Color.red(measurePixel) < Color.red(measurePixelCompare)) {
                        measureCount++;
                        col += 15;
                    }
                }
                col++;
            }

            totalImage.recycle();
        }

        if (measureCount == 16) {
            MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.EXPOSURE_OK_MESSAGE, 0);
        } else {
            int maxY;
            int maxMaxY = 0;
            if (decodeData.getFinderPatternsFound() != null) {
                for (FinderPattern fp : decodeData.getFinderPatternsFound()) {
                    maxY = maxY(decodeData, fp);
                    if (maxY > maxMaxY) {
                        maxMaxY = maxY;
                    }
                }
            }

            if (maxMaxY < Constants.MAX_LUM_LOWER) {
                // send the message that the Exposure should be changed upwards
                MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.CHANGE_EXPOSURE_MESSAGE, 2);
                return;
            }

            if (maxMaxY > Constants.MAX_LUM_UPPER) {
                // send the message that the Exposure should be changed downwards
                MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.CHANGE_EXPOSURE_MESSAGE, -2);
                return;
            }

            // send the message that the Exposure is ok
            MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.EXPOSURE_OK_MESSAGE, 0);
        }
    }

    public void startShadowQualityCheck() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runShadowQualityCheck);
            mDecodeHandler.post(runShadowQualityCheck);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    /*
     * checks exposure of image, by looking at the homogeneity of the white values.
     */
    private void checkShadowQuality() {

        MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.SHADOW_QUALITY_OK_MESSAGE, 0);

//        org.akvo.caddisfly.sensor.striptest.models.DecodeData decodeData = StriptestHandler.getDecodeData();
//        CalibrationCardData calCardData = StriptestHandler.getCalCardData();
//
//        float tlCardX = calCardData.hSize;
//        float tlCardY = 0f;
//        float trCardX = calCardData.hSize;
//        float trCardY = calCardData.vSize;
//        float blCardX = 0f;
//        float blCardY = 0;
//        float brCardX = 0;
//        float brCardY = calCardData.vSize;
//
//        org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPatternInfo info = decodeData.getPatternInfo();
//        if (info == null) {
//            MessageUtils.sendMessage(titrationTestHandler, StriptestHandler.DECODE_FAILED_MESSAGE, 0);
//            return;
//        }
//
//        float tlX = info.getTopLeft().getX();
//        float tlY = info.getTopLeft().getY();
//        float trX = info.getTopRight().getX();
//        float trY = info.getTopRight().getY();
//        float blX = info.getBottomLeft().getX();
//        float blY = info.getBottomLeft().getY();
//        float brX = info.getBottomRight().getX();
//        float brY = info.getBottomRight().getY();
//
//        // create transform from picture coordinates to calibration card coordinates
//        // the calibration card starts with 0,0 in the top left corner, and is measured in mm
//        PerspectiveTransform cardToImageTransform = PerspectiveTransform.quadrilateralToQuadrilateral(
//                tlCardX, tlCardY, trCardX, trCardY, blCardX, blCardY, brCardX, brCardY,
//                tlX, tlY, trX, trY, blX, blY, brX, brY);
//
//        decodeData.setCardToImageTransform(cardToImageTransform);
//
//        // get white point array
//        float[][] points = CalibrationCardUtils.createWhitePointArray(decodeData, calCardData);
//
//        // store in decodeData
//        decodeData.setWhitePointArray(points);
//        if (points.length > 0) {
//            // select those that are not ok, looking at Y only
//            float sumY = 0;
//            float deviation;
//            for (float[] point : points) {
//                sumY += point[2];
//            }
//            // compute average illumination Y value
//            float avgY = sumY / points.length;
//
//            // take reciprocal for efficiency reasons
//            float avgYReciprocal = 1.0f / avgY;
//
//            int numDev = 0;
//            List<float[]> badPoints = new ArrayList<>();
//            for (float[] point : points) {
//                deviation = Math.abs(point[2] - avgY) * avgYReciprocal;
//                // count number of points that differ more than CONTRAST_DEVIATION_FRACTION from the average
//                if (deviation > Constants.CONTRAST_DEVIATION_FRACTION) {
//                    badPoints.add(point);
//                    numDev++;
//
//                    // extra penalty for points that differ more than CONTRAST_MAX_DEVIATION_FRACTION from the average
//                    if (deviation > Constants.CONTRAST_MAX_DEVIATION_FRACTION) {
//                        numDev += 4;
//                    }
//                }
//            }
//
//            // store in decodeData, and send message
//            decodeData.setShadowPoints(badPoints);
//
//            // compute percentage of good points
//            float devPercent = 100f - (100.0f * numDev) / points.length;
//            decodeData.setPercentageShadow(Math.min(Math.max(50f, devPercent), 100f));
//
//            // if the percentage of good point is under the limit (something like 90%), we fail the test
//            if (devPercent < Constants.SHADOW_PERCENTAGE_LIMIT) {
//                MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.SHADOW_QUALITY_FAILED_MESSAGE, 0);
//            } else {
//                MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.SHADOW_QUALITY_OK_MESSAGE, 0);
//            }
//        } else {
//            MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.SHADOW_QUALITY_OK_MESSAGE, 0);
//        }
    }

    public void storeImageData(int currentDelay) {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runStoreData);
            mCurrentDelay = currentDelay;
            mDecodeHandler.post(runStoreData);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    private void storeData() {
        // subtract black part and put it in a rectangle. Do the calibration at the same time.
        // 1) determine size of new image array
        // 2) loop over pixels of new image array
        // 3) compute location of pixel using card-to-image transform
        // 4) apply illumination correction
        // 5) apply calibration
        // 6) store new array

        DecodeData decodeData = TitrationTestHandler.getDecodeData();
        MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.IMAGE_SAVED_MESSAGE, 0);
    }
}