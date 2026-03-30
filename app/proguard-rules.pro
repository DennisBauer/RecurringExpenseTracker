# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn kotlinx.serialization.internal.AbstractPolymorphicSerializer

-keep class * extends androidx.room.RoomDatabase { <init>(); }

# Ensure all Previews have been stripped https://issuetracker.google.com/issues/157891235#comment6
-checkdiscard class * { @androidx.compose.ui.tooling.preview.Preview <methods>; }
-keepclassmembers,allowshrinking class * { @androidx.compose.ui.tooling.preview.Preview <methods>; }

# WorkManager InputMerger subclasses are instantiated via reflection
-keep class * extends androidx.work.InputMerger { <init>(); }
