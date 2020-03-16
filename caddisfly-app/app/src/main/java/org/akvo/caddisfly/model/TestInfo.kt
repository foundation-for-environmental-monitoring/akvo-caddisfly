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

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.helper.SwatchHelper.generateGradient
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TestInfo : Parcelable {
    @Transient
    private val symbols = DecimalFormatSymbols(Locale.US)

    @Transient
    private val decimalFormat = DecimalFormat("#.###", symbols)

    //    @SerializedName("reagentType")
    //    @Expose
    //    private String reagentType = "";
    @SerializedName("reagents")
    @Expose
    private var reagents: List<Reagent> = emptyList()

    @SerializedName("isCategory")
    @Expose
    private var isCategory = false

    @SerializedName("category")
    @Expose
    private var category: String? = null

    @JvmField
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("nameSuffix")
    @Expose
    var nameSuffix: String? = null
        private set

    @SerializedName("description")
    @Expose
    private var description: String? = null

    @SerializedName("type")
    @Expose
    var sampleType = TestSampleType.ALL
        private set

    @SerializedName("subtype")
    @Expose
    var subtype: TestType? = null
        private set

    @SerializedName("tags")
    @Expose
    private var tags: List<String>? = null

    @SerializedName("uuid")
    @Expose
    var uuid: String? = null
        private set

    @SerializedName("calibration")
    @Expose
    private var calibration: String? = null

    @SerializedName("brand")
    @Expose
    var brand: String? = null
        private set

    @SerializedName("brandUrl")
    @Expose
    var brandUrl: String? = ""
        private set

    @SerializedName("groupingType")
    @Expose
    var groupingType: GroupType? = null
        private set

    @SerializedName("illuminant")
    @Expose
    private var illuminant: String? = null

    @SerializedName("length")
    @Expose
    private var length: Double? = null

    @SerializedName("height")
    @Expose
    private var height: Double? = null

    @SerializedName("unit")
    @Expose
    private var unit: String? = null

    @SerializedName("hasImage")
    @Expose
    private var hasImage: Boolean? = false

    @JvmField
    @SerializedName("cameraAbove")
    @Expose
    var cameraAbove: Boolean? = false

    @SerializedName("results")
    @Expose
    var results: List<Result>? = ArrayList()
        private set

    @SerializedName("calibrate")
    @Expose
    private var calibrate: Boolean? = false

    @SerializedName("ranges")
    @Expose
    var ranges: String? = null
        private set

    @SerializedName("dilutions")
    @Expose
    val dilutions: List<Int> = ArrayList()

    //    public void setSwatches(List<Swatch> swatches) {
    //        this.swatches = swatches;
    //    }
    @SerializedName("monthsValid")
    @Expose
    var monthsValid: Int? = null
        private set

    //
    //    public Double getHeight() {
    //        return height;
    //    }
    //
    //    public void setHeight(Double height) {
    //        this.height = height;
    //    }
    @SerializedName("sampleQuantity")
    @Expose
    var sampleQuantity: String? = null
        private set

    @SerializedName("selectInstruction")
    @Expose
    private var selectInstruction: String? = null

    @SerializedName("endInstruction")
    @Expose
    private var endInstruction: String? = null

    @SerializedName("hasEndInstruction")
    @Expose
    private var hasEndInstruction: Boolean? = null

    @SerializedName("instructions")
    @Expose
    var instructions: List<Instruction>? = null
        private set

    @SerializedName("instructions2")
    @Expose
    private var instructions2: List<Instruction>? = null

    //    public void setInstructions(List<Instruction> instructions) {
    //        this.instructions = instructions;
    //    }
    @SerializedName("image")
    @Expose
    var image: String? = null
        private set

    @SerializedName("numPatch")
    @Expose
    private var numPatch: Int? = null

    @SerializedName("deviceId")
    @Expose
    private var deviceId: String? = null

    @SerializedName("responseFormat")
    @Expose
    private var responseFormat: String? = null

    val presetColors: List<ColorItem>?
        get() {
            return results!![0].presetColors
        }

    //    public void setImage(String image) {
    //        this.image = image;
    //    }
    @SerializedName("imageScale")
    @Expose
    var imageScale: String? = null
        private set

    @JvmField
    var resultSuffix: String? = ""

    var calibrations: List<Calibration> = ArrayList()
        set(value) {
            swatches.clear()
            if (results!!.isNotEmpty()) {
                val result = results!![0]
                val newCalibrations: MutableList<Calibration> = ArrayList()
                for (colorItem in result.colors) {
                    val newCalibration = Calibration(colorItem.value!!, Color.TRANSPARENT)
                    newCalibration.uid = uuid!!
                    for (i in value.indices.reversed()) {
                        val calibration = value[i]
                        if (calibration.value == colorItem.value) {
                            newCalibration.color = calibration.color
                            newCalibration.date = calibration.date
                            newCalibration.image = calibration.image
                            newCalibration.croppedImage = calibration.croppedImage
                            colorItem.rgbInt = calibration.color
                        }
                    }
                    val swatch = Swatch(newCalibration.value, newCalibration.color, Color.TRANSPARENT)
                    swatches.add(swatch)
                    val text = abs(newCalibration.value).toString()
                    if (newCalibration.value % 1 != 0.0) {
                        decimalPlaces = max(text.length - text.indexOf('.') - 1, decimalPlaces)
                    }
                    newCalibrations.add(newCalibration)
                }
                field = newCalibrations
                swatches = generateGradient(swatches)
            }
        }


    var dilution = 1
        set(dilution) {
            field = max(1, dilution)
        }

    //    /**
    //     * Connected device id.
    //     *
    //     * @return the device id
    //     */
    //    public String getDeviceId() {
    //        return deviceId;
    //    }
    //
    //    /**
    //     * Use the response format to display the results in test id order.
    //     *
    //     * @return the Format
    //     */
    //    public String getResponseFormat() {
    //        return responseFormat;
    //    }
    //
    var swatches: MutableList<Swatch> = ArrayList()
    var decimalPlaces = 0
    var resultDetail: ResultDetail? = null

    constructor()
    constructor(categoryName: String?) {
        category = categoryName
        isCategory = true
    }

    private constructor(`in`: Parcel) {
        isCategory = `in`.readByte().toInt() != 0
        category = `in`.readString()
        name = `in`.readString()
        nameSuffix = `in`.readString()
        subtype = TestType.valueOf(`in`.readString()!!)
        sampleType = TestSampleType.valueOf(`in`.readString()!!)
        description = `in`.readString()
        tags = `in`.createStringArrayList()
        reagents = ArrayList()
        `in`.readTypedList(reagents, Reagent.CREATOR)
        uuid = `in`.readString()
        calibration = `in`.readString()
        calibrations = ArrayList()
        `in`.readTypedList(calibrations, Calibration.CREATOR)
        swatches = ArrayList()
        `in`.readTypedList(swatches, Swatch.CREATOR)
        brand = `in`.readString()
        brandUrl = `in`.readString()
        val tmpGroupingType = `in`.readString()
        if (tmpGroupingType != null && !tmpGroupingType.equals("null", ignoreCase = true)) {
            groupingType = GroupType.valueOf(tmpGroupingType)
        }
        illuminant = `in`.readString()
        length = if (`in`.readByte().toInt() == 0) {
            0.0
        } else {
            `in`.readDouble()
        }
        height = if (`in`.readByte().toInt() == 0) {
            0.0
        } else {
            `in`.readDouble()
        }
        unit = `in`.readString()
        val tmpHasImage = `in`.readByte()
        hasImage = tmpHasImage.toInt() == 1
        val tmpCameraAbove = `in`.readByte()
        cameraAbove = tmpCameraAbove.toInt() == 1
        val tmpCalibrate = `in`.readByte()
        calibrate = tmpCalibrate.toInt() == 1
        ranges = `in`.readString()
        `in`.readList(dilutions, Int::class.java.classLoader)
        monthsValid = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        sampleQuantity = `in`.readString()
        results = ArrayList()
        `in`.readTypedList(results!!, Result.CREATOR)
        instructions = ArrayList()
        `in`.readTypedList(instructions!!, Instruction.CREATOR)
        instructions2 = ArrayList()
        `in`.readTypedList(instructions2!!, Instruction.CREATOR)
        selectInstruction = `in`.readString()
        endInstruction = `in`.readString()
        val tmpHasEndInstruction = `in`.readByte()
        hasEndInstruction = tmpHasEndInstruction.toInt() == 1
        image = `in`.readString()
        imageScale = `in`.readString()
        numPatch = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        deviceId = `in`.readString()
        responseFormat = `in`.readString()
        decimalPlaces = if (`in`.readByte().toInt() == 0) {
            0
        } else {
            `in`.readInt()
        }
        resultSuffix = `in`.readString()
    }

    //
    //    public boolean getCameraAbove() {
    //        return cameraAbove == null ? false : cameraAbove;
    //    }
    //    public void setCameraAbove(boolean value) {
    //        cameraAbove = value;
    //    }
    //
    val isGroup: Boolean
        get() {
            return isCategory
        }

    fun getCategory(): String? {
        return category
    }

    //    public double getMinRangeValue() {
    //        if (ranges != null) {
    //            try {
    //                String[] array = ranges.split(",");
    //                return Double.parseDouble(array[0]);
    //            } catch (NumberFormatException e) {
    //                return -1;
    //            }
    //        } else {
    //            return -1;
    //        }
    //    }
    //
    private fun getMaxRangeValue(): Double {
        return if (ranges != null) {
            try {
                val array = ranges!!.split(",").toTypedArray()
                array[array.size - 1].toDouble()
            } catch (e: NumberFormatException) {
                (-1).toDouble()
            }
        } else {
            (-1).toDouble()
        }
    }

    val minMaxRange: String
        get() {
            if (results != null && results!!.isNotEmpty()) {
                val minMaxRange = StringBuilder()
                for (result in results!!) {
                    result.colors
                    if (result.colors.size > 0) {
                        val valueCount = result.colors.size
                        if (minMaxRange.isNotEmpty()) {
                            minMaxRange.append(", ")
                        }
                        if (result.colors.size > 0) {
                            minMaxRange.append(String.format(Locale.US, "%s - %s",
                                    decimalFormat.format(result.colors[0].value),
                                    decimalFormat.format(result.colors[valueCount - 1].value)))
                        }
                        if (groupingType === GroupType.GROUP) {
                            break
                        }
                    } else {
                        if (ranges != null) {
                            val rangeArray = ranges!!.split(",").toTypedArray()
                            if (rangeArray.size > 1) {
                                if (minMaxRange.isNotEmpty()) {
                                    minMaxRange.append(", ")
                                }
                                val maxRangeValue = result.calculateResult(getMaxRangeValue())
                                minMaxRange.append(rangeArray[0].trim { it <= ' ' }).append(" - ")
                                        .append(decimalFormat.format(maxRangeValue))
                                minMaxRange.append(" ")
                                minMaxRange.append(result.unit)
                            }
                        }
                    }
                }
                if (dilutions.size > 1) {
                    val maxDilution = dilutions[min(dilutions.size - 1, 2)]
                    val result = results!![0]
                    val maxRangeValue = result.calculateResult(getMaxRangeValue())
                    val text: String
                    text = if (dilutions.size > 3) {
                        String.format(Locale.US, " (<dilutionRange>%s+</dilutionRange>)",
                                decimalFormat.format(maxDilution * maxRangeValue))
                    } else {
                        String.format(" (<dilutionRange>%s</dilutionRange>)",
                                decimalFormat.format(maxDilution * maxRangeValue))
                    }
                    return minMaxRange.toString() + text
                }
                return String.format("%s", minMaxRange.toString())
            }
            return ""
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeByte((if (isCategory) 1 else 0).toByte())
        parcel.writeString(category)
        parcel.writeString(name)
        parcel.writeString(nameSuffix)
        parcel.writeString(subtype!!.name)
        parcel.writeString(sampleType.name)
        parcel.writeString(description)
        parcel.writeStringList(tags)
        parcel.writeTypedList(reagents)
        parcel.writeString(uuid)
        parcel.writeString(calibration)
        parcel.writeTypedList(calibrations)
        parcel.writeTypedList(swatches)
        parcel.writeString(brand)
        parcel.writeString(brandUrl)
        parcel.writeString(groupingType.toString())
        parcel.writeString(illuminant)
        if (length == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeDouble(length!!)
        }
        if (height == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeDouble(height!!)
        }
        parcel.writeString(unit)
        parcel.writeByte((if (hasImage == null) 0 else if (hasImage!!) 1 else 2).toByte())
        parcel.writeByte((if (cameraAbove == null) 0 else if (cameraAbove!!) 1 else 2).toByte())
        parcel.writeByte((if (calibrate == null) 0 else if (calibrate!!) 1 else 2).toByte())
        parcel.writeString(ranges)
        parcel.writeList(dilutions)
        if (monthsValid == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeInt(monthsValid!!)
        }
        parcel.writeString(sampleQuantity)
        parcel.writeTypedList(results)
        parcel.writeTypedList(instructions)
        parcel.writeTypedList(instructions2)
        parcel.writeString(selectInstruction)
        parcel.writeString(endInstruction)
        parcel.writeByte((if (hasEndInstruction == null) 0 else if (hasEndInstruction!!) 1 else 2).toByte())
        parcel.writeString(image)
        parcel.writeString(imageScale)
        if (numPatch == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeInt(numPatch!!)
        }
        parcel.writeString(deviceId)
        parcel.writeString(responseFormat)
        parcel.writeByte(1.toByte())
        parcel.writeInt(decimalPlaces)
        parcel.writeString(resultSuffix)
    }

    fun getReagent(i: Int): Reagent {
        return if (reagents.size > i) {
            reagents[i]
        } else {
            Reagent()
        }
    }

    //    public String getSelectInstruction() {
    //        return selectInstruction;
    //    }
    //
    //    public String getEndInstruction() {
    //        return endInstruction;
    //    }
    fun getStripLength(): Double {
        return length!!
    }

    //    public Boolean getHasImage() {
    //        return hasImage;
    //    }
    val maxDilution: Int
        get() {
            return if (dilutions.isNotEmpty()) {
                dilutions[dilutions.size - 1]
            } else {
                1
            }
        }

    companion object CREATOR : Parcelable.Creator<TestInfo> {
        override fun createFromParcel(parcel: Parcel): TestInfo {
            return TestInfo(parcel)
        }

        override fun newArray(size: Int): Array<TestInfo?> {
            return arrayOfNulls(size)
        }
    }
}