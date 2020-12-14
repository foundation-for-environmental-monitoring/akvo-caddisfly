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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.ResultUtil;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;

import java.util.List;
import java.util.Locale;

public class DiagnosticActivity extends BaseActivity {

    public static final int CODE_DELETE = -5;
    public static final int CODE_ENTER = 13;
    private static final int CODE_COMMA = 44;
    private static final int CODE_MINUS = 45;
    private static final int CODE_DECIMAL = 46;
    private KeyboardView mKeyboardView;
    private EditText editColor;
    private final boolean decimalStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic);

        Keyboard mKeyboard = new Keyboard(this, R.xml.digit_keyboard);
        mKeyboardView = findViewById(R.id.keyboard_view);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setKeyboard(mKeyboard);

        editColor = findViewById(R.id.edit_color);

        editColor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(v);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        editColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomKeyboard(view);
            }
        });

        editColor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });

        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int i) {

            }

            @Override
            public void onRelease(int i) {

            }

            @Override
            public void onKey(int primaryCode, int[] ints) {
                View focusCurrent = DiagnosticActivity.this.getWindow().getCurrentFocus();
                if (focusCurrent == null || focusCurrent.getClass() != AppCompatEditText.class) {
                    return;
                }
                EditText edittext = (EditText) focusCurrent;
                Editable editable = edittext.getText();
                int start = edittext.getSelectionStart();

                // Handle key
                if (primaryCode == CODE_ENTER) {
                    String resultMessage = "";
                    String colorText = editColor.getText().toString();

                    StripTest stripTest = new StripTest();

                    StripTest.Brand brand = stripTest.getBrand(getIntent().getStringExtra(Constant.UUID));

                    List<StripTest.Brand.Patch> patches = brand.getPatchesSortedByPosition();

                    double[] labColors = new double[3];
                    if (colorText.length() > 0) {
                        colorText = colorText.trim().replace(" ", ",").replace(" ", ",");

                        String[] colors;
                        if (colorText.contains(",")) {
                            colors = colorText.split(",");
                        } else {
                            return;
                        }

                        if (colors.length > 2) {
                            labColors[0] = Double.parseDouble(colors[0]);
                            labColors[1] = Double.parseDouble(colors[1]);
                            labColors[2] = Double.parseDouble(colors[2]);
                        } else {
                            return;
                        }
                    }

                    for (int i = 0; i < patches.size(); i++) { // handle patch
                        try {
                            resultMessage += String.format(Locale.US, "%s: %.3f\n",
                                    patches.get(i).getDesc(),
                                    ResultUtil.calculateResultSingle(labColors,
                                            patches.get(i).getColors(), patches.get(i).getId(), true));
                        } catch (Exception ignored) {
                        }
                    }

                    AlertUtil.showAlert(DiagnosticActivity.this, R.string.result, resultMessage,
                            R.string.ok, null, null, null);

                } else if (primaryCode == CODE_DELETE) {
                    if (editable != null && start > 0) {
                        editable.delete(start - 1, start);
                    }
                } else if (primaryCode == CODE_MINUS) {

                    if (editable.toString().trim().length() > 0 && editable.toString().endsWith(",")) {
                        editable.insert(start, Character.toString((char) primaryCode));
                    }

                } else if (primaryCode == CODE_COMMA) {

                    int count = editable.toString().length() - editable.toString().replace(",", "").length();
                    if (count < 2 && (editable.toString().trim().length() > 0 && !editable.toString().endsWith(","))) {
                        editable.insert(start, Character.toString((char) primaryCode));
                    }

                } else if (primaryCode == CODE_DECIMAL) {

                    if (!decimalStarted && editable.toString().trim().length() != 0 && !editable.toString().endsWith(".")) {

                        for (int i = editable.toString().length() - 1; i >= 0; i--) {
                            if (editable.toString().charAt(i) == ',' || editable.toString().charAt(i) == ' ') {
                                break;
                            }

                            if (editable.toString().charAt(i) == '.') {
                                return;
                            }
                        }

                        editable.insert(start, Character.toString((char) primaryCode));
                    }


                } else {

                    editable.insert(start, Character.toString((char) primaryCode));
                }
            }

            @Override
            public void onText(CharSequence charSequence) {

            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        StripTest stripTest = new StripTest();
        setTitle(stripTest.getBrand(getIntent().getStringExtra(Constant.UUID)).getName());
    }


    public void showCustomKeyboard(View v) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (v != null) {
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
