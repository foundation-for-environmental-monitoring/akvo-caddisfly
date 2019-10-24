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

package org.akvo.caddisfly.sensor.titration;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.MathUtil;

import java.util.Locale;

import timber.log.Timber;

public class TitrationInputFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private EditText editResult1;
    private EditText editResult2;
    private OnSubmitResultListener mListener;

    /**
     * Get the instance.
     */
    public static TitrationInputFragment newInstance(TestInfo testInfo) {
        TitrationInputFragment fragment = new TitrationInputFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, testInfo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_titration_input, container, false);

        TextView textInput1 = view.findViewById(R.id.textInput1);
        TextView textInput2 = view.findViewById(R.id.textInput2);

        editResult1 = view.findViewById(R.id.editTitration1);
        editResult2 = view.findViewById(R.id.editTitration2);

        editResult1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editResult1.setError(null);
                editResult2.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editResult2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editResult1.setError(null);
                editResult2.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (getArguments() != null) {

            TestInfo testInfo = getArguments().getParcelable(ARG_PARAM1);

            if (testInfo != null) {

                if (testInfo.getResults().size() > 1) {

                    //todo: remove hardcoding of test names
                    textInput1.setText("Calcium & Magnesium");
                    textInput2.setText("Calcium Only");

                    editResult2.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (mListener != null) {

                                String n1String = editResult1.getText().toString();
                                String n2String = editResult2.getText().toString();

                                if (n1String.isEmpty()) {
                                    editResult1.setError("Enter result");
                                    editResult1.requestFocus();
                                } else {

                                    closeKeyboard(getActivity(), editResult2);
                                    closeKeyboard(getContext(), editResult1);

                                    float[] results = new float[testInfo.getResults().size()];

                                    float n1 = Float.parseFloat(n1String);

                                    if (n2String.isEmpty()) {
                                        editResult2.setError("Enter result");
                                        editResult2.requestFocus();
                                    } else {

                                        float n2 = Float.parseFloat(n2String);

                                        if (n2 > n1) {
                                            editResult1.setError("Invalid result");
                                            editResult2.setError("Invalid result");
                                            editResult1.requestFocus();
                                        } else {
                                            for (int i = 0; i < testInfo.getResults().size(); i++) {
                                                String formula = testInfo.getResults().get(i).getFormula();

                                                if (!formula.isEmpty()) {
                                                    results[i] = (float) MathUtil.eval(String.format(Locale.US, formula, n1, n2));
                                                }
                                            }
                                        }
                                    }

                                    mListener.onSubmitResult(results);
                                }

                            }
                            return true;
                        }
                        return false;
                    });
                } else {
                    textInput1.setVisibility(View.GONE);
                    textInput1.setVisibility(View.GONE);
                    editResult2.setVisibility(View.GONE);

                    editResult1.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (mListener != null) {

                                String n1String = editResult1.getText().toString();

                                if (n1String.isEmpty()) {
                                    editResult1.setError("Enter result");
                                    editResult1.requestFocus();
                                } else {

                                    closeKeyboard(getActivity(), editResult2);
                                    closeKeyboard(getContext(), editResult1);

                                    float[] results = new float[testInfo.getResults().size()];

                                    float n1 = Float.parseFloat(n1String);

                                    String formula = testInfo.getResults().get(0).getFormula();
                                    results[0] = (float) MathUtil.eval(String.format(Locale.US, formula, n1));

                                    mListener.onSubmitResult(results);
                                }

                            }
                            return true;
                        }
                        return false;
                    });
                }
            }
        }
        return view;
    }

    private void closeKeyboard(Context context, EditText input) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if (getActivity() != null) {
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void showSoftKeyboard(View view) {
        if (getActivity() != null && view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

//    private void hideSoftKeyboard(View view) {
//        if (getActivity() != null) {
//            InputMethodManager imm = (InputMethodManager)
//                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm != null) {
//                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
//            }
//        }
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        (new Handler()).postDelayed(() -> showSoftKeyboard(editResult1), 200);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubmitResultListener) {
            mListener = (OnSubmitResultListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnSubmitResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSubmitResultListener {
        void onSubmitResult(float[] results);
    }
}
