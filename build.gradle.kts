// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint) apply false
    alias(libs.plugins.compose.compiler) apply false
}
true // Needed to make the Suppress annotation work for the plugins block
