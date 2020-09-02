package org.akvo.caddisfly.preference

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import org.akvo.caddisfly.R

class DebuggingPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_debugging)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.rgb(255, 240, 220))
    }
}