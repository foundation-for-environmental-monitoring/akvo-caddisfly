package org.akvo.caddisfly.entity;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(primaryKeys = {"uid", "value"})
public class Calibration implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<Calibration> CREATOR = new Creator<Calibration>() {
        @Override
        public Calibration createFromParcel(Parcel in) {
            return new Calibration(in);
        }

        @Override
        public Calibration[] newArray(int size) {
            return new Calibration[size];
        }
    };
    @NonNull
    public String uid = "";
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "value")
    public double value;
    @ColumnInfo(name = "color")
    public int color;
    @ColumnInfo(name = "quality")
    public int quality = 0;
    @ColumnInfo(name = "zoom")
    public int zoom = 0;
    @ColumnInfo(name = "resWidth")
    public int resWidth = 0;
    @ColumnInfo(name = "resHeight")
    public int resHeight = 0;
    @ColumnInfo(name = "centerOffset")
    public int centerOffset = 0;
    @ColumnInfo(name = "image")
    public String image;
    @ColumnInfo(name = "croppedImage")
    public String croppedImage;

    public Calibration() {
    }

    @Ignore
    public Calibration(double value, int color) {
        this.value = value;
        this.color = color;
    }

    public Calibration(Parcel in) {
        uid = Objects.requireNonNull(in.readString());
        date = in.readLong();
        value = in.readDouble();
        color = in.readInt();
        quality = in.readInt();
        zoom = in.readInt();
        resWidth = in.readInt();
        resHeight = in.readInt();
        centerOffset = in.readInt();
        image = in.readString();
        croppedImage = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeLong(date);
        dest.writeDouble(value);
        dest.writeInt(color);
        dest.writeInt(quality);
        dest.writeInt(zoom);
        dest.writeInt(resWidth);
        dest.writeInt(resHeight);
        dest.writeInt(centerOffset);
        dest.writeString(image);
        dest.writeString(croppedImage);
    }
}