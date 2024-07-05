package com.xda.nachonotch.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()

            MaterialTheme(
                colorScheme = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        if (isSystemInDarkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(
                            this
                        )
                    }

                    else -> {
                        if (isSystemInDarkTheme) darkColorScheme() else lightColorScheme()
                    }
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier,
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    @Composable
    abstract fun Content()
}
