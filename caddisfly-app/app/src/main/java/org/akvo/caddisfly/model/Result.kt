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
package org.akvo.caddisfly.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.akvo.caddisfly.preference.AppPreferences.ignoreTimeDelays

import org.akvo.caddisfly.util.MathUtil
import java.util.*

class Result : Parcelable {
    @JvmField
    @SerializedName("id")
    @Expose
    var id: Int = 0

    @JvmField
    @SerializedName("name")
    @Expose
    var name: String? = null

    @JvmField
    @SerializedName("unit")
    @Expose
    var unit: String? = ""

    @SerializedName("formula")
    @Expose
    var formula: String? = ""
        private set

    @SerializedName("unitChoice")
    @Expose
    var unitChoice: String? = ""
        private set

    @JvmField
    @SerializedName("patchPos")
    @Expose
    var patchPos: Double? = null

    @JvmField
    @SerializedName("patchWidth")
    @Expose
    var patchWidth: Double? = null

    @SerializedName("timeDelay")
    @Expose
    var timeDelay: Int = 0
        get() {
            return if (ignoreTimeDelays()) {
                // use the id as seconds when ignoring actual timeDelay
                id
            } else {
                field
            }
        }

    @SerializedName("testStage")
    @Expose
    var testStage: Int? = 1
        private set

    @SerializedName("colors")
    @Expose
    var colors: ArrayList<ColorItem> = ArrayList()
        private set

    @SerializedName("preset")
    @Expose
    var presetColors: List<ColorItem>? = ArrayList()
        private set

    @SerializedName("grayScale")
    @Expose
    var grayScale: Boolean = false
        private set

    @SerializedName("code")
    @Expose
    var code: String? = null

    @SerializedName("display")
    @Expose
    var display: Int? = 0

    @JvmField
    var result: String? = null
    private var highLevelsFound = false
    var resultValue = 0.0
        private set
    var dilution: Int? = 0
        private set

    constructor()
    private constructor(`in`: Parcel) {
        id = if (`in`.readByte().toInt() == 0x00) 0 else `in`.readInt()
        name = `in`.readString()
        unit = `in`.readString()
        formula = `in`.readString()
        unitChoice = `in`.readString()
        patchPos = if (`in`.readByte().toInt() == 0x00) null else `in`.readDouble()
        patchWidth = if (`in`.readByte().toInt() == 0x00) null else `in`.readDouble()
        timeDelay = if (`in`.readByte().toInt() == 0x00) 0 else `in`.readInt()
        testStage = if (`in`.readByte().toInt() == 0x00) null else `in`.readInt()
        if (`in`.readByte().toInt() == 0x01) {
            colors = ArrayList()
            `in`.readList(colors as List<*>, ColorItem::class.java.classLoader)
        } else {
            colors = ArrayList()
        }
        if (`in`.readByte().toInt() == 0x01) {
            presetColors = ArrayList()
            `in`.readList(presetColors!!, ColorItem::class.java.classLoader)
        } else {
            presetColors = null
        }
        val tmpGrayScale = `in`.readByte()
        grayScale = tmpGrayScale.toInt() == 1
        code = `in`.readString()
        display = if (`in`.readByte().toInt() == 0x00) null else `in`.readInt()
        dilution = (if (`in`.readByte().toInt() == 0x00) null else `in`.readInt())!!
    }

//    fun setColorItems(colorItems: List<ColorItem?>?) {
//        colors = colorItems
//    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(0x01.toByte())
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeString(unit)
        dest.writeString(formula)
        dest.writeString(unitChoice)
        if (patchPos == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeDouble(patchPos!!)
        }
        if (patchWidth == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeDouble(patchWidth!!)
        }
        dest.writeByte(0x01.toByte())
        dest.writeInt(timeDelay)
        if (testStage == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeInt(testStage!!)
        }
        dest.writeByte(0x01.toByte())
        dest.writeList(colors as List<*>?)
        if (presetColors == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeList(presetColors)
        }
        dest.writeByte((if (grayScale) 1 else 2).toByte())
        dest.writeString(code)
        if (display == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeInt(display!!)
        }
        if (dilution == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeInt(dilution!!)
        }
    }

    fun setResult(resultDouble: Double, dilution: Int, maxDilution: Int) {
        this.dilution = dilution
        if (resultDouble == -1.0) {
            result = ""
        } else {
            if (colors.isNotEmpty()) {
                // determine if high levels of contaminant
                val maxResult = colors[colors.size - 1].value!!
                highLevelsFound = resultDouble > maxResult * 0.95
                resultValue = applyFormula(resultDouble * dilution, formula)

                // if no more dilution can be performed then set result to highest value
                if (highLevelsFound) {
                    resultValue = applyFormula(maxResult * dilution, formula)
                }
                result = String.format(Locale.getDefault(), "%.2f", resultValue)

                // Add 'greater than' symbol if result could be an unknown high value
                if (highLevelsFound) {
                    result = "> $result"
                }
            } else {
                result = String.format(Locale.getDefault(), "%.2f", resultDouble)
            }
        }
    }

    private fun applyFormula(value: Double, formula: String?): Double {
        // if we don't have a valid result, return the value unchanged
        if (value == -1.0 || java.lang.Double.isNaN(value)) {
            return value
        }
        return if (formula!!.isNotEmpty()) {
            MathUtil.eval(String.format(Locale.US, formula, value))
        } else value
        // if we didn't have a formula, return the unchanged value.
    }

    fun highLevelsFound(): Boolean {
        return highLevelsFound
    }

    fun calculateResult(value: Double): Double {
        return applyFormula(value, formula)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Result?> = object : Parcelable.Creator<Result?> {
            override fun createFromParcel(`in`: Parcel): Result {
                return Result(`in`)
            }

            override fun newArray(size: Int): Array<Result?> {
                return arrayOfNulls(size)
            }
        }
    }
}