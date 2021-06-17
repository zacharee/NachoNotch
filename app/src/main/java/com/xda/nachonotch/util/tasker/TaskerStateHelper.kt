package com.xda.nachonotch.util.tasker

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.xda.nachonotch.data.tasker.TaskerToggleData

class TaskerStateHelper(config: TaskerPluginConfig<TaskerToggleData>) : TaskerPluginConfigHelperNoOutput<TaskerToggleData, TaskerStateRunner>(config) {
    override val runnerClass: Class<TaskerStateRunner>
        get() = TaskerStateRunner::class.java
    override val inputClass: Class<TaskerToggleData>
        get() = TaskerToggleData::class.java
}