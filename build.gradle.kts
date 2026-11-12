import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// Yarn copies the local `file:` package webSqliteWorker/worker into node_modules once and never
// refreshes it, so edits to the worker would be ignored. Track its sources and force a fresh copy.
tasks.withType<KotlinNpmInstallTask>().configureEach {
    inputs
        .dir(layout.projectDirectory.dir("webSqliteWorker/worker"))
        .withPropertyName("sqliteWasmWorkerSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    doFirst {
        val nodeModules = (this as KotlinNpmInstallTask).nodeModules.get().asFile
        nodeModules.resolve("sqlite-wasm-worker").deleteRecursively()
        nodeModules.resolve(".yarn-integrity").delete()
    }
}
