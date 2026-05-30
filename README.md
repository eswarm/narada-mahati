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

## Releasing

We use GitHub Actions to automate release builds for Android (APK), Windows (MSI), and Linux (DEB). Builds are triggered manually or when a new tag starting with `v` (e.g., `v1.0.0`) is pushed.

In order for the Android release builds to succeed in CI, you must configure the following **Repository Secrets** within your GitHub repository settings:

- **Narada Secrets:**
  - `NARADA_KEYSTORE_B64`: Base64-encoded version of your `narada.jks` file (e.g., `base64 -w0 narada.jks`).
  - `NARADA_STORE_PASSWORD`: Keystore password.
  - `NARADA_KEY_ALIAS`: Key alias.
  - `NARADA_KEY_PASSWORD`: Key password.

- **Mahati Secrets:**
  - `MAHATI_KEYSTORE_B64`: Base64-encoded version of your `mahati.jks` file.
  - `MAHATI_STORE_PASSWORD`: Keystore password.
  - `MAHATI_KEY_ALIAS`: Key alias.
  - `MAHATI_KEY_PASSWORD`: Key password.

If you don't provide these secrets, the CI workflow will attempt to rely on the keystore files present in the repo (or fail during signing if signatures don't match or the password isn't passed).

![narada_home](https://user-images.githubusercontent.com/381511/181612020-4e580c81-f876-4441-a81e-db9119daf65c.png)
