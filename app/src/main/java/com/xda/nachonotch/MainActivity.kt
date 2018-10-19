package com.xda.nachonotch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.xda.nachonotch.activities.SettingsActivity
import com.xda.nachonotch.util.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Utils.enforceTerms(this)) finish()
        else {
            setContentView(R.layout.activity_main)

            val message = findViewById<TextView>(R.id.app_warning)
            message.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_terms -> {
                val termsIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/zacharee/NachoNotch/blob/master/app/src/main/assets/Terms.md"))
                startActivity(termsIntent)
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }
}