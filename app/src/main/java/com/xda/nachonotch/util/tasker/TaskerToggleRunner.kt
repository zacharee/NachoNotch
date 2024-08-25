package com.xda.nachonotch.util.tasker

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.xda.nachonotch.data.tasker.TaskerToggleData
import com.xda.nachonotch.util.updateServiceState

class TaskerToggleRunner : TaskerPluginRunnerActionNoOutput<TaskerToggleData>() {
    override fun run(
        context: Context,
        input: TaskerInput<TaskerToggleData>
    ): TaskerPluginResult<Unit> {
        val (success, message) = context.updateServiceState(toggle = true)

        return if (success) {
            TaskerPluginResultSucess()
        } else {
            TaskerPluginResultError(100, message ?: "")
        }
    }
}