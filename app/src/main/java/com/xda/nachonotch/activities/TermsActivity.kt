package com.xda.nachonotch.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.xda.nachonotch.R
import com.xda.nachonotch.util.Utils
import com.xda.nachonotch.util.prefManager
import kotlinx.android.synthetic.main.activity_terms.*
import ru.noties.markwon.Markwon
import java.io.BufferedReader
import java.io.InputStreamReader

class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        Markwon.setMarkdown(terms_text, getTermsText())
        terms_text.movementMethod = LinkMovementMethod.getInstance()

        terms_button.setOnClickListener {
            val termsIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/zacharee/NachoNotch/blob/master/app/src/main/assets/Terms.md"))
            startActivity(termsIntent)

            Handler(Looper.getMainLooper()).postDelayed({
                agree_box.isEnabled = true
            }, 2000)
        }

        terms_holder.setOnScrollChangeListener { v, _, _, _, _ ->
            if (terms_text.bottom - v.height - v.scrollY <= 0) {
                agree_box.isEnabled = true
            }
        }

        agree_box.setOnCheckedChangeListener { _, isChecked ->
            done.isClickable = isChecked
            done.isFocusable = isChecked
        }

        done.setOnClickListener {
            if (agree_box.isChecked) {
                prefManager.termsVersion = Utils.TERMS_VERSION

                finish()
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
