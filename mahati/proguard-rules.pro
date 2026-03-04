# --- Generic Android Rules ---
-ignorewarnings
-keepattributes Signature,RuntimeVisibleAnnotations

# Keep the entry point of the application
-keep public class * extends android.app.Application
-keep public class * extends androidx.core.app.ComponentActivity

# --- Kotlinx Serialization ---
# Keep classes annotated with @Serializable and their generated serializers.
-keepclasseswithmembers,allowshrinking,allowoptimization class * {
    @kotlinx.serialization.Serializable <methods>;
}
-keep class *$$serializer { *; }

# --- HiveMQ MQTT Client ---
# This is a Java library not designed for Android and uses reflection.
# Keeping the entire package is the safest approach to prevent runtime crashes.
-keep class com.hivemq.client.** { *; }

# --- Netty (HiveMQ Dependency) ---
# Netty is heavily reliant on reflection.
-keep class io.netty.** { *; }

-keep class org.jctools.** { *; }
-dontwarn org.jctools.**

# --- SQLDelight ---
# Keep the generated adapters and runtime components.
-keep class app.cash.sqldelight.** { *; }
-keep class in.eswarm.mahati.db.** { *; } # Keep your generated DB classes

# --- Log4j2 & SLF4J ---
# Keep the runtime binding for Log4j2 and the custom SLF4J provider.
-keep class org.apache.logging.log4j.core.** { *; }
-keep public class in.eswarm.mahati.log.MahatiLogger { *; }
-keep public class in.eswarm.mahati.log.MahatiLoggerFactory { *; }
-keep public class in.eswarm.mahati.log.MahatiServiceProvider { *; }
-keep interface org.slf4j.spi.SLF4JServiceProvider { *; }

# --- Google ML Kit Barcode Scanning ---
# These rules are recommended by Google to prevent ML Kit models from being stripped.
-keep public class com.google.mlkit.vision.barcode.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }

# --- AndroidX Camera ---
# Keep CameraX implementation classes.
-keep class androidx.camera.camera2.internal.** { *; }
-keep class androidx.camera.core.impl.** { *; }
