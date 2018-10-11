package com.xda.nachonotch.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.xda.nachonotch.R
import kotlinx.android.synthetic.main.activity_terms.*

class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        terms_button.setOnClickListener {
            val termsIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/zacharee/NachoNotch/blob/master/README.md"))
            startActivity(termsIntent)

            mainThreadHandler.postDelayed({
                agree_box.isEnabled = true
            }, 2000)
        }

        agree_box.setOnCheckedChangeListener { _, isChecked ->
            done.isClickable = isChecked
            done.isFocusable = isChecked
        }

        done.setOnClickListener {
            if (agree_box.isChecked) {
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putBoolean("agreed_terms", true)
                        .apply()

                finish()
            }
        }
    }
}
