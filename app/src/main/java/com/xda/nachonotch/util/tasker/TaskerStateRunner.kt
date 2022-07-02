package com.xda.nachonotch.util.tasker

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionSatisfied
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionUnsatisfied
import com.xda.nachonotch.data.tasker.TaskerToggleData
import com.xda.nachonotch.util.prefManager

class TaskerStateRunner : TaskerPluginRunnerConditionNoOutput<TaskerToggleData, Unit>() {
    override val isEvent: Boolean
        get() = false

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<TaskerToggleData>,
        update: Unit?
    ): TaskerPluginResultCondition<Unit> {
        return if (context.prefManager.isEnabled == input.regular.enabled) TaskerPluginResultConditionSatisfied(context)
        else TaskerPluginResultConditionUnsatisfied()
    }
}