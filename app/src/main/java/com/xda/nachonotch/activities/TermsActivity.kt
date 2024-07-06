package com.xda.nachonotch.activities

import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.xda.nachonotch.MainActivity
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils
import com.xda.nachonotch.util.prefManager
import ru.noties.markwon.Markwon
import java.io.BufferedReader
import java.io.InputStreamReader

class TermsActivity : BaseActivity() {
    companion object {
        const val FROM_MAIN_ACTIVITY = "from_main_activity"
    }

    private val fromMainActivity by lazy { intent.getBooleanExtra(FROM_MAIN_ACTIVITY, false) }

    @Composable
    override fun Content() {
        var agreedToTerms by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .systemBarsPadding()
                .imePadding(),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.agree_to_terms),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                AndroidView(
                    factory = { TextView(it) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Markwon.setMarkdown(it, getTermsText())
                    it.movementMethod = LinkMovementMethod.getInstance()
                }
            }

            OutlinedButton(
                onClick = {
                    val termsIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/zacharee/NachoNotch/blob/master/app/src/main/assets/Terms.md"))
                    startActivity(termsIntent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(text = stringResource(R.string.view_terms_online))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                    )

                    Text(text = stringResource(id = R.string.agree))
                }

                IconButton(
                    onClick = {
                        prefManager.termsVersion = Utils.TERMS_VERSION

                        finish()

                        if (fromMainActivity) {
                            startActivity(Intent(this@TermsActivity, MainActivity::class.java))
                        }
                    },
                    enabled = agreedToTerms,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_black_24dp),
                        contentDescription = stringResource(R.string.done),
                    )
                }
            }
        }
    }

    private fun getTermsText(): String {
        val builder = StringBuilder()
        val reader = BufferedReader(InputStreamReader(assets.open("Terms.md"), "UTF-8"))
        reader.forEachLine { builder.append(it + "\n") }

        return builder.toString()
    }
}
