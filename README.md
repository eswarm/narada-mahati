# Narada :: MQTT Broker on Android.

Narada, MQTT Broker which runs on Android.

Narada is a thin UI wrapper over Moquette, which allows you to run a MQTT broker on your Android
phone. 

App can be found here, https://play.google.com/store/apps/details?id=in.eswarm.narada

## Integration Testing

This repository includes cross-app integration tests for Narada (MQTT Broker) and Mahati (MQTT Client).

**Basic Usage:**

```bash
# Windows
.\gradlew runAndroidIntegrationTests
.\gradlew runDesktopIntegrationTests

# Mac/Linux
./gradlew runAndroidIntegrationTests
./gradlew runDesktopIntegrationTests
```

**Cross-Device Testing (Broker and Client on Different Devices):**

```bash
# List your devices
adb devices

# Windows - Run broker on one device, client on another
$env:NARADA_DEVICE = "DEVICE_SERIAL_1"  # Broker device
$env:MAHATI_DEVICE = "DEVICE_SERIAL_2"  # Client device
.\gradlew runAndroidIntegrationTests

# Mac/Linux
export NARADA_DEVICE=DEVICE_SERIAL_1
export MAHATI_DEVICE=DEVICE_SERIAL_2
./gradlew runAndroidIntegrationTests
```

**Same Device Testing:**

```bash
# Use ANDROID_SERIAL for both apps on same device
$env:ANDROID_SERIAL = "YOUR_DEVICE_SERIAL"
.\gradlew runAndroidIntegrationTests
```

For more details, see [INTEGRATION_TESTING.md](INTEGRATION_TESTING.md).


![narada_home](https://user-images.githubusercontent.com/381511/181612020-4e580c81-f876-4441-a81e-db9119daf65c.png)
