# Keep model classes
-keep class com.zulfiqar.mileagemeter.models.** { *; }

# Keep Room database classes
-keep class com.zulfiqar.mileagemeter.data.** { *; }

# Keep MPAndroidChart classes
-keep class com.github.mikephil.charting.** { *; }

# Keep navigation component args
-keep class * extends androidx.navigation.Navigator
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile