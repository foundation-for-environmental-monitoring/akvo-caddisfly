package org.akvo.caddisfly.preference

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.preference.PreferenceFragmentCompat
import org.akvo.caddisfly.R
import org.akvo.caddisfly.util.ListViewUtil

class DebuggingPreferenceFragment : PreferenceFragmentCompat() {
    private var list: ListView? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_debugging)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(android.R.id.list)
        view.setBackgroundColor(Color.rgb(255, 240, 220))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0)
    }
}