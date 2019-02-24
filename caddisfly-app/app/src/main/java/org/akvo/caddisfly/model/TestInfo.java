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

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SwatchHelper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestInfo implements Parcelable {

    public static final Creator<TestInfo> CREATOR = new Creator<TestInfo>() {
        @Override
        public TestInfo createFromParcel(Parcel in) {
            return new TestInfo(in);
        }

        @Override
        public TestInfo[] newArray(int size) {
            return new TestInfo[size];
        }
    };
    private transient DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    private transient DecimalFormat decimalFormat = new DecimalFormat("#.###", symbols);
    @SerializedName("reagents")
    @Expose
    private List<Reagent> reagents = null;
    @SerializedName("isCategory")
    @Expose
    private boolean isCategory;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("nameSuffix")
    @Expose
    private String nameSuffix;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("type")
    @Expose
    private TestSampleType sampleType = TestSampleType.ALL;
    @SerializedName("subtype")
    @Expose
    private TestType subtype;
    @SerializedName("tags")
    @Expose
    private List<String> tags = null;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("calibration")
    @Expose
    private String calibration;
    @SerializedName("brand")
    @Expose
    private String brand;
    @SerializedName("brandUrl")
    @Expose
    private String brandUrl = "";
    @SerializedName("groupingType")
    @Expose
    private GroupType groupingType;
    @SerializedName("illuminant")
    @Expose
    private String illuminant;
    @SerializedName("length")
    @Expose
    private Double length;
    @SerializedName("height")
    @Expose
    private Double height;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("hasImage")
    @Expose
    private Boolean hasImage = false;
    @SerializedName("cameraAbove")
    @Expose
    private Boolean cameraAbove = false;
    @SerializedName("results")
    @Expose
    private List<Result> results = new ArrayList<>();
    @SerializedName("calibrate")
    @Expose
    private Boolean calibrate = false;
    @SerializedName("ranges")
    @Expose
    private String ranges;
    @SerializedName("defaultColors")
    @Expose
    private String defaultColors;
    @SerializedName("hueTrend")
    @Expose
    private Integer hueTrend = 0;
    @SerializedName("dilutions")
    @Expose
    private List<Integer> dilutions = new ArrayList<>();
    @SerializedName("monthsValid")
    @Expose
    private Integer monthsValid;
    @SerializedName("md610_id")
    @Expose
    private String md610Id;
    @SerializedName("sampleQuantity")
    @Expose
    private String sampleQuantity;
    @SerializedName("selectInstruction")
    @Expose
    private String selectInstruction;
    @SerializedName("instructions")
    @Expose
    private List<Instruction> instructions = null;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("numPatch")
    @Expose
    private Integer numPatch;
    @SerializedName("deviceId")
    @Expose
    private String deviceId;
    @SerializedName("responseFormat")
    @Expose
    private String responseFormat;
    @SerializedName("imageScale")
    @Expose
    private String imageScale;
    private String resultSuffix = "";

    private List<Calibration> calibrations = new ArrayList<>();
    private int dilution = 1;
    private List<Swatch> swatches = new ArrayList<>();
    private List<Swatch> oneStepSwatches = new ArrayList<>();
    private Integer decimalPlaces = 0;

    private ResultDetail resultDetail;
    private List<Calibration> oneStepCalibrations;
    private double pivotCalibration;

    public TestInfo() {
    }

    public TestInfo(String categoryName) {
        category = categoryName;
        isCategory = true;
    }

    public TestInfo(String name, String id) {
        this.name = name;
        uuid = id;
    }

    protected TestInfo(Parcel in) {
        isCategory = in.readByte() != 0;
        category = in.readString();
        name = in.readString();
        nameSuffix = in.readString();
        subtype = TestType.valueOf(in.readString());
        sampleType = TestSampleType.valueOf(in.readString());
        description = in.readString();
        tags = in.createStringArrayList();
        reagents = new ArrayList<>();
        in.readTypedList(reagents, Reagent.CREATOR);
        uuid = in.readString();
        calibration = in.readString();

        calibrations = new ArrayList<>();
        in.readTypedList(calibrations, Calibration.CREATOR);

        oneStepCalibrations = new ArrayList<>();
        in.readTypedList(oneStepCalibrations, Calibration.CREATOR);

        swatches = new ArrayList<>();
        in.readTypedList(swatches, Swatch.CREATOR);

        oneStepSwatches = new ArrayList<>();
        in.readTypedList(oneStepSwatches, Swatch.CREATOR);

        pivotCalibration = in.readDouble();

        brand = in.readString();
        brandUrl = in.readString();
        String tmpGroupingType = in.readString();
        if (!tmpGroupingType.equalsIgnoreCase("null")) {
            groupingType = GroupType.valueOf(tmpGroupingType);
        }
        illuminant = in.readString();
        if (in.readByte() == 0) {
            length = 0.0;
        } else {
            length = in.readDouble();
        }
        if (in.readByte() == 0) {
            height = 0.0;
        } else {
            height = in.readDouble();
        }
        unit = in.readString();
        byte tmpHasImage = in.readByte();
        hasImage = tmpHasImage != 0 && tmpHasImage == 1;
        byte tmpCameraAbove = in.readByte();
        cameraAbove = tmpCameraAbove != 0 && tmpCameraAbove == 1;
        byte tmpCalibrate = in.readByte();
        calibrate = tmpCalibrate != 0 && tmpCalibrate == 1;
        ranges = in.readString();
        defaultColors = in.readString();
        if (in.readByte() == 0) {
            hueTrend = null;
        } else {
            hueTrend = in.readInt();
        }
        in.readList(this.dilutions, (java.lang.Integer.class.getClassLoader()));
        if (in.readByte() == 0) {
            monthsValid = null;
        } else {
            monthsValid = in.readInt();
        }
        md610Id = in.readString();
        sampleQuantity = in.readString();

        results = new ArrayList<>();
        in.readTypedList(results, Result.CREATOR);

        instructions = new ArrayList<>();
        in.readTypedList(instructions, Instruction.CREATOR);
        selectInstruction = in.readString();
        image = in.readString();
        imageScale = in.readString();
        if (in.readByte() == 0) {
            numPatch = null;
        } else {
            numPatch = in.readInt();
        }
        deviceId = in.readString();
        responseFormat = in.readString();
        if (in.readByte() == 0) {
            decimalPlaces = 0;
        } else {
            decimalPlaces = in.readInt();
        }
        resultSuffix = in.readString();
    }

    public boolean getCameraAbove() {
        return cameraAbove == null ? false : cameraAbove;
    }

    public void setCameraAbove(boolean value) {
        cameraAbove = value;
    }

    public boolean getIsGroup() {
        return isCategory;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public TestType getSubtype() {
        return subtype;
    }

    public TestSampleType getSampleType() {
        return sampleType;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBrand() {
        return brand;
    }

    public String getRanges() {
        return ranges;
    }

    public double getMinRangeValue() {
        if (ranges != null) {
            try {
                String[] array = ranges.split(",");
                return Double.valueOf(array[0]);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public double getMaxRangeValue() {
        if (ranges != null) {
            try {
                String[] array = ranges.split(",");
                return Double.valueOf(array[array.length - 1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public List<Integer> getDilutions() {
        return dilutions;
    }

    public String getMinMaxRange() {

        if (results != null && results.size() > 0) {
            StringBuilder minMaxRange = new StringBuilder();
            for (Result result : results) {
                if (result.getColors() != null && result.getColors().size() > 0) {
                    int valueCount = result.getColors().size();
                    if (minMaxRange.length() > 0) {
                        minMaxRange.append(", ");
                    }
                    if (result.getColors().size() > 0) {
                        minMaxRange.append(String.format(Locale.US, "%s - %s",
                                decimalFormat.format(result.getColors().get(0).getValue()),
                                decimalFormat.format(result.getColors().get(valueCount - 1).getValue())));
                    }
                    if (groupingType == GroupType.GROUP) {
                        break;
                    }
                } else {
                    if (ranges != null) {
                        String[] rangeArray = ranges.split(",");
                        if (rangeArray.length > 1) {
                            if (minMaxRange.length() > 0) {
                                minMaxRange.append(", ");
                            }

                            double maxRangeValue = result.calculateResult(getMaxRangeValue());

                            minMaxRange.append(rangeArray[0].trim()).append(" - ")
                                    .append(decimalFormat.format(maxRangeValue));
                            minMaxRange.append(" ");
                            minMaxRange.append(result.getUnit());
                        }
                    }
                }
            }

            if (dilutions.size() > 1) {
                int maxDilution = dilutions.get(Math.min(dilutions.size() - 1, 2));
                Result result = results.get(0);

                double maxRangeValue = result.calculateResult(getMaxRangeValue());

                String text;
                if (dilutions.size() > 3) {
                    text = String.format(" (More than %s with dilution)", maxDilution * maxRangeValue);
                } else {
                    text = String.format(" (Upto %s with dilution)", maxDilution * maxRangeValue);
                }

                return minMaxRange.toString() + text;
            }

            return String.format("%s", minMaxRange.toString());
        }
        return "";
    }

    public String getMd610Id() {
        return md610Id;
    }

    public String getBrandUrl() {
        return brandUrl;
    }

    public GroupType getGroupingType() {
        return groupingType;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getSampleQuantity() {
        return sampleQuantity;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageScale() {
        return imageScale;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isCategory ? 1 : 0));
        parcel.writeString(category);
        parcel.writeString(name);
        parcel.writeString(nameSuffix);
        parcel.writeString(subtype.name());
        parcel.writeString(sampleType.name());
        parcel.writeString(description);
        parcel.writeStringList(tags);
        parcel.writeTypedList(reagents);
        parcel.writeString(uuid);
        parcel.writeString(calibration);
        parcel.writeTypedList(calibrations);
        parcel.writeTypedList(oneStepCalibrations);
        parcel.writeTypedList(swatches);
        parcel.writeTypedList(oneStepSwatches);
        parcel.writeDouble(pivotCalibration);
        parcel.writeString(brand);
        parcel.writeString(brandUrl);
        parcel.writeString(String.valueOf(groupingType));
        parcel.writeString(illuminant);
        if (length == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(length);
        }
        if (height == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(height);
        }
        parcel.writeString(unit);
        parcel.writeByte((byte) (hasImage == null ? 0 : hasImage ? 1 : 2));
        parcel.writeByte((byte) (cameraAbove == null ? 0 : cameraAbove ? 1 : 2));
        parcel.writeByte((byte) (calibrate == null ? 0 : calibrate ? 1 : 2));
        parcel.writeString(ranges);
        parcel.writeString(defaultColors);
        if (hueTrend == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(hueTrend);
        }
        parcel.writeList(dilutions);
        if (monthsValid == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(monthsValid);
        }
        parcel.writeString(md610Id);
        parcel.writeString(sampleQuantity);
        parcel.writeTypedList(results);
        parcel.writeTypedList(instructions);
        parcel.writeString(selectInstruction);
        parcel.writeString(image);
        parcel.writeString(imageScale);
        if (numPatch == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(numPatch);
        }
        parcel.writeString(deviceId);
        parcel.writeString(responseFormat);
        if (decimalPlaces == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(decimalPlaces);
        }
        parcel.writeString(resultSuffix);
    }

    public Reagent getReagent(int i) {
        if (reagents != null && reagents.size() > i) {
            return reagents.get(i);
        } else {
            return new Reagent();
        }
    }

    public List<Result> getResults() {
        return results;
    }

    public String getSelectInstruction() {
        return selectInstruction;
    }

    public double getStripLength() {
        return length;
    }

    public List<Calibration> getCalibrations() {
        if (calibrations == null) {
            calibrations = new ArrayList<>();
        }
        return calibrations;
    }

    public void setCalibrations(List<Calibration> calibrations) {
        swatches.clear();

        Result result = results.get(0);

        List<Calibration> newCalibrations = new ArrayList<>();

        for (ColorItem colorItem : result.getColors()) {

            Calibration newCalibration = new Calibration(colorItem.getValue(), Color.TRANSPARENT);
            newCalibration.uid = uuid;

            for (int i = calibrations.size() - 1; i >= 0; i--) {
                Calibration calibration = calibrations.get(i);
                if (calibration.value == colorItem.getValue()) {
                    newCalibration.color = calibration.color;
                    newCalibration.date = calibration.date;
                    newCalibration.image = calibration.image;
                    newCalibration.croppedImage = calibration.croppedImage;
                    newCalibration.quality = calibration.quality;
                    newCalibration.resWidth = calibration.resWidth;
                    newCalibration.resHeight = calibration.resHeight;
                    newCalibration.zoom = calibration.zoom;
                    newCalibration.centerOffset = calibration.centerOffset;
                    colorItem.setRgbInt(calibration.color);
                }
            }

            Swatch swatch = new Swatch(newCalibration.value, newCalibration.color, Color.TRANSPARENT);
            swatches.add(swatch);

            String text = Double.toString(Math.abs(newCalibration.value));
            if (newCalibration.value % 1 != 0) {
                decimalPlaces = Math.max(text.length() - text.indexOf('.') - 1, decimalPlaces);
            }

            newCalibrations.add(newCalibration);
        }

        this.calibrations = newCalibrations;
        swatches = SwatchHelper.generateGradient(swatches);

        // if only one calibration exists then choose that as the default calibration
        double tempPivotCalibration = -1;
        for (Calibration calibration : calibrations) {
            if (calibration.color != Color.TRANSPARENT) {
                if (tempPivotCalibration == -1) {
                    tempPivotCalibration = calibration.value;
                } else {
                    tempPivotCalibration = -1;
                    break;
                }
            }
        }
        if (tempPivotCalibration != -1) {
            pivotCalibration = tempPivotCalibration;
        }
        setPivotCalibration(pivotCalibration);
    }

    public List<Calibration> getOneStepCalibrations() {
        if (oneStepCalibrations == null) {
            oneStepCalibrations = new ArrayList<>();
        }
        return oneStepCalibrations;
    }

    public int getDilution() {
        return dilution;
    }

    public void setDilution(int dilution) {
        this.dilution = Math.max(1, dilution);
    }

    /**
     * Connected device id.
     *
     * @return the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Use the response format to display the results in test id order.
     *
     * @return the Format
     */
    public String getResponseFormat() {
        return responseFormat;
    }

    public List<Swatch> getSwatches() {
        return swatches;
    }

    public void setSwatches(List<Swatch> swatches) {
        this.swatches = swatches;
    }

    public List<Swatch> getOneStepSwatches() {
        return oneStepSwatches;
    }

    public Integer getMonthsValid() {
        return monthsValid;
    }

    public Boolean getHasImage() {
        return hasImage;
    }

    public int getMaxDilution() {
        if (dilutions.size() > 0) {
            return dilutions.get(dilutions.size() - 1);
        } else {
            return 1;
        }
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public ResultDetail getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(ResultDetail resultDetail) {
        this.resultDetail = resultDetail;
    }

    public List<ColorItem> getReferenceColors() {
        return results.get(0).getReferenceColors();
    }

    public String getResultSuffix() {
        return resultSuffix;
    }

    public void setResultSuffix(String resultSuffix) {
        this.resultSuffix = resultSuffix;
    }

    public void clearCalibration() {
        for (Calibration calibration : calibrations) {
            calibration.color = Color.TRANSPARENT;
        }
        Result result = results.get(0);
        for (ColorItem colorItem : result.getColors()) {
            colorItem.setRgbInt(Color.TRANSPARENT);
        }
        oneStepCalibrations.clear();
        swatches.clear();
        oneStepCalibrations.clear();
    }

    public double getPivotCalibration() {
        return pivotCalibration;
    }

    public void setPivotCalibration(double pivotCalibration) {
        this.pivotCalibration = pivotCalibration;

        for (int i = 0; i < calibrations.size(); i++) {
            Calibration calibration = calibrations.get(i);
            if (calibration.value == pivotCalibration) {
                results.get(0).setPivotIndex(i);
                break;
            }
        }

        oneStepSwatches.clear();
        if (oneStepCalibrations != null) {
            oneStepCalibrations.clear();
        }

        oneStepCalibrations = SwatchHelper.getOneStepCalibrations(this.calibrations,
                pivotCalibration, getReferenceColors());

        for (Calibration calibration : this.oneStepCalibrations) {
            Swatch swatch = new Swatch(calibration.value, calibration.color, Color.TRANSPARENT);
            oneStepSwatches.add(swatch);
        }
        oneStepSwatches = SwatchHelper.generateGradient(oneStepSwatches);
    }
}