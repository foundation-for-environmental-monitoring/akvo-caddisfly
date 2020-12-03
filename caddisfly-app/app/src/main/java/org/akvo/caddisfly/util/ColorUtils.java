package org.akvo.caddisfly.util;

public class ColorUtils {

    private final static float X_REF = 95.047f;
    private final static float Y_REF = 100f;
    private final static float Z_REF = 108.883f;
    private final static float eps = 0.008856f; //kE
    private final static float kappa = 903.3f; // kK
    private final static float kappaEps = 8.0f; // kKE

    // XYZ D65 to gamma-corrected sRGB D65
    // according to http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // and http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // using sRGB gamma companding
    // we assume XYZ is scaled [0..100]
    // RGB is scaled to [0..255] and clamped
    public static int[] xyzToRgbInt(float[] XYZ) {
        float[] xyzScaled = new float[3];
        float[] RGB = new float[3];

        // first we scale to [0..1]
        for (int i = 0; i < 3; i++) {
            xyzScaled[i] = XYZ[i] / 100.0f;
        }

        // next, we apply a matrix:
        RGB[0] = 3.2404542f * xyzScaled[0] - 1.5371385f * xyzScaled[1] - 0.4985314f * xyzScaled[2];
        RGB[1] = -0.9692660f * xyzScaled[0] + 1.8760108f * xyzScaled[1] + 0.0415560f * xyzScaled[2];
        RGB[2] = 0.0556434f * xyzScaled[0] - 0.2040259f * xyzScaled[1] + 1.0572252f * xyzScaled[2];

        // next, we apply gamma encoding
        for (int i = 0; i < 3; i++) {
            if (RGB[i] < 0.0031308) {
                RGB[i] = 12.92f * RGB[i];
            } else {
                RGB[i] = (float) (1.055f * Math.pow(RGB[i], 1 / 2.4)) - 0.055f;
            }
        }

        // next, we scale to [0..255] and clamp
        int[] rgbInt = new int[3];
        for (int i = 0; i < 3; i++) {
            RGB[i] *= 255.0f;
            rgbInt[i] = Math.min(Math.max(0, Math.round(RGB[i])), 255);
        }
        return rgbInt;
    }

    // Lab D65 to XYZ D65 scaled [0..100]
    // http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html
    public static float[] Lab2XYZ(float[] lab) {
        float CIEL = lab[0];
        float CIEa = lab[1];
        float CIEb = lab[2];

        float fy = (CIEL + 16.0f) / 116.0f;
        float fx = 0.002f * CIEa + fy;
        float fz = fy - 0.005f * CIEb;

        float fx3 = fx * fx * fx;
        float fz3 = fz * fz * fz;

        float xr = (fx3 > eps) ? fx3 : ((116.0f * fx - 16.0f) / kappa);
        float yr = (float) ((CIEL > kappaEps) ? Math.pow((CIEL + 16.0f) / 116.0f, 3.0f) : (CIEL / kappa));
        float zr = (fz3 > eps) ? fz3 : ((116.0f * fz - 16.0f) / kappa);

        return new float[]{xr * X_REF, yr * Y_REF, zr * Z_REF};
    }
}
