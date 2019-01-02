package org.akvo.caddisfly.sensor.titration.ui;

@SuppressWarnings("deprecation")
interface TitrationMeasureListener {

    void moveToInstructions(int testStage);

    void moveToStripMeasurement();

    void moveToResults();

    void playSound();

    void updateTimer(int value);

    void showTimer();

}
