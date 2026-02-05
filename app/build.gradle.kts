plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "de.dbauer.expensetracker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val runsCIReleaseBuild = System.getenv("SIGNING_STORE_PASSWORD") != null

    defaultConfig {
        applicationId = "de.dbauer.expensetracker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 57
        versionName = "0.19.5"

        buildFeatures {
            buildConfig = true
        }
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
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName(if (runsCIReleaseBuild) "release" else "debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        sarifReport = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { variantOutput ->
            if (variantOutput is com.android.build.api.variant.impl.VariantOutputImpl) {
                variantOutput.outputFileName.set("${rootProject.name}_${variant.outputs.first().versionName.get()}.apk")
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.compose.components.resources)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)

    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    // Koin dependencies
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)

    compileOnly(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}

aboutLibraries {
    export {
        prettyPrint = true
        exportVariant = "release"
        outputFile = file("aboutlibraries.json")
    }
    collect.gitHubApiToken = System.getenv("ABOUT_LIBRARIES_TOKEN")
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
