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

import java.util.ArrayList;
import java.util.List;

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
    @SerializedName("md610_id")
    @Expose
    private String md610Id;
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
    private String result;
    private double resultDouble;
    private boolean highLevelsFound;

    public Result() {
    }

    protected Result(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        md610Id = in.readString();
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
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMd610Id() {
        return md610Id;
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

    public Integer getTimeDelay() {
        return timeDelay;
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
        dest.writeString(md610Id);
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
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getResultDouble() {
        return resultDouble;
    }

    public void setResultDouble(double resultDouble) {
        this.resultDouble = resultDouble;
    }

    public double getMaxValue() {
        return colorItems.get(colorItems.size() - 1).getValue();
    }

    public boolean isHighLevelsFound() {
        return false;
    }

    public void setHighLevelsFound(boolean highLevelsFound) {
        this.highLevelsFound = highLevelsFound;
    }
}