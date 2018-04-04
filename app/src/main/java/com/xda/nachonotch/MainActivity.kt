package com.xda.nachonotch

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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