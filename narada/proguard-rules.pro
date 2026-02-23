# --- Generic Android Rules ---
-ignorewarnings
-keepattributes Signature,RuntimeVisibleAnnotations

# Keep the entry point of the application
-keep public class * extends android.app.Application
-keep public class * extends androidx.core.app.ComponentActivity

# --- Kotlinx Serialization ---
# Keep classes annotated with @Serializable, and their generated serializers.
-keepclasseswithmembers,allowshrinking,allowoptimization class * {
    @kotlinx.serialization.Serializable <methods>;
}
-keep class *$$serializer { *; }

# --- Moquette (MQTT Broker) Rules ---
# Moquette is a Java library not designed for Android and uses reflection.
# Keeping the entire package is the safest approach to prevent runtime crashes.
-keep class io.moquette.** { *; }

# --- Netty Rules ---
# Netty is a Moquette dependency and is heavily reliant on reflection.
-keep class io.netty.** { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.** { *; }

# --- SLF4J Custom Logger Bridge ---
# Keep our custom SLF4J implementation and the SPI interface so it can be discovered.
-keep public class in.eswarm.narada.log.NaradaLogger { *; }
-keep public class in.eswarm.narada.log.NaradaLoggerFactory { *; }
-keep class org.slf4j.impl.StaticLoggerBinder { *; }

# --- Jetpack DataStore ---
# Keep the generated code for DataStore.
-keepclassmembers class androidx.datastore.preferences.protobuf.** { *; }

# --- QR Code Generator ---
-keep public class com.github.alexzhirkevich.customqrgenerator.** { *; }

# --- Suppress Warnings for Moquette's Optional Dependencies ---
# These are dependencies that Moquette can use but are not present in an Android environment.
# It's safer to suppress these known warnings than to include the full libraries.
-dontwarn org.bouncycastle.**
-dontwarn sun.security.**
-dontwarn java.rmi.**
-dontwarn javax.jms.**
-dontwarn javax.management.**
-dontwarn com.conversantmedia.**
