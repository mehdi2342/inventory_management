# Keep ML Kit barcode scanning model metadata
-keep class com.google.mlkit.vision.barcode.** { *; }
-keep class com.google.mlkit.common.** { *; }
-keep class com.google.android.gms.vision.** { *; }

# CameraX uses reflection in some code paths
-keep class androidx.camera.** { *; }

# Room entities are accessed via reflection for schema validation
-keep @androidx.room.Entity class ** { *; }

# GSON (transitively included) model data
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
