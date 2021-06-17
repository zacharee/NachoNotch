package com.xda.nachonotch.activities.tasker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.xda.nachonotch.util.tasker.TaskerEnabledHelper

class TaskerEnabledConfigureActivity : AppCompatActivity(), TaskerPluginConfigNoInput {
    override val context: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TaskerEnabledHelper(this).finishForTasker()
    }
}