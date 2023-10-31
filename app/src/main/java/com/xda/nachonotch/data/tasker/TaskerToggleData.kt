package com.xda.nachonotch.data.tasker

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.xda.nachonotch.R

@TaskerInputRoot
class TaskerToggleData @JvmOverloads constructor(
    @field:TaskerInputField("enabled") var enabled: Boolean = false
)

@TaskerOutputObject
class TaskerToggleDataOutput(
    @get:TaskerOutputVariable("enabled", labelResIdName = "enabled", htmlLabelResIdName = "enabled") val enabled: Boolean
)