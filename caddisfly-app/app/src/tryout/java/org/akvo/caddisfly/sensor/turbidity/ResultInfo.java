package org.akvo.caddisfly.sensor.turbidity;

import java.io.Serializable;

public class ResultInfo implements Serializable {
    public int testNumber;
    public String date;
    public String media;
    public String testType;
    public String volume;
    public String startImage;
    public String turbidImage;
    public String endImage;
    public String turbidTime;
    public String totalTime;
    public String result;
    public int version;
    public boolean resultBoolean;
}
