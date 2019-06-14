package org.akvo.caddisfly.common;

import org.akvo.caddisfly.model.TestSampleType;

public final class TestConstants {

    public static final int STRIP_TESTS_COUNT = 1;
    public static final int CUVETTE_TESTS_COUNT = 6;

    // Soil - Available Iron
    public static final String CUVETTE_TEST_ID_1 = "3353f5cf-1cd2-4bf5-b47f-15d3db917add";
    public static final String CUVETTE_TEST_NAME_1 = "Available Iron";
//    public static final int TEST_INDEX = 1;
//    public static final int CUVETTE_TEST_TIME_DELAY = 180;

    // Soil - pH
    public static final TestSampleType IS_TEST_TYPE = TestSampleType.SOIL;
    public static final String IS_TEST_GROUP = "Soil Tests 2";
    public static final String IS_TEST_HIGH_CALIBRATION = "HighLevelTest";
    public static final int IS_TEST_CALIBRATION_INDEX = 4;
    public static final String IS_TEST_NAME = "Available Iron";
    //    public static final String IS_TEST_ID = TestConstant.SOIL_IRON_ID;
    public static final int IS_START_DELAY = 0;
    public static final int IS_TEST_INDEX = 1;
    public static final int IS_TIME_DELAY = 180;
    public static final double IS_EXPECTED_RESULT = 3.9;
    public static final boolean IS_HAS_DILUTION = true;

    //    public static final String IS_TEST_GROUP = "Soil Tests 1";
    public static final String IS_TEST_VALID_CALIBRATION = "pH_Valid";
    public static final String CUVETTE_TEST_UNIT = "mg\\/l";
//    public static final int IS_TEST_CALIBRATION_INDEX = 0;
//    public static final String IS_TEST_NAME = "pH";
//    public static final String IS_TEST_ID = TestConstant.SOIL_PH_ID;
//    public static final int IS_START_DELAY = 0;
//    public static final int IS_TEST_INDEX = 5;
//    public static final int IS_TIME_DELAY = 30;
//    public static final double IS_EXPECTED_RESULT = 3.9;
//    public static final boolean IS_HAS_DILUTION = false;


}
