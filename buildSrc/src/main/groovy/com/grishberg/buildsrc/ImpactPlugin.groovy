package com.grishberg.buildsrc

import org.gradle.api.Plugin
import org.gradle.api.Project

class ImpactPlugin implements Plugin<Project> {
    public static final String EXTENSION_NAME = 'impactAnalysisConfig'
    public static final String TASK_NAME = "startActualTests"

    @Override
    void apply(Project project) {
        String targetBranch = "master"

        ImpactAnalysisExtension extension = project.extensions
                .create(EXTENSION_NAME, ImpactAnalysisExtension)

        if (project.rootProject.tasks.findByPath(TASK_NAME)) {
            println("already found")
            return
        }


        /**
         * Setup install apk and test apk
         */
        ImpactAnalysisTestTask testTask = project.rootProject.tasks.create(TASK_NAME, ImpactAnalysisTestTask.class)
    }
}