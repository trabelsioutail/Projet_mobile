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

# Keep Navigation Fragments - CRITICAL FOR NAVIGATION
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class com.edunova.mobile.presentation.ui.** { *; }
-keepnames class com.edunova.mobile.presentation.ui.admin.AdminProfileFragment
-keepnames class com.edunova.mobile.presentation.ui.admin.AdminMessagesFragment
-keepnames class com.edunova.mobile.presentation.ui.admin.ConversationFragment
-keepnames class com.edunova.mobile.presentation.ui.admin.CourseDetailFragment
-keepnames class com.edunova.mobile.presentation.ui.admin.QuizDetailFragment

# Keep Navigation Args classes
-keep class **.*Args { *; }
-keep class **.*Directions { *; }

# Keep Jetpack Navigation
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.navigation.Navigator

# Keep Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Room entities
-keep class com.edunova.mobile.data.local.entity.** { *; }
-keep class com.edunova.mobile.data.remote.dto.** { *; }
-keep class com.edunova.mobile.domain.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier