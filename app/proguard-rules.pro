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

# Retrofit 基本规则
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Retrofit 接口和回调
-keep interface retrofit2.Call { *; }
-keep interface retrofit2.Callback { *; }
-keep class retrofit2.Response { *; }

# OkHttp（Retrofit 依赖 OkHttp）
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

# 你的实体类（例如 Account、RoomInfo 等）
-keep class com.diandeng.chaoxinglibrary.data.model.** { *; }