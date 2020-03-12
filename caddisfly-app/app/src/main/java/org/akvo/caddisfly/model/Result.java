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

package org.akvo.caddisfly.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Result implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("unit")
    @Expose
    private String unit = "";
    @SerializedName("formula")
    @Expose
    private String formula = "";
    @SerializedName("unitChoice")
    @Expose
    private String unitChoice = "";
    @SerializedName("patchPos")
    @Expose
    private Double patchPos;
    @SerializedName("patchWidth")
    @Expose
    private Double patchWidth;
    @SerializedName("timeDelay")
    @Expose
    private Integer timeDelay = 0;
    @SerializedName("testStage")
    @Expose
    private Integer testStage = 1;
    @SerializedName("colors")
    @Expose
    private List<ColorItem> colorItems = new ArrayList<>();
    @SerializedName("preset")
    @Expose
    private List<ColorItem> presetColors = new ArrayList<>();
    @SerializedName("grayScale")
    @Expose
    private Boolean grayScale = false;
    @SerializedName("code")
    @Expose
    private String code;
    private String result;
    private boolean highLevelsFound;
    private double resultValue;
    private Integer dilution = 0;

    public Result() {
    }

    private Result(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        unit = in.readString();
        formula = in.readString();
        unitChoice = in.readString();
        patchPos = in.readByte() == 0x00 ? null : in.readDouble();
        patchWidth = in.readByte() == 0x00 ? null : in.readDouble();
        timeDelay = in.readByte() == 0x00 ? null : in.readInt();
        testStage = in.readByte() == 0x00 ? null : in.readInt();
        if (in.readByte() == 0x01) {
            colorItems = new ArrayList<>();
            in.readList(colorItems, ColorItem.class.getClassLoader());
        } else {
            colorItems = null;
        }
        if (in.readByte() == 0x01) {
            presetColors = new ArrayList<>();
            in.readList(presetColors, ColorItem.class.getClassLoader());
        } else {
            presetColors = null;
        }
        byte tmpGrayScale = in.readByte();
        grayScale = tmpGrayScale == 1;
        code = in.readString();
        dilution = in.readByte() == 0x00 ? null : in.readInt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFormula() {
        return formula;
    }

    public String getUnitChoice() {
        return unitChoice;
    }

    public Double getPatchPos() {
        return patchPos;
    }

    public void setPatchPos(Double patchPos) {
        this.patchPos = patchPos;
    }

    public Double getPatchWidth() {
        return patchWidth;
    }

    public void setPatchWidth(Double patchWidth) {
        this.patchWidth = patchWidth;
    }

    /**
     * Time to wait before analyzing.
     *
     * @return the time delay milli seconds
     */
    public Integer getTimeDelay() {
        if (AppPreferences.ignoreTimeDelays()) {
            // use the id as seconds when ignoring actual timeDelay
            return id;
        } else {
            return timeDelay;
        }
    }

    public void setTimeDelay(Integer timeDelay) {
        this.timeDelay = timeDelay;
    }

    public Integer getTestStage() {
        return testStage;
    }

    public List<ColorItem> getColors() {
        return colorItems;
    }

    public void setColorItems(List<ColorItem> colorItems) {
        this.colorItems = colorItems;
    }

    List<ColorItem> getPresetColors() {
        return presetColors;
    }

    public Boolean getGrayScale() {
        return grayScale;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(id);
        }
        dest.writeString(name);
        dest.writeString(unit);
        dest.writeString(formula);
        dest.writeString(unitChoice);
        if (patchPos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(patchPos);
        }
        if (patchWidth == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(patchWidth);
        }
        if (timeDelay == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(timeDelay);
        }
        if (testStage == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(testStage);
        }
        if (colorItems == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(colorItems);
        }
        if (presetColors == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(presetColors);
        }
        dest.writeByte((byte) (grayScale == null ? 0 : grayScale ? 1 : 2));
        dest.writeString(code);
        if (dilution == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(dilution);
        }
    }

    public String getResult() {
        return result;
    }

    public void setResult(String value) {
        result = value;
    }

    public void setResult(double resultDouble, int dilution, Integer maxDilution) {

        this.dilution = dilution;

        if (resultDouble == -1) {
            result = "";
        } else {
            if (colorItems.size() > 0) {
                // determine if high levels of contaminant
                double maxResult = colorItems.get(colorItems.size() - 1).getValue();
                highLevelsFound = resultDouble > maxResult * 0.95;

                resultValue = applyFormula(resultDouble * dilution, formula);

                // if no more dilution can be performed then set result to highest value
                if (highLevelsFound && dilution >= maxDilution) {
                    resultValue = applyFormula(maxResult * dilution, formula);
                }

                result = String.format(Locale.getDefault(), "%.2f", resultValue);

                // Add 'greater than' symbol if result could be an unknown high value
                if (highLevelsFound) {
                    result = "> " + result;
                }
            } else {
                result = String.format(Locale.getDefault(), "%.2f", resultDouble);
            }
        }
    }

    private double applyFormula(double value, String formula) {
        // if we don't have a valid result, return the value unchanged
        if (value == -1 || Double.isNaN(value)) {
            return value;
        }

        if (!formula.isEmpty()) {
            return (float) MathUtil.eval(String.format(Locale.US,
                    formula, value));
        }
        // if we didn't have a formula, return the unchanged value.
        return value;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean highLevelsFound() {
        return highLevelsFound;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getResultValue() {
        return resultValue;
    }

    public int getDilution() {
        return dilution;
    }

    double calculateResult(double value) {
        return applyFormula(value, formula);
    }
}