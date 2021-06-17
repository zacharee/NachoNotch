package com.xda.nachonotch.activities.tasker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.xda.nachonotch.data.tasker.TaskerToggleData
import com.xda.nachonotch.util.tasker.TaskerToggleHelper

class TaskerDisableConfigureActivity : AppCompatActivity(), TaskerPluginConfig<TaskerToggleData> {
    override val context: Context
        get() = this
    override val inputForTasker: TaskerInput<TaskerToggleData>
        get() = TaskerInput(TaskerToggleData(false))

    private val helper by lazy { TaskerToggleHelper(this) }

    override fun assignFromInput(input: TaskerInput<TaskerToggleData>) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helper.onCreate()

        helper.finishForTasker()
    }
}