import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":webSqliteWorker"))

            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

// Webpack does not track node_modules; rebundle when the local sqlite worker package changes.
tasks.withType<KotlinWebpack>().configureEach {
    inputs
        .dir(rootProject.layout.projectDirectory.dir("webSqliteWorker/worker"))
        .withPropertyName("sqliteWasmWorkerSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set(libs.versions.ktlint.version.get())
    enableExperimentalRules.set(true)
}
