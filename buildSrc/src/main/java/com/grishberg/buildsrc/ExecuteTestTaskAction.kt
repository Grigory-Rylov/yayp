package com.grishberg.buildsrc

import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Action for launching tests for module.
 */
interface ExecuteTestTaskAction {
    /**
     * Is called when found changed files in moduleProject or in dependencies.
     * @param visitorTask you should use visitorTask to make it depend on Your task.
     * @param changedModule module in which found changed.
     */
    fun executeTestTask(visitorTask: Task, changedModule: Project) = Unit

    object STUB : ExecuteTestTaskAction
}