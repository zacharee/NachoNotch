package com.xda.nachonotch

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xda.nachonotch.activities.BaseActivity
import com.xda.nachonotch.components.CardItem
import com.xda.nachonotch.components.LinkItem
import com.xda.nachonotch.components.rememberNavigationLinks
import com.xda.nachonotch.components.rememberSocialLinks
import com.xda.nachonotch.util.PrefManager
import com.xda.nachonotch.util.enforceTerms
import com.xda.nachonotch.util.prefManager
import com.xda.nachonotch.util.rememberPreferenceState

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceTerms(true)) {
            finishAndRemoveTask()
        }
    }

    @SuppressLint("InflateParams")
    @Composable
    override fun Content() {
        val layoutDirection = LocalLayoutDirection.current

        val mainItems = rememberNavigationLinks()
        val socialItems = rememberSocialLinks()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.systemBars
                .add(WindowInsets.ime)
                .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .asPaddingValues().run {
                    PaddingValues(
                        start = 8.dp + calculateStartPadding(layoutDirection),
                        end = 8.dp + calculateEndPadding(layoutDirection),
                        top = 8.dp + calculateTopPadding(),
                        bottom = 8.dp + calculateBottomPadding(),
                    )
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "MainToggle") {
                var isEnabled by rememberPreferenceState(
                    key = PrefManager.SHOULD_RUN,
                    value = { prefManager.isEnabled },
                    onChanged = { prefManager.isEnabled = it },
                )

                LaunchedEffect(key1 = isEnabled) {
                    if (isEnabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (checkCallingOrSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(
                                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                    100
                                )
                            }
                        }
                    }
                }

                CardItem(
                    onClick = {
                        isEnabled = !isEnabled
                    },
                    icon = painterResource(R.drawable.ic_space_bar_black_24dp) to stringResource(R.string.hide_notch),
                    title = stringResource(R.string.hide_notch),
                    desc = stringResource(R.string.hide_notch_desc),
                    modifier = Modifier.fillMaxWidth(),
                    widget = {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                        )
                    },
                )
            }

            items(items = mainItems, key = { it.title }) {
                LinkItem(
                    option = it,
                )
            }

            items(items = socialItems, key = { it.title }) {
                LinkItem(
                    option = it,
                )
            }
        }
    }
}