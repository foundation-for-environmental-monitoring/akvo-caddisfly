package org.akvo.caddisfly.preference;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.preference.PreferenceFragmentCompat;

import org.akvo.caddisfly.R;
import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class DebuggingPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_debugging);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.rgb(255, 240, 220));
    }
}
