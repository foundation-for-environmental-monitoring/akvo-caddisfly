package org.akvo.caddisfly.diagnostic;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.sensor.chamber.BaseRunTest;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.ArrayList;
import java.util.Locale;

public class DiagnosticResultDialog extends DialogFragment {

    private ArrayList<ResultDetail> resultDetails;
    private ResultDetail result;
    private OnDismissed mListener;

    /**
     * Instance of dialog.
     *
     * @param testFailed          did test fail
     * @param retryCount          count of retries
     * @param resultDetail        the result
     * @param resultDetails       the result details
     * @param isCalibration       is this in calibration mode
     * @return the dialog
     */
    public static DialogFragment newInstance(boolean testFailed, int retryCount, ResultDetail resultDetail,
                                             ArrayList<ResultDetail> resultDetails,
                                             boolean isCalibration) {
        DiagnosticResultDialog fragment = new DiagnosticResultDialog();
        Bundle args = new Bundle();
        fragment.result = resultDetail;
        fragment.resultDetails = resultDetails;
        args.putBoolean("testFailed", testFailed);
        args.putInt("retryCount", retryCount);
        args.putBoolean("isCalibration", isCalibration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.dialog_diagnostic_result, container, false);

        ListView listResults = view.findViewById(R.id.listResults);
        listResults.setAdapter(new ResultListAdapter());

        boolean testFailed = getArguments().getBoolean("testFailed");
        int retryCount = getArguments().getInt("retryCount");
        boolean isCalibration = getArguments().getBoolean("isCalibration");

        Button buttonColorExtract = view.findViewById(R.id.buttonColorExtract);
        Button buttonSwatchColor = view.findViewById(R.id.buttonSwatchColor);
        TextView textExtractedRgb = view.findViewById(R.id.textExtractedRgb);
        TextView textSwatchRgb = view.findViewById(R.id.textSwatchRgb);
//        TextView textDimension = view.findViewById(R.id.textDimension);
        TextView textDistance = view.findViewById(R.id.textDistance);
        TextView textQuality = view.findViewById(R.id.textQuality);

        TextView textResult = view.findViewById(R.id.textResult);

        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonRetry = view.findViewById(R.id.buttonRetry);

        buttonColorExtract.setBackgroundColor(result.getColor());
        buttonSwatchColor.setBackgroundColor(result.getMatchedColor());

        textExtractedRgb.setText(String.format("%s", ColorUtil.getColorRgbString(result.getColor())));
        textSwatchRgb.setText(String.format("%s", ColorUtil.getColorRgbString(result.getMatchedColor())));

        textDistance.setText(String.format(Locale.getDefault(), "D: %.2f", result.getDistance()));
        textQuality.setText(String.format(Locale.getDefault(), "Q: %d%%", result.getQuality()));

        if (testFailed) {
            getDialog().setTitle(R.string.no_result);
        } else {
            if (isCalibration) {
                TableLayout tableDetails = view.findViewById(R.id.tableDetails);
                tableDetails.setVisibility(View.GONE);
                if (result.getColor() == Color.TRANSPARENT) {
                    getDialog().setTitle(R.string.error);
                } else {
                    getDialog().setTitle(String.format("%s: %s", getString(R.string.result),
                            ColorUtil.getColorRgbString(result.getColor())));
                }
            } else {
                getDialog().setTitle(R.string.result);
                textResult.setText(String.format(Locale.getDefault(),
                        "%.2f", result.getResult()));
            }
        }

        Button buttonOk = view.findViewById(R.id.buttonOk);

        buttonCancel.setVisibility(View.GONE);
        if (testFailed && retryCount < 1) {
            buttonRetry.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.VISIBLE);
            buttonOk.setVisibility(View.GONE);
            buttonRetry.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onDismissed(true);
                }
                dismiss();
            });
        } else {
            buttonRetry.setVisibility(View.GONE);
            buttonCancel.setVisibility(View.GONE);
            buttonOk.setVisibility(View.VISIBLE);
        }

        buttonOk.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onDismissed(false);
            }
            dismiss();
        });

        buttonCancel.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDismissed(false);
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseRunTest.OnResultListener) {
            mListener = (OnDismissed) context;
        }
    }

    public interface OnDismissed {
        void onDismissed(boolean retry);
    }

    private class ResultListAdapter extends BaseAdapter {

        public int getCount() {
            return resultDetails.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            @SuppressLint("ViewHolder")
            View rowView = inflater.inflate(R.layout.row_info, parent, false);

            if (rowView != null) {
                ImageView imageView = rowView.findViewById(R.id.imageView);
                TextView textRgb = rowView.findViewById(R.id.textRgb);
                TextView textSwatch = rowView.findViewById(R.id.textSwatch);

                ResultDetail result = resultDetails.get(position);

                imageView.setImageBitmap(result.getCroppedBitmap());
                int color = result.getColor();

                textSwatch.setBackgroundColor(color);

                //display rgb value
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                textRgb.setText(String.format(Locale.getDefault(), "%d  %d  %d", r, g, b));
            }
            return rowView;
        }
    }
}
