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

package org.akvo.caddisfly.common;

import org.akvo.caddisfly.model.TestSampleType;

public final class TestConstants {

    public static final int STRIP_TESTS_COUNT = 20;

    // Soil - Available Iron
//    public static final String CUVETTE_TEST_ID_1 = "3353f5cf-1cd2-4bf5-b47f-15d3db917add";
//    public static final int TEST_INDEX = 1;

    // Soil - pH
//    public static final TestSampleType IS_TEST_TYPE = TestSampleType.WATER;
//    public static final String IS_TEST_GROUP = "Soil Tests 1";
//    public static final String IS_TEST_NAME = "pH";
//    public static final String IS_TEST_ID = TestConstant.SOIL_PH_ID;
//    public static final int IS_START_DELAY = 0;
//    public static final int IS_TEST_INDEX = 0;
//    public static final int IS_TIME_DELAY = 30;
//    public static final double IS_EXPECTED_RESULT = 3.9;
//    public static final boolean IS_HAS_DILUTION = false;

    public static final String IS_TEST_HIGH_CALIBRATION = "HighLevelTest";
    public static final int IS_TEST_CALIBRATION_INDEX = 4;
//    public static final String IS_TEST_VALID_CALIBRATION = "pH_Valid";

    // Water - Fluoride
    public static final TestSampleType IS_TEST_TYPE = TestSampleType.WATER;
    public static final String IS_TEST_GROUP = "Water Tests 1";
    public static final String IS_TEST_NAME = "Fluoride";
    public static final String IS_TEST_ID = Constants.FLUORIDE_ID;
    public static final String IS_TEST_VALID_CALIBRATION = "Fluoride_Valid";
    public static final int IS_START_DELAY = 34;
    public static final int IS_TEST_INDEX = 5;
    public static final int IS_TIME_DELAY = 0;
    public static final double IS_EXPECTED_RESULT = 1.4;
    public static final boolean IS_HAS_DILUTION = true;

    public static final int CUVETTE_TEST_TIME_DELAY = 0;
    public static final int IS_EXTRA_DELAY = 5;

}
