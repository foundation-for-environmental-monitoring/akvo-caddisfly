package org.akvo.caddisfly.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ResultUtils {
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###", symbols);

    // creates formatted string from float value, for display of colour charts
    // here, we use points as decimal separator always, as this is also used
    // to format numbers that are returned by json.
    public static String createValueString(float value) {
        return decimalFormat.format(value);
    }
}