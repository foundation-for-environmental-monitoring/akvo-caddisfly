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

package org.akvo.caddisfly.color

import android.graphics.Color
import org.akvo.caddisfly.helper.SwatchHelper
import org.akvo.caddisfly.model.ResultDetail
import org.akvo.caddisfly.util.ClassUtil
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.lang.reflect.InvocationTargetException
import java.util.*

class SwatchHelperTest {
    @Before
    fun setUp() {
        try {
            ClassUtil.assertUtilityClassWellDefined(SwatchHelper::class.java)
        } catch (e: NoSuchMethodException) {
            Timber.e(e)
        } catch (e: InvocationTargetException) {
            Timber.e(e)
        } catch (e: IllegalAccessException) {
            Timber.e(e)
        } catch (e: InstantiationException) {
            Timber.e(e)
        }
    }

    private fun createNewResult(value: Double, color: Int = 0): ResultDetail {
        return ResultDetail(value, color, 0)
    }

    @Test
    fun testGetAverageResult() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.5))
        results.add(createNewResult(0.0))
        results.add(createNewResult(1.7))
        results.add(createNewResult(0.0))
        results.add(createNewResult(1.8))
        results.add(createNewResult(0.0))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.16, result, 0.0)
    }

    @Test
    fun testAverage2() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.61, result, 0.0)
    }

    @Test
    fun testAverage3() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.5))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.63, result, 0.0)
    }

    @Test
    fun testAverage4() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.6, result, 0.0)
    }

    @Test
    fun testAverage5() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.7))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.62, result, 0.0)
    }

    fun testAverage6() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.7))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(-1.0, result, 0.0)
    }

    @Test
    fun testAverage7() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.63, result, 0.0)
    }

    @Test
    fun testAverage8() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6))
        results.add(createNewResult(1.8))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.7))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.5))
        results.add(createNewResult(1.6))
        val result = SwatchHelper.getAverageResult(results)
        assertEquals(1.63, result, 0.0)
    }

    @Test
    fun testGetAverageColor1() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)))
        results.add(createNewResult(1.8, Color.rgb(250, 250, 250)))
        results.add(createNewResult(1.7, Color.rgb(245, 245, 245)))
        results.add(createNewResult(1.7, Color.rgb(240, 240, 240)))
        results.add(createNewResult(1.5, Color.rgb(235, 235, 235)))
        results.add(createNewResult(1.5, Color.rgb(235, 235, 235)))
        results.add(createNewResult(1.6, Color.rgb(230, 230, 230)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(0, color)
    }

    @Test
    fun testGetAverageColor2() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)))
        results.add(createNewResult(1.8, Color.rgb(250, 250, 250)))
        results.add(createNewResult(1.7, Color.rgb(245, 245, 245)))
        results.add(createNewResult(1.7, Color.rgb(240, 240, 240)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(0, color)
    }

    @Test
    fun testGetAverageColor3() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)))
        results.add(createNewResult(1.8, Color.rgb(250, 250, 250)))
        results.add(createNewResult(1.7, Color.rgb(245, 245, 245)))
        results.add(createNewResult(1.7, Color.rgb(240, 240, 240)))
        results.add(createNewResult(1.5, Color.rgb(235, 235, 235)))
        results.add(createNewResult(1.5, Color.rgb(210, 230, 210)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(0, color)
    }

    @Test
    fun testGetAverageColor4() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.8, Color.rgb(250, 254, 250)))
        results.add(createNewResult(1.8, Color.rgb(251, 253, 250)))
        results.add(createNewResult(1.8, Color.rgb(252, 252, 250)))
        results.add(createNewResult(1.8, Color.rgb(253, 251, 250)))
        results.add(createNewResult(1.8, Color.rgb(254, 250, 250)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(Color.rgb(252, 252, 250), color)
    }

    @Test
    fun testGetAverageColor5() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6, Color.rgb(225, 3, 1)))
        results.add(createNewResult(1.8, Color.rgb(225, 1, 2)))
        results.add(createNewResult(1.8, Color.rgb(215, 1, 1)))
        results.add(createNewResult(1.8, Color.rgb(225, 1, 4)))
        results.add(createNewResult(1.8, Color.rgb(225, 6, 1)))
        results.add(createNewResult(1.8, Color.rgb(225, 1, 8)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(Color.rgb(223, 2, 2), color)
    }

    @Test
    fun testGetAverageColor6() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.6, Color.rgb(179, 128, 81)))
        results.add(createNewResult(1.8, Color.rgb(176, 126, 77)))
        results.add(createNewResult(1.8, Color.rgb(177, 125, 77)))
        results.add(createNewResult(1.8, Color.rgb(177, 125, 77)))
        results.add(createNewResult(1.8, Color.rgb(175, 125, 76)))
        results.add(createNewResult(1.8, Color.rgb(175, 124, 77)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(Color.rgb(176, 125, 77), color)
    }

    @Test
    fun testGetAverageColor7() {
        val results = ArrayList<ResultDetail>()

//        results.add(createNewResult(10, Color.rgb(175, 124, 77)));
//        results.add(createNewResult(20, Color.rgb(175, 124, 77)));


        results.add(createNewResult(1.90, Color.rgb(253, 0, 18)))
        results.add(createNewResult(40.0, Color.rgb(254, 1, 21)))
        results.add(createNewResult(0.0, Color.rgb(254, 1, 19)))
        results.add(createNewResult(0.0, Color.rgb(253, 0, 18)))
        results.add(createNewResult(0.0, Color.rgb(253, 0, 18)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(Color.rgb(253, 0, 18), color)
    }

    @Test
    fun testGetAverageColor8() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.90, Color.rgb(184, 134, 45)))
        results.add(createNewResult(40.0, Color.rgb(182, 135, 45)))
        results.add(createNewResult(0.0, Color.rgb(186, 139, 46)))
        results.add(createNewResult(0.0, Color.rgb(180, 130, 41)))
        results.add(createNewResult(0.0, Color.rgb(181, 134, 44)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(Color.rgb(182, 134, 44), color)
    }

    @Test
    fun testGetAverageColor9() {
        val results = ArrayList<ResultDetail>()
        results.add(createNewResult(1.0, Color.rgb(196, 218, 156)))
        results.add(createNewResult(0.0, Color.rgb(198, 220, 158)))
        results.add(createNewResult(0.0, Color.rgb(197, 219, 157)))
        results.add(createNewResult(0.0, Color.rgb(197, 219, 157)))
        results.add(createNewResult(0.0, Color.rgb(193, 215, 151)))
        val color = SwatchHelper.getAverageColor(results)
        assertEquals(Color.rgb(196, 218, 155), color)
    }
}