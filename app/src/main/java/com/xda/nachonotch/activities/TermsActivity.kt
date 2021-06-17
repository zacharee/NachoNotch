package com.xda.nachonotch.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.xda.nachonotch.databinding.ActivityTermsBinding
import com.xda.nachonotch.util.Utils
import com.xda.nachonotch.util.prefManager
import ru.noties.markwon.Markwon
import java.io.BufferedReader
import java.io.InputStreamReader

class TermsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTermsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Markwon.setMarkdown(binding.termsText, getTermsText())
        binding.termsText.movementMethod = LinkMovementMethod.getInstance()

        binding.termsButton.setOnClickListener {
            val termsIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/zacharee/NachoNotch/blob/master/app/src/main/assets/Terms.md"))
            startActivity(termsIntent)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.agreeBox.isEnabled = true
            }, 2000)
        }

        binding.termsHolder.setOnScrollChangeListener { v, _, _, _, _ ->
            if (binding.termsText.bottom - v.height - v.scrollY <= 0) {
                binding.agreeBox.isEnabled = true
            }
        }

        binding.agreeBox.setOnCheckedChangeListener { _, isChecked ->
            binding.done.isClickable = isChecked
            binding.done.isFocusable = isChecked
        }

        binding.done.setOnClickListener {
            if (binding.agreeBox.isChecked) {
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
