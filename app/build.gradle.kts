import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compilerOptions {
        // Common compiler options applied to all Kotlin source sets
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "App"
            isStatic = true
            // Required when using NativeSQLiteDriver
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.accompanist.permissions)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.biometric)
            implementation(libs.room.runtime.android)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.ui)

            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.compose.colorpicker)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.room.runtime)

            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.sqlite.bundled)
        }
        iosMain.dependencies {
            implementation(libs.sqlite.bundled)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "de.dbauer.expensetracker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    val runsCIReleaseBuild = System.getenv("SIGNING_STORE_PASSWORD") != null

    defaultConfig {
        applicationId = "de.dbauer.expensetracker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 55
        versionName = "0.19.3"
    }
    signingConfigs {
        create("release") {
            storeFile = file("keystore/android_keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            applicationVariants.all {
                outputs.forEach { output ->
                    if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                        output.outputFileName = "${rootProject.name}_$versionName.apk"
                    }
                }
            }
            signingConfig = signingConfigs.getByName(if (runsCIReleaseBuild) "release" else "debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
    lint {
        sarifReport = true
    }
}

compose {
    resources {
        publicResClass = true
    }
    desktop {
        application {
            mainClass = "de.dbauer.expensetracker.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "de.dbauer.expensetracker"
                packageVersion = "1.0.0"
            }
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set(libs.versions.ktlint.version.get())
    enableExperimentalRules.set(true)
    filter {
        exclude { element ->
            val path = element.file.path
            path.contains("\\generated\\") || path.contains("/generated/")
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)

    ktlintRuleset(libs.ktlint)

    debugImplementation(compose.uiTooling)
}

aboutLibraries {
    export {
        prettyPrint = true
        exportVariant = "release"
    }
    collect.gitHubApiToken = System.getenv("ABOUT_LIBRARIES_TOKEN")
}

// Make sure Coroutines can be debugged properly
tasks.withType<KotlinCompile> {
    if (project.gradle.startParameter.taskNames.any { it.contains("Debug") }) {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
            freeCompilerArgs.add("-Xdebug")
        }
    }
}
