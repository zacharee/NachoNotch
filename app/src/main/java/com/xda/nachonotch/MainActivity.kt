package com.xda.nachonotch

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.xda.nachonotch.util.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Utils.enforceTerms(this)) finish()
        else {
            setContentView(R.layout.activity_main)

            val message = findViewById<TextView>(R.id.app_warning)
            message.movementMethod = LinkMovementMethod.getInstance()

            val disable = findViewById<Button>(R.id.disable)
            disable.setOnClickListener {
                applicationContext.packageManager.setComponentEnabledSetting(
                        ComponentName(applicationContext.packageName, "${applicationContext.packageName}.MainActivity"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                )
                Toast.makeText(this, resources.getText(R.string.activity_disabled), Toast.LENGTH_SHORT).show()

                finish()
            }
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
        }

        return super.onOptionsItemSelected(item)
    }
}