package org.akvo.caddisfly.sensor.striptest.qrdetector;

import org.akvo.caddisfly.common.ConstantKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FinderPatternInfoToJson {

    public static String toJson(FinderPatternInfo info) {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray topLeft = new JSONArray();
            topLeft.put(info.getTopLeft().getX());
            topLeft.put(info.getTopLeft().getY());
            jsonObject.put(ConstantKey.TOP_LEFT, topLeft);

            JSONArray topRight = new JSONArray();
            topRight.put(info.getTopRight().getX());
            topRight.put(info.getTopRight().getY());
            jsonObject.put(ConstantKey.TOP_RIGHT, topRight);

            JSONArray bottomLeft = new JSONArray();
            bottomLeft.put(info.getBottomLeft().getX());
            bottomLeft.put(info.getBottomLeft().getY());
            jsonObject.put(ConstantKey.BOTTOM_LEFT, bottomLeft);

            JSONArray bottomRight = new JSONArray();
            bottomRight.put(info.getBottomRight().getX());
            bottomRight.put(info.getBottomRight().getY());
            jsonObject.put(ConstantKey.BOTTOM_RIGHT, bottomRight);

            return jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


}
