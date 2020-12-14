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

package org.akvo.caddisfly.sensor.colorimetry.strip.calibration;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.CalibrationException;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationData;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationResultData;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.util.detector.BitMatrix;
import org.akvo.caddisfly.util.detector.Detector;
import org.akvo.caddisfly.util.detector.FinderPattern;
import org.akvo.caddisfly.util.detector.MathUtils;
import org.akvo.caddisfly.util.detector.ResultPoint;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Performs the calibration of the image
public final class CalibrationCard {

    private static final String TAG = "CalibrationCard";

    private static final int CODE_NOT_FOUND = 0;
    private static final double ONE_OVER_NINE = 1.0 / 9;
    private static final SparseIntArray versionNumberMap = new SparseIntArray();
    // array to cache the loaded card calibration versions
    private static final SparseArray<CalibrationData> calibrationsMeta = new SparseArray<>();
    private static boolean errorFound;

    private CalibrationCard() {
    }

    public static void initialize() {
        versionNumberMap.clear();
    }

    //put version number in array: number, frequency
    public static void addVersionNumber(Integer number) {
        int existingFrequency = versionNumberMap.get(number);
        versionNumberMap.put(number, existingFrequency + 1);
    }

    public static int getMostFrequentVersionNumber() {
        int mostFrequent = 0;
        int value = -1;

        //what is the most frequent value
        for (int i = 0; i < versionNumberMap.size(); i++) {
            int key = versionNumberMap.keyAt(i);
            int frequency = versionNumberMap.get(key);
            if (frequency > mostFrequent) {
                mostFrequent = frequency;
                value = key;
            }
        }

        Log.d(TAG, String.format("%s : %s", versionNumberMap.size(), mostFrequent));

        return value;
    }

    public static boolean isCardVersionEstablished() {
        int mostFrequent = 0;
        int prevMostFrequent = 0;

        for (int i = 0; i < versionNumberMap.size(); i++) {
            int key = versionNumberMap.keyAt(i);
            int frequency = versionNumberMap.get(key);
            if (frequency > mostFrequent) {
                prevMostFrequent = mostFrequent;
                mostFrequent = frequency;
            }
        }

        return mostFrequent - prevMostFrequent > 5;
    }

    public static CalibrationData readCalibrationFile() throws CalibrationException {

        int version = getMostFrequentVersionNumber();

        CalibrationData calibrationData = calibrationsMeta.get(version);

        // if calibration data has already been loaded then return the cached data else load and cache
        if (calibrationData == null) {

            String calFileName = "calibration" + version + ".json";
            String json = AssetsManager.getInstance().loadJSONFromAsset(calFileName);

            if (json != null) {
                try {
                    CalibrationData calData = new CalibrationData();

                    JSONObject obj = new JSONObject(json);

                    // general data
//                calData.date = obj.getString("date");
//                calData.cardVersion = obj.getString("cardVersion");
//                calData.unit = obj.getString("unit");

                    // sizes
                    JSONObject calDataJSON = obj.getJSONObject("calData");
                    calData.setPatchSize(calDataJSON.getDouble("patchSize"));
                    calData.hSize = calDataJSON.getDouble("hSize");
                    calData.vSize = calDataJSON.getDouble("vSize");

                    // locations
                    JSONArray locJSON = calDataJSON.getJSONArray("locations");
                    for (int i = 0; i < locJSON.length(); i++) {
                        JSONObject loc = locJSON.getJSONObject(i);
                        calData.addLocation(loc.getString("l"), loc.getDouble("x"), loc.getDouble("y"), loc.getBoolean("gray"));
                    }

                    // colors
                    JSONArray colJSON = calDataJSON.getJSONArray("calValues");
                    for (int i = 0; i < colJSON.length(); i++) {
                        JSONObject cal = colJSON.getJSONObject(i);
                        // we scale the Lab values in the same way as openCV does
                        calData.addCal(cal.getString("l"), cal.getDouble("CIE_L") * 2.55, cal.getDouble("CIE_A") + 128, cal.getDouble("CIE_B") + 128);
                    }

                    // white lines
                    JSONArray linesJSON = obj.getJSONObject("whiteData").getJSONArray("lines");
                    for (int i = 0; i < linesJSON.length(); i++) {
                        JSONObject line = linesJSON.getJSONObject(i);
                        JSONArray p = line.getJSONArray("p");
                        calData.addWhiteLine(p.getDouble(0), p.getDouble(1), p.getDouble(2), p.getDouble(3), line.getDouble("width"));
                    }

                    // strip area
                    JSONArray stripArea = obj.getJSONObject("stripAreaData").getJSONArray("area");
                    calData.setStripArea(stripArea.getDouble(0), stripArea.getDouble(1), stripArea.getDouble(2), stripArea.getDouble(3));

                    errorFound = false;

                    calibrationsMeta.put(version, calData);

                    return calData;

                } catch (JSONException e) {
                    Log.e(TAG, "Error reading calibration file", e);
                }
            } else {
                // Wait for a few version readings before declaring error
                if (versionNumberMap.get(version) > 3) {
                    initialize();
                    errorFound = true;
                    throw new CalibrationException("Unknown version of color card");
                }
            }
            return null;
        } else {
            return calibrationData;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static int capValue(int val, int min, int max) {
        if (val > max) {
            return max;
        }
        return val < min ? min : val;
    }

    // computes the color around a single point
    // x and y in pixels
    // This method expects a CIELab file
    @NonNull
    private static double[] getWhiteVal(@NonNull Mat lab, int x, int y, int dp) {
        double totLum = 0;
        double totA = 0;
        double totB = 0;

        int totNum = 0;
        for (int i = -dp; i <= dp; i++) {
            for (int ii = -dp; ii <= dp; ii++) {
                totLum = totLum + lab.get(y + i, x + ii)[0];
                totA = totA + lab.get(y + i, x + ii)[1];
                totB = totB + lab.get(y + i, x + ii)[2];
                totNum++;
            }
        }
        return new double[]{totLum / totNum, totA / totNum, totB / totNum};
    }

    /*
    * Samples the white area of a calibration card. This information is used to straighten out the illumination.
    * @result: array of point vectors, with the structure [x,y,L,A,B]
     */
    @NonNull
    public static double[][] createWhitePointArray(@NonNull Mat lab, @NonNull CalibrationData calData) {
        List<CalibrationData.WhiteLine> lines = calData.getWhiteLines();
        int numLines = lines.size() * 10; // on each line, we sample 10 points
        double[][] points = new double[numLines][5];
        int index = 0;

        calData.hSizePixel = lab.cols();
        double hPixels = calData.hSizePixel / calData.hSize; // pixel per mm in the horizontal direction
        calData.vSizePixel = lab.rows();
        double vPixels = calData.vSizePixel / calData.vSize; // pixel per mm in the vertical direction

        for (CalibrationData.WhiteLine line : lines) {
            double xStart = line.getPosition()[0];
            double yStart = line.getPosition()[1];
            double xEnd = line.getPosition()[2];
            double yEnd = line.getPosition()[3];
            double xDiff = (xEnd - xStart) * ONE_OVER_NINE;
            double yDiff = (yEnd - yStart) * ONE_OVER_NINE;
            int dp = (int) Math.round(line.getWidth() * hPixels * 0.5);
            if (dp == 0) {
                dp = 1; // minimum of one pixel
            }

            // sample line
            for (int i = 0; i <= 9; i++) {
                int xp = (int) Math.round((xStart + i * xDiff) * hPixels);
                int yp = (int) Math.round((yStart + i * yDiff) * vPixels);

                points[index * 10 + i][0] = xp;
                points[index * 10 + i][1] = yp;
                double[] whiteVal = getWhiteVal(lab, xp, yp, dp);
                points[index * 10 + i][2] = whiteVal[0];
                points[index * 10 + i][3] = whiteVal[1];
                points[index * 10 + i][4] = whiteVal[2];
            }
            index++;
        }
        return points;
    }

    /*
    * Turns the white point array into a matrix
     */
    private static RealMatrix createWhitePointMatrix(@NonNull Mat lab, @NonNull CalibrationData calData) {
        double[][] points = createWhitePointArray(lab, calData);
        return MatrixUtils.createRealMatrix(points);
    }

    /*
    * Straightens the illumination of the calibration file. It does this by sampling the white areas
    * and compute a quadratic profile. The image is then corrected using this profile.
     */
    @NonNull
    private static Mat doIlluminationCorrection(@NonNull Mat imgLab, @NonNull CalibrationData calData) {
        // create HLS image for homogeneous illumination calibration
        int pHeight = imgLab.rows();
        int pWidth = imgLab.cols();


        RealMatrix points = createWhitePointMatrix(imgLab, calData);

        // create coefficient matrix for all three variables L,A,B
        // the model for all three is y = ax + bx^2 + cy + dy^2 + exy + f
        // 6th row is the constant 1
        RealMatrix coefficient = new Array2DRowRealMatrix(points.getRowDimension(), 6);
        coefficient.setColumnMatrix(0, points.getColumnMatrix(0));
        coefficient.setColumnMatrix(2, points.getColumnMatrix(1));

        //create constant, x^2, y^2 and xy terms
        for (int i = 0; i < points.getRowDimension(); i++) {
            coefficient.setEntry(i, 1, Math.pow(coefficient.getEntry(i, 0), 2)); // x^2
            coefficient.setEntry(i, 3, Math.pow(coefficient.getEntry(i, 2), 2)); // y^2
            coefficient.setEntry(i, 4, coefficient.getEntry(i, 0) * coefficient.getEntry(i, 2)); // xy
            coefficient.setEntry(i, 5, 1d); // constant = 1
        }

        // create vectors
        RealVector L = points.getColumnVector(2);
        RealVector A = points.getColumnVector(3);
        RealVector B = points.getColumnVector(4);

        // solve the least squares problem for all three variables
        DecompositionSolver solver = new SingularValueDecomposition(coefficient).getSolver();
        RealVector solutionL = solver.solve(L);
        RealVector solutionA = solver.solve(A);
        RealVector solutionB = solver.solve(B);

        // get individual coefficients
        float La = (float) solutionL.getEntry(0);
        float Lb = (float) solutionL.getEntry(1);
        float Lc = (float) solutionL.getEntry(2);
        float Ld = (float) solutionL.getEntry(3);
        float Le = (float) solutionL.getEntry(4);
        float Lf = (float) solutionL.getEntry(5);

        float Aa = (float) solutionA.getEntry(0);
        float Ab = (float) solutionA.getEntry(1);
        float Ac = (float) solutionA.getEntry(2);
        float Ad = (float) solutionA.getEntry(3);
        float Ae = (float) solutionA.getEntry(4);
        float Af = (float) solutionA.getEntry(5);

        float Ba = (float) solutionB.getEntry(0);
        float Bb = (float) solutionB.getEntry(1);
        float Bc = (float) solutionB.getEntry(2);
        float Bd = (float) solutionB.getEntry(3);
        float Be = (float) solutionB.getEntry(4);
        float Bf = (float) solutionB.getEntry(5);

        // compute mean (the luminosity value of the plane in the middle of the image)
        float L_mean = (float) (0.5 * La * pWidth + 0.5 * Lc * pHeight + Lb * pWidth * pWidth / 3.0
                + Ld * pHeight * pHeight / 3.0 + Le * 0.25 * pHeight * pWidth + Lf);
        float A_mean = (float) (0.5 * Aa * pWidth + 0.5 * Ac * pHeight + Ab * pWidth * pWidth / 3.0
                + Ad * pHeight * pHeight / 3.0 + Ae * 0.25 * pHeight * pWidth + Af);
        float B_mean = (float) (0.5 * Ba * pWidth + 0.5 * Bc * pHeight + Bb * pWidth * pWidth / 3.0
                + Bd * pHeight * pHeight / 3.0 + Be * 0.25 * pHeight * pWidth + Bf);

        // Correct image
        // we do this per row. We tried to do it in one block, but there is no speed difference.
        byte[] temp = new byte[imgLab.cols() * imgLab.channels()];
        int valL, valA, valB;
        int ii, ii3;
        float iiSq, iSq;
        int imgCols = imgLab.cols();
        int imgRows = imgLab.rows();

        // use lookup tables to speed up computation
        // create lookup tables
        float[] L_aii = new float[imgCols];
        float[] L_biiSq = new float[imgCols];
        float[] A_aii = new float[imgCols];
        float[] A_biiSq = new float[imgCols];
        float[] B_aii = new float[imgCols];
        float[] B_biiSq = new float[imgCols];

        float[] Lci = new float[imgRows];
        float[] LdiSq = new float[imgRows];
        float[] Aci = new float[imgRows];
        float[] AdiSq = new float[imgRows];
        float[] Bci = new float[imgRows];
        float[] BdiSq = new float[imgRows];

        for (ii = 0; ii < imgCols; ii++) {
            iiSq = ii * ii;
            L_aii[ii] = La * ii;
            L_biiSq[ii] = Lb * iiSq;
            A_aii[ii] = Aa * ii;
            A_biiSq[ii] = Ab * iiSq;
            B_aii[ii] = Ba * ii;
            B_biiSq[ii] = Bb * iiSq;
        }

        for (int i = 0; i < imgRows; i++) {
            iSq = i * i;
            Lci[i] = Lc * i;
            LdiSq[i] = Ld * iSq;
            Aci[i] = Ac * i;
            AdiSq[i] = Ad * iSq;
            Bci[i] = Bc * i;
            BdiSq[i] = Bd * iSq;
        }

        // We can also improve the performance of the i,ii term, if we want, but it won't make much difference.
        for (int i = 0; i < imgRows; i++) { // y
            imgLab.get(i, 0, temp);
            ii3 = 0;
            for (ii = 0; ii < imgCols; ii++) {  //x
                valL = capValue(Math.round((temp[ii3] & 0xFF) - (L_aii[ii] + L_biiSq[ii] + Lci[i] + LdiSq[i] + Le * i * ii + Lf) + L_mean), 0, 255);
                valA = capValue(Math.round((temp[ii3 + 1] & 0xFF) - (A_aii[ii] + A_biiSq[ii] + Aci[i] + AdiSq[i] + Ae * i * ii + Af) + A_mean), 0, 255);
                valB = capValue(Math.round((temp[ii3 + 2] & 0xFF) - (B_aii[ii] + B_biiSq[ii] + Bci[i] + BdiSq[i] + Be * i * ii + Bf) + B_mean), 0, 255);

                temp[ii3] = (byte) valL;
                temp[ii3 + 1] = (byte) valA;
                temp[ii3 + 2] = (byte) valB;
                ii3 += 3;
            }
            imgLab.put(i, 0, temp);
        }

        return imgLab;
    }

    @NonNull
    private static float[] measurePatch(@NonNull Mat imgMat, double x, double y, @NonNull CalibrationData calData) {
        float[] LAB_result = new float[3];
        float totL = 0;
        float totA = 0;
        float totB = 0;
        int totNum = 0;

        calData.hSizePixel = imgMat.cols();
        double hPixels = calData.hSizePixel / calData.hSize; // pixel per mm
        calData.vSizePixel = imgMat.rows();
        double vPixels = calData.vSizePixel / calData.vSize; // pixel per mm

        int xp = (int) Math.round(x * hPixels);
        int yp = (int) Math.round(y * vPixels);
        int dp = (int) Math.round(calData.getPatchSize() * hPixels * 0.25);
        byte[] temp = new byte[(2 * dp + 1) * imgMat.channels()];
        int ii3;
        for (int i = -dp; i <= dp; i++) {
            if (yp - i >= 0) {
                imgMat.get(yp - i, xp - dp, temp);
                ii3 = 0;
                for (int ii = 0; ii <= 2 * dp; ii++) {
                    totL += temp[ii3] & 0xFF; //imgMat.get(yp + i, xp + ii)[0];
                    totA += temp[ii3 + 1] & 0xFF; //imgMat.get(yp + i, xp + ii)[1];
                    totB += temp[ii3 + 2] & 0xFF; //imgMat.get(yp + i, xp + ii)[2];
                    totNum++;
                    ii3 += 3;
                }
            }
        }
        LAB_result[0] = totL / totNum;
        LAB_result[1] = totA / totNum;
        LAB_result[2] = totB / totNum;
        return LAB_result;
    }

    /*
    * Perform two calibration steps:
    * 1) a 1D calibration which looks at the individual L, A, B channels and corrects them
    * 2) a 3d calibration which can mix the L,A,B channels to arrive at optimal results
     */
    @NonNull
    private static Mat do1D_3DCorrection(@NonNull Mat imgMat, @Nullable CalibrationData calData) throws CalibrationException {

        if (calData == null) {
            throw new CalibrationException("no calibration data.");
        }

        final WeightedObservedPoints obsL = new WeightedObservedPoints();
        final WeightedObservedPoints obsA = new WeightedObservedPoints();
        final WeightedObservedPoints obsB = new WeightedObservedPoints();

        Map<String, double[]> calResultIllumination = new HashMap<>();
        // iterate over all patches
        try {
            for (String label : calData.getCalValues().keySet()) {
                CalibrationData.CalValue cal = calData.getCalValues().get(label);
                CalibrationData.Location loc = calData.getLocations().get(label);
                float[] LAB_color = measurePatch(imgMat, loc.x, loc.y, calData); // measure patch color
                obsL.add(LAB_color[0], cal.getL());
                obsA.add(LAB_color[1], cal.getA());
                obsB.add(LAB_color[2], cal.getB());
                calResultIllumination.put(label, new double[]{LAB_color[0], LAB_color[1], LAB_color[2]});
            }
        } catch (Exception e) {
            throw new CalibrationException("1D calibration: error iterating over all patches.", e);
        }

        // Instantiate a second-degree polynomial fitter.
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);

        // Retrieve fitted parameters (coefficients of the polynomial function).
        // order of coefficients is (c + bx + ax^2), so [c,b,a]
        try {
            final double[] coefficientL = fitter.fit(obsL.toList());
            final double[] coefficientA = fitter.fit(obsA.toList());
            final double[] coefficientB = fitter.fit(obsB.toList());

            double[] valIllumination;
            double L_orig, A_orig, B_orig, L_new, A_new, B_new;

            // transform patch values using the 1d calibration results
            Map<String, double[]> calResult1D = new HashMap<>();
            for (String label : calData.getCalValues().keySet()) {
                valIllumination = calResultIllumination.get(label);

                L_orig = valIllumination[0];
                A_orig = valIllumination[1];
                B_orig = valIllumination[2];

                L_new = coefficientL[2] * L_orig * L_orig + coefficientL[1] * L_orig + coefficientL[0];
                A_new = coefficientA[2] * A_orig * A_orig + coefficientA[1] * A_orig + coefficientA[0];
                B_new = coefficientB[2] * B_orig * B_orig + coefficientB[1] * B_orig + coefficientB[0];

                calResult1D.put(label, new double[]{L_new, A_new, B_new});
            }

            // use the 1D calibration result for the second calibration step
            // Following http://docs.scipy.org/doc/scipy/reference/tutorial/linalg.html#solving-linear-least-squares-problems-and-pseudo-inverses
            // we will solve P = M x
            int total = calData.getLocations().keySet().size();
            RealMatrix coefficient = new Array2DRowRealMatrix(total, 3);
            RealMatrix cal = new Array2DRowRealMatrix(total, 3);
            int index = 0;

            // create coefficient and calibration vectors
            for (String label : calData.getCalValues().keySet()) {
                CalibrationData.CalValue calv = calData.getCalValues().get(label);
                double[] cal1dResult = calResult1D.get(label);
                coefficient.setEntry(index, 0, cal1dResult[0]);
                coefficient.setEntry(index, 1, cal1dResult[1]);
                coefficient.setEntry(index, 2, cal1dResult[2]);

                cal.setEntry(index, 0, calv.getL());
                cal.setEntry(index, 1, calv.getA());
                cal.setEntry(index, 2, calv.getB());
                index++;
            }

            DecompositionSolver solver = new SingularValueDecomposition(coefficient).getSolver();
            RealMatrix sol = solver.solve(cal);

            float a_L, b_L, c_L, a_A, b_A, c_A, a_B, b_B, c_B;
            a_L = (float) sol.getEntry(0, 0);
            b_L = (float) sol.getEntry(1, 0);
            c_L = (float) sol.getEntry(2, 0);
            a_A = (float) sol.getEntry(0, 1);
            b_A = (float) sol.getEntry(1, 1);
            c_A = (float) sol.getEntry(2, 1);
            a_B = (float) sol.getEntry(0, 2);
            b_B = (float) sol.getEntry(1, 2);
            c_B = (float) sol.getEntry(2, 2);

            //use the solution to correct the image
            double L_temp, A_temp, B_temp, L_mid, A_mid, B_mid;
            int L_fin, A_fin, B_fin;
            int ii3;
            byte[] temp = new byte[imgMat.cols() * imgMat.channels()];
            for (int i = 0; i < imgMat.rows(); i++) { // y
                imgMat.get(i, 0, temp);
                ii3 = 0;
                for (int ii = 0; ii < imgMat.cols(); ii++) {  //x
                    L_temp = temp[ii3] & 0xFF;
                    A_temp = temp[ii3 + 1] & 0xFF;
                    B_temp = temp[ii3 + 2] & 0xFF;

                    L_mid = coefficientL[2] * L_temp * L_temp + coefficientL[1] * L_temp + coefficientL[0];
                    A_mid = coefficientA[2] * A_temp * A_temp + coefficientA[1] * A_temp + coefficientA[0];
                    B_mid = coefficientB[2] * B_temp * B_temp + coefficientB[1] * B_temp + coefficientB[0];

                    L_fin = (int) Math.round(a_L * L_mid + b_L * A_mid + c_L * B_mid);
                    A_fin = (int) Math.round(a_A * L_mid + b_A * A_mid + c_A * B_mid);
                    B_fin = (int) Math.round(a_B * L_mid + b_B * A_mid + c_B * B_mid);

                    // cap values
                    L_fin = capValue(L_fin, 0, 255);
                    A_fin = capValue(A_fin, 0, 255);
                    B_fin = capValue(B_fin, 0, 255);

                    temp[ii3] = (byte) L_fin;
                    temp[ii3 + 1] = (byte) A_fin;
                    temp[ii3 + 2] = (byte) B_fin;

                    ii3 += 3;
                }
                imgMat.put(i, 0, temp);
            }

            return imgMat;
        } catch (Exception e) {
            throw new CalibrationException("error while performing calibration: ", e);
        }
    }

    private static void addPatch(@NonNull Mat imgMat, Double x, Double y, @NonNull CalibrationData calData, String label) {

        CalibrationData.CalValue calValue = calData.getCalValues().get(label);
        calData.hSizePixel = imgMat.cols();
        double hPixels = calData.hSizePixel / calData.hSize; // pixel per mm
        calData.vSizePixel = imgMat.rows();
        double vPixels = calData.vSizePixel / calData.vSize; // pixel per mm

        int xp = (int) Math.round(x * hPixels);
        int yp = (int) Math.round(y * vPixels);
        int dp = (int) Math.round(calData.getPatchSize() * hPixels * 0.150);
        for (int i = -dp; i <= dp; i++) {
            for (int ii = -dp; ii <= dp; ii++) {
                byte[] col = new byte[3];
                col[0] = (byte) Math.round(calValue.getL());
                col[1] = (byte) Math.round(calValue.getA());
                col[2] = (byte) Math.round(calValue.getB());
                if (yp + i >= 0) {
                    imgMat.put(yp + i, xp + ii, col);
                }
            }
        }
    }

    private static void addCalColors(@NonNull Mat imgMat, @NonNull CalibrationData calData) {
        for (String label : calData.getLocations().keySet()) {
            CalibrationData.Location loc = calData.getLocations().get(label);
            addPatch(imgMat, loc.x, loc.y, calData, label);
        }
    }

    /*
    * Calibrate an image using a 3-step process. The steps are:
     * 1) straighten illumination profile
     * 2) 1D color balance
     * 3) 3D color balance
     *
     * As a final step, we put the calibration colors in the image, so that the quality can be judged later
     *
     * imgMat: CV_8UC3 (8-bit) Mat object, in BGR encoding.
     * @result: calibrated image
     */
    @Nullable
    public static CalibrationResultData calibrateImage(@Nullable Mat labImg,
                                                       @Nullable CalibrationData calData) throws CalibrationException {

        if (calData != null) {
            // illumination correction
            if (labImg != null) {
                labImg = doIlluminationCorrection(labImg, calData);
            }

            // 1D and 3D color balance
            if (labImg != null) {
                labImg = do1D_3DCorrection(labImg, calData);
            }

            if (labImg != null) {
                // measure quality of the calibration
                computeE94Error(labImg, calData);

                // insert calibration colors in image
                addCalColors(labImg, calData);
            }

            return new CalibrationResultData(labImg);
        }
        return null;
    }

    /*
    * Computes E94 distance between two colors.
    * First normalises the colors as follows:
    * l : 0...100
    * a,b: -128 ... 128
    * follows http://colormine.org/delta-e-calculator/cie94
    *
    * @returns: E94 distance
     */
    public static double E94(double l1, double a1, double b1, double l2, double a2, double b2, boolean normalise) {

        if (normalise) {
            // normalise values to standard ranges
            l1 = l1 / 2.55;
            l2 = l2 / 2.55;
            a1 = a1 - 128;
            a2 = a2 - 128;
            b1 = b1 - 128;
            b2 = b2 - 128;
        }

        final double dL = l1 - l2;
        final double da = a1 - a2;
        final double db = b1 - b2;

        final double c1 = Math.sqrt(a1 * a1 + b1 * b1);
        final double c2 = Math.sqrt(a2 * a2 + b2 * b2);
        final double dC = c1 - c2;

        double dH = da * da + db * db - dC * dC;
        dH = dH < 0 ? 0 : Math.sqrt(dH);

        final double sl = 1.0;
        final double sc = 1.0 + 0.045 * c1;
        final double sh = 1.0 + 0.015 * c1;

        return Math.sqrt(Math.pow(dL / sl, 2) + Math.pow(dC / sc, 2) + Math.pow(dH / sh, 2));
    }

    /*
    * Computes mean and max E94 distance of calibrated image and the calibration patches
    * @returns: vector of double, with [mean E94, max E94]
     */
    private static void computeE94Error(@NonNull Mat labImg, @NonNull CalibrationData calData) {
        int num = 0;
        double totE94 = 0;
        double maxE94 = 0;
        StringBuilder calibrationColors = new StringBuilder();

        List<String> sortedKeys = new ArrayList<>(calData.getCalValues().keySet());
        if (AppPreferences.isDiagnosticMode()) {
            Collections.sort(sortedKeys);
        }

        for (String label : sortedKeys) {
            CalibrationData.CalValue cal = calData.getCalValues().get(label);
            CalibrationData.Location loc = calData.getLocations().get(label);
            float[] LAB_color = measurePatch(labImg, loc.x, loc.y, calData); // measure patch color

            if (AppPreferences.isDiagnosticMode()) {
                calibrationColors.append(String.format(Locale.US,
                        "{\"l\":\"%s\",\"CAL_L\":\"%.2f\",\"CAL_A\":\"%.2f\",\"CAL_B\":\"%.2f\""
                                + ",\"CIE_L\":\"%.2f\",\"CIE_A\":\"%.2f\",\"CIE_B\":\"%.2f\"},%n",
                        label, cal.getL() / 2.55, cal.getA() - 128, cal.getB() - 128,
                        LAB_color[0] / 2.55, LAB_color[1] - 128, LAB_color[2] - 128));
            }

            // as both measured and calibration values are in openCV range, we need to normalise the values
            double E94Dist = E94(LAB_color[0], LAB_color[1], LAB_color[2], cal.getL(), cal.getA(), cal.getB(), true);
            totE94 += E94Dist;
            if (E94Dist > maxE94) {
                maxE94 = E94Dist;
            }
            num++;
        }

        if (AppPreferences.isDiagnosticMode()) {
            calibrationColors.deleteCharAt(calibrationColors.length() - 2);

            String distanceInfo = String.format(Locale.getDefault(),
                    "AD: %.2f, MD: %.2f, TD: %.2f", totE94 / num, maxE94, totE94);

            String diagnosticInfo = "{\"calData\": {"
                    + "{Result}"
                    + "\"info\" : \"" + distanceInfo + "\","
                    + "\"calValues\": [" + calibrationColors.toString() + "]}}";

            PreferencesUtil.setString(CaddisflyApp.getApp().getApplicationContext(),
                    Constant.DIAGNOSTIC_INFO, diagnosticInfo);

            PreferencesUtil.setString(CaddisflyApp.getApp().getApplicationContext(),
                    Constant.CALIBRATION_INFO, distanceInfo);
        }

//        return new double[]{totE94 / num, maxE94, totE94};

    }

    /**
     * find and decode the code of the calibration card
     * The code is stored as a simple barcode. It starts 4.5 modules from the center of the bottom left finder pattern
     * and extends to module 29.5.
     * It has 12 bits, of 2 modules wide each.
     * It starts and ends with a 1 bit.
     * The remaining 10 bits are interpreted as a 9 bit number with the last bit as parity bit.
     * Position barcode:
     * _________________________________________________________
     * |                                                        |
     * |________________                                        |
     * ||0             1|                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||              b|                                       |
     * ||              b|                                       |
     * ||2_____________3|                                       |
     * |________________________________________________________|
     */
    public static int decodeCalibrationCardCode(@NonNull List<FinderPattern> patternInfo, @NonNull BitMatrix image) {
        // patterns are ordered top left, top right, bottom left, bottom right (in portrait mode, with black area to the right)
        if (patternInfo.size() == 4) {
            ResultPoint bottomLeft = new ResultPoint(patternInfo.get(3).getX(), patternInfo.get(3).getY());
            ResultPoint bottomRight = new ResultPoint(patternInfo.get(1).getX(), patternInfo.get(1).getY());

            // get estimated module size
            Detector detector = new Detector(image);
            float modSize = detector.calculateModuleSize(bottomLeft, bottomRight, bottomRight);

            // go from one finder pattern to the other,
            //because camera is in portrait mode, we need to shift x and y
            double lrx = bottomRight.getX() - bottomLeft.getX();
            double lry = bottomRight.getY() - bottomLeft.getY();
            double hNorm = MathUtils.distance(bottomLeft.getX(), bottomLeft.getY(),
                    bottomRight.getX(), bottomRight.getY());

            // check if left and right are ok
            if (lry > 0) {
                return CODE_NOT_FOUND;
            }

            // create vector of length 1 pixel, in the direction of the bottomRight finder pattern
            lrx /= hNorm;
            lry /= hNorm;

            // sample line into new row
            boolean[] bits = new boolean[image.getHeight()];
            int index = 0;
            double px = bottomLeft.getX();
            double py = bottomLeft.getY();
            try {
                while (px > 0 && py > 0 && px < image.getWidth() && py < image.getHeight()) {
                    bits[index] = image.get((int) Math.round(px), (int) Math.round(py));
                    px += lrx;
                    py += lry;
                    index++;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sample line into new row");
                return CODE_NOT_FOUND;
            }

            // starting index: 4.5 modules in the direction of the bottom right finder pattern
            // end index: our pattern ends at module 17, so we take 25 to be sure.
            int startIndex = (int) Math.abs(Math.round(4.5 * modSize / lry));
            int endIndex = (int) Math.abs(Math.round(25 * modSize / lry));

            // determine qualityChecksOK of pattern: first black bit. Approach from the left
            try {
                int startI = startIndex;
                while (startI < endIndex && !bits[startI]) {
                    startI++;
                }

                // determine end of pattern: last black bit. Approach from the right
                int endI = endIndex;
                while (endI > startI && !bits[endI]) {
                    endI--;
                }

                int lengthPattern = endI - startI + 1;

                // sanity check on length of pattern.
                // We put the minimum size at 20 pixels, which would correspond to a module size of less than 2 pixels,
                // which is too small.
                if (lengthPattern < 20) {
                    Log.e(TAG, "Length of pattern too small");
                    return CODE_NOT_FOUND;
                }

                double pWidth = lengthPattern / 12.0;

                // determine bits by majority voting
                int[] bitVote = new int[12];
                for (int i = 0; i < 12; i++) {
                    bitVote[i] = 0;
                }

                int bucket;
                for (int i = startI; i <= endI; i++) {
                    bucket = (int) Math.round(Math.floor((i - startI) / pWidth));
                    bitVote[bucket] += bits[i] ? 1 : -1;
                }

                // translate into information bits. Skip first and last, which are always 1
                boolean[] bitResult = new boolean[10]; // will contain the information bits
                for (int i = 1; i < 11; i++) {
                    bitResult[i - 1] = bitVote[i] > 0;
                }

                // check parity bit
                if (parity(bitResult) != bitResult[9]) {
                    return CODE_NOT_FOUND;
                }

                // compute result
                int code = 0;
                int count = 0;
                for (int i = 8; i >= 0; i--) {
                    if (bitResult[i]) {
                        code += (int) Math.pow(2, count);
                    }
                    count++;
                }

                return code;
            } catch (Exception e) {
                return CODE_NOT_FOUND;
            }
        } else {
            return CODE_NOT_FOUND;
        }
    }

    /**
     * Compute even parity, where last bit is the even parity bit
     */
    private static boolean parity(@NonNull boolean[] bits) {
        int oneCount = 0;
        for (int i = 0; i < bits.length - 1; i++) {  // skip parity bit in calculation of parity
            if (bits[i]) {
                oneCount++;
            }
        }
        return oneCount % 2 != 0; // returns true if parity is odd
    }

    public static boolean hasError() {
        return errorFound;
    }
}
