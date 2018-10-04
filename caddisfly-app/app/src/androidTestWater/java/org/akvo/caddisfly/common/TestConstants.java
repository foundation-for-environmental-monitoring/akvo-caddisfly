package org.akvo.caddisfly.common;

import org.akvo.caddisfly.model.TestSampleType;

public final class TestConstants {

    public static final int STRIP_TESTS_COUNT = 1;
    public static final int CUVETTE_TESTS_COUNT = 13;

    // Water - Fluoride
    public static final TestSampleType IS_TEST_TYPE = TestSampleType.WATER;
    public static final String TEST_GROUP = "Water Tests 1";
    public static final String TEST_NAME = "Fluoride";
    public static final String CUVETTE_TEST_ID_1 = Constants.FLUORIDE_ID;
    public static final int TEST_START_DELAY = 34;
    public static final int TEST_INDEX = 6;
    public static final int CUVETTE_TEST_TIME_DELAY = 0;
    public static final int DELAY_EXTRA = 4;

    public static final String CUVETTE_TEST_NAME_1 = "Aluminum";
    public static final double EXPECTED_RESULT = 1.4;
    public static final boolean IS_HAS_DILUTION = true;
}
