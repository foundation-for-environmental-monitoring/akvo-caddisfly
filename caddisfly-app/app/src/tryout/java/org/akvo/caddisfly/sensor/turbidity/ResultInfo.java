package org.akvo.caddisfly.sensor.turbidity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ResultInfo implements Serializable {
    public final String id;
    private final SimpleDateFormat dateFormatWithYear
            = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US);
    private final SimpleDateFormat dateFormat
            = new SimpleDateFormat("dd MMM, HH:mm", Locale.US);
    private final SimpleDateFormat sourceDateFormat
            = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
    public String description;
    public String date;
    public String result;
    public int version;
    public String folder;
    private Date testDate;

    ResultInfo(String id) {
        this.id = id;
    }

    public void setDate(String value) {
        Calendar calendar = Calendar.getInstance();
        try {
            testDate = sourceDateFormat.parse(value);
            calendar.setTime(testDate);
            if (calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                date = dateFormat.format(calendar.getTime());
            } else {
                date = dateFormatWithYear.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    Date getTestDate() {
        return testDate;
    }
}
