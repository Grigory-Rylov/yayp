package com.grishberg.buildsrc

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

open class ImpactAnalysisTestTask : DefaultTask() {
    var testTest: ExecuteTestTaskAction = ExecuteTestTaskAction.STUB

    private val extension: ImpactAnalysisExtension =
        project.extensions.findByType(ImpactAnalysisExtension::class.java) ?: ImpactAnalysisExtension()
    private val affectedModules = mutableSetOf<Project>()
    private var diffFiles: List<File> = listOf()
    private val projectsDiffFilesMap = mutableMapOf<Project, List<File>>()

    @TaskAction
    fun runTask() {
        projectsDiffFilesMap.clear()
        diffFiles = getAffectedFiles()

        project.rootProject.allprojects { currentModule ->
            if (currentModule == project.rootProject) {
                return@allprojects
            }
            val hasChangedDependency = checkChangedDependencies(currentModule)
            if (hasChangedDependency || moduleHasChangedFiles(diffFiles, currentModule)) {
                affectedModules.add(currentModule)
            }
        }

        for (module in affectedModules) {
            executeRelativeModuleTests(module)
        }
    }

    private fun moduleHasChangedFiles(diffFiles: List<File>, currentModule: Project): Boolean {
        val diff = findDiffForProject(diffFiles, currentModule.projectDir)
        return diff.isNotEmpty()
    }

    private fun checkChangedDependencies(module: Project): Boolean {
        var hasChanges = false
        module.configurations.forEach { conf ->
            conf.allDependencies.forEach { dep ->
                if (dep is DefaultProjectDependency) {
                    val currentModule = dep.dependencyProject
                    if (affectedModules.contains(currentModule)) {
                        return@forEach
                    }

                    if (moduleHasChangedFiles(diffFiles, currentModule) || checkChangedDependencies(currentModule)) {
                        affectedModules.add(currentModule)
                        hasChanges = true
                    }
                }
            }
        }
        return hasChanges
    }

    private fun executeRelativeModuleTests(currentModule: Project) {
        println("execute tests for project: $currentModule")
        testTest.executeTestTask(this, currentModule)
        currentModule.parent ?: executeRelativeModuleTests(currentModule.parent!!)
    }

    private fun findDiffForProject(diffFiles: List<File>, projectDir: File): List<File> {
        val path = projectDir.canonicalPath

        return diffFiles.filter {
            it.canonicalPath.contains(path)
        }
    }

    private fun getAffectedFiles(): List<File> {
        val output = runCommand(listOf("git", "diff", "--name-only", extension.targetBranch))
        return output.split("\n").map {
            File(it)
        }
    }

    private fun runCommand(args: List<String>): String {
        val workingDir: File = project.buildDir

        try {
            val proc = ProcessBuilder(args)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.MINUTES)
            return proc.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }
}