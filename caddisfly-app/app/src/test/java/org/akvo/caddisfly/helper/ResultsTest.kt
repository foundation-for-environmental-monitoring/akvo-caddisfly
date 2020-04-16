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

package org.akvo.caddisfly.helper

import android.os.Build
import android.util.SparseArray
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_ID_1
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_NAME_1
import org.akvo.caddisfly.common.UnitTestConstants.CUVETTE_TEST_UNIT
import org.akvo.caddisfly.repository.TestConfigRepository
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class ResultsTest {
    @Test
    fun testColorimetryResult() {
        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(CUVETTE_TEST_ID_1)!!
        val results = SparseArray<String>()
        results.put(1, "> 2.0")
        val resultJson: JSONObject = TestConfigHelper.getJsonResult(testInfo, results, null, -1, "")

        // Replace items that cannot be tested (e.g. currentTime)
        var json = resultJson.toString().replace("(\"testDate\":\").*?\"".toRegex(), "$1today\"")
        json = json.replace("(\"appVersion\":\").*?\"".toRegex(), "$1version\"")
        json = json.replace("(\"country\":\").*?\"".toRegex(), "$1\"")
        val expectedJson = "{\"type\":\"" + BuildConfig.APPLICATION_ID + "\",\"name\":\"" + CUVETTE_TEST_NAME_1 + "\",\"uuid\":\"" + CUVETTE_TEST_ID_1 + "\",\"result\":[{\"dilution\":0,\"name\":\"" + CUVETTE_TEST_NAME_1 + "\",\"unit\":\"" + CUVETTE_TEST_UNIT + "\",\"id\":1,\"value\":\"> 2.0\"}],\"testDate\":\"today\"}"
        assertEquals(expectedJson, json)
    }
}