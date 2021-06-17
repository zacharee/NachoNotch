package com.xda.nachonotch.util.tasker

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoInput
import com.xda.nachonotch.data.tasker.TaskerToggleDataOutput

class TaskerEnabledHelper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperNoInput<TaskerToggleDataOutput, TaskerEnabledRunner>(config) {
    override val outputClass: Class<TaskerToggleDataOutput>
        get() = TaskerToggleDataOutput::class.java
    override val runnerClass: Class<TaskerEnabledRunner>
        get() = TaskerEnabledRunner::class.java
}