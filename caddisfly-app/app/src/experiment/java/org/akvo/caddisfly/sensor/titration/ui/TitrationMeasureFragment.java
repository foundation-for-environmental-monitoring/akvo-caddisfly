package org.akvo.caddisfly.sensor.titration.ui;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.striptest.utils.MessageUtils;
import org.akvo.caddisfly.sensor.striptest.widget.PercentageMeterView;
import org.akvo.caddisfly.sensor.titration.TitrationConstants;

public class TitrationMeasureFragment extends Fragment {
    private static final long INITIAL_DELAY_MILLIS = 200;
    private static TitrationTestHandler titrationTestHandler;
    private ProgressBar progressBar;
    private TextView textBottom;
    private TextSwitcher textSwitcher;
    private PercentageMeterView exposureView;

    @NonNull
    public static TitrationMeasureFragment newInstance(TitrationTestHandler titrationTestHandler) {
        TitrationMeasureFragment.titrationTestHandler = titrationTestHandler;
        return new TitrationMeasureFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_titration_measure, container, false);
        exposureView = rootView.findViewById(R.id.quality_brightness);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setMax(TitrationConstants.COUNT_QUALITY_CHECK_LIMIT);
            progressBar.setProgress(0);
        }

        textSwitcher = view.findViewById(R.id.textSwitcher);

        if (textSwitcher != null) {
            textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                private TextView textView;
                private boolean isFirst = true;

                @Override
                public View makeView() {
                    if (isFirst) {
                        isFirst = false;
                        textView = view.findViewById(R.id.textMessage1);
                        ((ViewGroup) textView.getParent()).removeViewAt(0);
                    } else {
                        textView = view.findViewById(R.id.textMessage2);
                        ((ViewGroup) textView.getParent()).removeViewAt(0);
                    }
                    return textView;
                }
            });

            new Handler().postDelayed(() -> {
                if (isAdded()) {
                    textSwitcher.setText(getString(R.string.detecting_card));
                }
            }, INITIAL_DELAY_MILLIS);
        }

        titrationTestHandler.setTextSwitcher(textSwitcher);
    }

    @Override
    public void onResume() {
        super.onResume();

        // hand over to state machine
        MessageUtils.sendMessage(titrationTestHandler, TitrationTestHandler.START_PREVIEW_MESSAGE, 0);
    }

    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    void showQuality(int value) {

        if (exposureView != null) {
            exposureView.setPercentage((float) value);
        }
    }

    public void clearProgress() {
        if (progressBar != null) {
            progressBar.setProgress(0);
        }
    }

    public void setMeasureText() {
        textBottom.setText(R.string.measure_instruction);
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }
}
