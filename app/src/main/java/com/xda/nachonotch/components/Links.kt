package com.xda.nachonotch.components

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xda.nachonotch.R
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.data.MainPageLink
import com.xda.nachonotch.util.launchEmail
import com.xda.nachonotch.util.launchUrl

@Composable
fun rememberNavigationLinks(): List<MainPageLink> {
    return remember {
        listOf(
            MainPageLink(
                icon = R.drawable.baseline_settings_24,
                title = R.string.settings,
                desc = R.string.settings_desc,
                onClick = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                },
            ),
            MainPageLink(
                icon = R.drawable.baseline_short_text_24,
                title = R.string.terms,
                desc = R.string.terms_desc,
                link = "https://github.com/zacharee/NachoNotch/blob/master/app/src/main/assets/Terms.md",
            ),
            MainPageLink(
                icon = R.drawable.ic_baseline_help_outline_24,
                title = R.string.main_screen_help,
                desc = R.string.main_screen_help_desc,
                link = "https://youtu.be/HhH5wK1NokY",
            ),
        )
    }
}

@Composable
fun rememberSocialLinks(): List<MainPageLink> {
    return remember {
        listOf(
            MainPageLink(
                icon = R.drawable.mastodon,
                title = R.string.main_screen_social_mastodon,
                desc = R.string.main_screen_social_mastodon_desc,
                link = "https://androiddev.social/@wander1236",
            ),
            MainPageLink(
                icon = R.drawable.ic_baseline_earth_24,
                title = R.string.main_screen_social_website,
                desc = R.string.main_screen_social_website_desc,
                link = "https://zwander.dev",
            ),
            MainPageLink(
                icon = R.drawable.ic_baseline_email_24,
                title = R.string.main_screen_social_email,
                desc = R.string.main_screen_social_email_desc,
                link = "zachary@zwander.dev",
                isEmail = true,
            ),
            MainPageLink(
                icon = R.drawable.ic_baseline_telegram_24,
                title = R.string.main_screen_social_telegram,
                desc = R.string.main_screen_social_telegram_desc,
                link = "https://bit.ly/ZachareeTG",
            ),
            MainPageLink(
                icon = R.drawable.ic_baseline_github_24,
                title = R.string.main_screen_social_github,
                desc = R.string.main_screen_social_github_desc,
                link = "https://github.com/zacharee/NachoNotch",
            ),
        )
    }
}

@Composable
fun LinkItem(option: MainPageLink) {
    val context = LocalContext.current

    CardItem(
        onClick = {
            if (option.onClick != null) {
                option.onClick.invoke(context)
            } else {
                if (option.isEmail) {
                    context.launchEmail(
                        option.link,
                        context.resources.getString(R.string.app_name),
                    )
                } else {
                    context.launchUrl(option.link)
                }
            }
        },
        icon = painterResource(option.icon) to stringResource(option.title),
        title = stringResource(option.title),
        desc = stringResource(option.desc),
        modifier = Modifier.fillMaxWidth(),
    )
}
