package com.xda.nachonotch.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.FragmentContainerView
import androidx.preference.PreferenceFragmentCompat
import com.xda.nachonotch.R
import com.xda.nachonotch.components.TitleBar
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.enforceTerms
import com.xda.nachonotch.util.resourceNavBarHeight
import com.xda.nachonotch.util.resourceStatusBarHeight
import tk.zwander.seekbarpreference.SeekBarPreference

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceTerms()) finish()
    }

    @SuppressLint("InflateParams")
    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize()
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                TitleBar(title = title.toString())

                val bottomPadding = with(LocalDensity.current) { WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().toPx() }

                AndroidView(
                    factory = {
                        FragmentContainerView(it).apply {
                            id = R.id.content
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f),
                ) { view ->
                    val fragment = MainFragment()

                    fragment.arguments = bundleOf("bottomInset" to bottomPadding)

                    supportFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(view.id, fragment, null)
                        .commitNowAllowingStateLoss()
                }
            }
        }
    }

    class MainFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            setListeners()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            view.findViewById<View>(R.id.recycler_view)
                ?.updatePaddingRelative(
                    bottom = requireArguments().getFloat("bottomInset").toInt(),
                )
        }

        private fun setListeners() {
            val statusHeight = findPreference<SeekBarPreference>(PrefManager.STATUS_HEIGHT) as SeekBarPreference
            val navHeight = findPreference<SeekBarPreference>(PrefManager.NAV_HEIGHT) as SeekBarPreference

            preferenceManager.sharedPreferences?.apply {
                if (!contains(PrefManager.STATUS_HEIGHT)) statusHeight.progress = requireActivity().resourceStatusBarHeight
                if (!contains(PrefManager.NAV_HEIGHT)) navHeight.progress = requireActivity().resourceNavBarHeight
            }

            statusHeight.setDefaultValue(requireActivity().resourceStatusBarHeight)
            navHeight.setDefaultValue(requireActivity().resourceNavBarHeight)
        }
    }
}
