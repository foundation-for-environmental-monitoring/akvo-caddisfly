package org.akvo.caddisfly.common;

import org.akvo.caddisfly.model.TestSampleType;

public final class TestConstants {

    public static final int STRIP_TESTS_COUNT = 1;
    public static final int CUVETTE_TESTS_COUNT = 13;


    public static final String IS_TEST_HIGH_CALIBRATION = "HighLevelTest";
    public static final int IS_TEST_CALIBRATION_INDEX = 4;
    public static final String IS_TEST_VALID_CALIBRATION = "Fluoride_Valid";

    // Water - Fluoride
    public static final TestSampleType IS_TEST_TYPE = TestSampleType.WATER;
    public static final String IS_TEST_GROUP = "Water Tests 1";
    public static final String IS_TEST_NAME = "Fluoride";
    public static final String IS_TEST_ID = Constants.FLUORIDE_ID;
    public static final int IS_START_DELAY = 60;
    public static final int IS_TEST_INDEX = 6;
    public static final double IS_EXPECTED_RESULT = 1.4;
    public static final boolean IS_HAS_DILUTION = true;

    public static final int CUVETTE_TEST_TIME_DELAY = 0;
    public static final int IS_EXTRA_DELAY = 3;
}
