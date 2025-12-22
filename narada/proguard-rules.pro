# --- Generic Android Rules ---
-ignorewarnings
-keepattributes Signature

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
# Moquette and its dependency Netty use reflection and have optional dependencies.

# Keep the main Server class and its public members
-keep public class io.moquette.broker.Server { *; }

# Netty uses reflection extensively. These are standard rules for Netty.
-keep public class io.netty.** { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueL1Pad { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueMidPad { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueL2Pad { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueColdProducerFields { *; }
-keepclassmembers class io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueL3Pad { *; }

# --- SLF4J Custom Logger Bridge ---
# Keep our custom SLF4J implementation so it can be discovered.
-keep public class org.slf4j.impl.StaticLoggerBinder { *; }
-keep public class in.eswarm.narada.log.NaradaLoggerFactory { *; }
-keep public class in.eswarm.narada.log.NaradaLogger { *; }

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

