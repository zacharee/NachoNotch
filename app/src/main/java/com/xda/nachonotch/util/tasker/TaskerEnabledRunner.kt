package com.xda.nachonotch.util.tasker

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.xda.nachonotch.data.tasker.TaskerToggleDataOutput
import com.xda.nachonotch.util.prefManager

class TaskerEnabledRunner : TaskerPluginRunnerActionNoInput<TaskerToggleDataOutput>() {
    override fun run(
        context: Context,
        input: TaskerInput<Unit>
    ): TaskerPluginResult<TaskerToggleDataOutput> {
        return TaskerPluginResultSucess(TaskerToggleDataOutput(context.prefManager.isEnabled))
    }
}