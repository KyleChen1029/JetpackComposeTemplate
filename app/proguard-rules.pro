# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/username/Library/Android/sdk/tools/proguard/proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection or introspection add keep rules here.
# Please define exactly what you need to keep!
# For example it's often looks like:
# -keep class com.example.myClass { *; }
# -keep interface com.example.myInterface { *; }
# -keep enum com.example.myEnum { *; }

# If you use Gson, you may need to add something like this:
# -keep class com.google.gson.stream.** { *; }

# If you use libraries that use reflection, like Retrofit or OkHttp, you may need to add keep rules for them.
# Example for Retrofit:
# -keep interface retrofit2.http.** { *; }
# -keep class retrofit2.** { *; }
# -keep class okhttp3.** { *; }
# -keep class okio.** { *; }

# If you are using Kotlin Coroutines, you might want to add this:
# -keepnames class kotlinx.coroutines.internal.** { *; }
