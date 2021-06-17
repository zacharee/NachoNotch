package com.xda.nachonotch.util.tasker

import android.content.Context
import android.provider.Settings
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.xda.nachonotch.data.tasker.TaskerToggleData
import com.xda.nachonotch.util.*

class TaskerToggleRunner : TaskerPluginRunnerActionNoOutput<TaskerToggleData>() {
    override fun run(
        context: Context,
        input: TaskerInput<TaskerToggleData>
    ): TaskerPluginResult<Unit> {
        return if (context.enforceTerms()) {
            if (Settings.canDrawOverlays(context)) {
                if (input.regular.enabled) context.addOverlayAndEnable()
                else context.removeOverlayAndDisable()

                TaskerPluginResultSucess()
            } else {
                TaskerPluginResultError(100, "Unable to change Nacho Notch state because overlay permission isn't granted.")
            }
        } else {
            TaskerPluginResultError(101, "Unable to change Nacho Notch state because user hasn't agreed to terms.")
        }
    }
}