package com.xda.nachonotch.util.tasker

import com.joaomgcd.taskerpluginlibrary.SimpleResult
import com.joaomgcd.taskerpluginlibrary.SimpleResultSuccess
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.xda.nachonotch.data.tasker.TaskerToggleData

class TaskerToggleHelper(config: TaskerPluginConfig<TaskerToggleData>) : TaskerPluginConfigHelperNoOutput<TaskerToggleData, TaskerToggleRunner>(config) {
    override val runnerClass: Class<TaskerToggleRunner>
        get() = TaskerToggleRunner::class.java
    override val inputClass: Class<TaskerToggleData>
        get() = TaskerToggleData::class.java

    override fun isInputValid(input: TaskerInput<TaskerToggleData>): SimpleResult {
        return SimpleResultSuccess()
    }
}