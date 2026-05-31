# --- Generic Android Rules ---
-ignorewarnings
-keepattributes Signature,RuntimeVisibleAnnotations

# Keep the entry point of the application (Android only)
-keep public class * extends android.app.Application
-keep public class * extends androidx.core.app.ComponentActivity

# Suppress warnings for Android classes not present in desktop builds
-dontwarn android.**
-dontwarn androidx.**

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
-dontwarn aQute.bnd.**
-dontwarn com.aayushatharva.brotli4j.**
-dontwarn com.codahale.metrics.**
-dontwarn com.conversantmedia.**
-dontwarn com.fasterxml.jackson.**
-dontwarn com.github.luben.zstd.**
-dontwarn com.google.protobuf.**
-dontwarn com.jcraft.jzlib.**
-dontwarn com.lmax.disruptor.**
-dontwarn com.ning.compress.**
-dontwarn com.oracle.svm.**
-dontwarn com.zaxxer.hikari.**
-dontwarn io.prometheus.**
-dontwarn java.lang.management.**
-dontwarn java.rmi.**
-dontwarn javassist.**
-dontwarn javax.activation.**
-dontwarn javax.jms.**
-dontwarn javax.lang.model.**
-dontwarn javax.mail.**
-dontwarn javax.management.**
-dontwarn javax.naming.**
-dontwarn javax.script.**
-dontwarn javax.tools.**
-dontwarn javax.xml.stream.**
-dontwarn lzma.sdk.**
-dontwarn net.jpountz.**
-dontwarn org.apache.commons.compress.**
-dontwarn org.apache.commons.csv.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.kafka.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.bouncycastle.**
-dontwarn org.codehaus.stax2.**
-dontwarn org.conscrypt.**
-dontwarn org.eclipse.jetty.alpn.**
-dontwarn org.eclipse.jetty.npn.**
-dontwarn org.graalvm.**
-dontwarn org.hibernate.**
-dontwarn org.jboss.marshalling.**
-dontwarn org.osgi.**
-dontwarn org.zeromq.**
-dontwarn reactor.blockhound.**
-dontwarn sun.security.**
