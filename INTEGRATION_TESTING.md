# Integration Testing Guide

This document describes how to run integration tests for Narada (MQTT Broker) and Mahati (MQTT Client) as independent applications.

## Overview

The integration tests verify that Narada and Mahati can communicate with each other via MQTT protocol when running as separate applications.

## Status Summary

| Test Type | Status | Notes |
|-----------|--------|-------|
| **Desktop Integration Tests** | ✅ **WORKING** | Full broker-client communication test, recommended for automated testing |
| **Cross-Device APK Installation** | ✅ **WORKING** | Successfully installs Narada and Mahati on separate devices |
| **Android UI Automator Tests** | ⚠️ **PERMISSION ISSUES** | Cross-app UI automation encounters security restrictions - see `ANDROID_CROSS_APP_TESTING_NOTES.md` |

**Recommendation:** Use Desktop integration tests for automated end-to-end validation. Android cross-app testing is available for manual testing workflows.

## Test Types

### Available Gradle Tasks

The project includes custom Gradle tasks for cross-platform integration testing:

| Task | Description |
|------|-------------|
| `buildIntegrationTestApks` | Build debug APKs for both Narada and Mahati |
| `installIntegrationTestApks` | Install both APKs on connected Android device/emulator |
| `runAndroidIntegrationTests` | Build, install, and run Android integration tests |
| `runDesktopIntegrationTests` | Run Desktop integration tests |
| `runAllIntegrationTests` | Run both Android and Desktop integration tests |

All tasks work identically on Windows, Mac, and Linux. Simply use:
- `.\gradlew <taskName>` on Windows
- `./gradlew <taskName>` on Mac/Linux

### 1. Android Integration Tests

**Test  Files:**
- `narada/src/androidInstrumentedTest/kotlin/in/eswarm/narada/AndroidServiceIntegrationTests.kt` - 3 platform-specific tests
- `narada/src/androidInstrumentedTest/kotlin/in/eswarm/narada/BrokerClientIntegrationTest.kt` - Cross-app UI tests (has permission issues)

**Platform-Specific Tests (AndroidServiceIntegrationTests.kt):**
1. `testBrokerServiceLifecycle()` - Android Service start/stop lifecycle
2. `testBasicPubSubOnAndroid()` - MQTT smoke test on Android runtime
3. `testServiceSurvivesProcessDeath()` - Foreground service survival

**What they test:**
- Android Service lifecycle (foreground service, Intents)
- Service state management via MQTTWrapper
- MQTT broker accessibility on Android runtime
- Service independence from app process

**Why only 3 Android tests vs 21 desktop tests?**
- MQTT protocol logic is in `commonMain` (shared code) - already tested by desktop
- These 3 tests focus on Android-specific platform behavior
- Desktop tests are faster and easier for CI/CD
- See `tests/ANDROID_TESTS_ADDED.md` for detailed rationale

**Prerequisites:**
- Android device or emulator connected and running
- Both apps can be built successfully
- **For cross-device testing:** Two Android devices/emulators connected (see device selection below)

**Device Selection:**

The integration tests support flexible device configuration:

**Option 1: Single Device (default)**
```bash
# With one device connected, it's used automatically
.\gradlew runAndroidIntegrationTests
```

**Option 2: Same Device for Both Apps**
```bash
# Find your device serial
adb devices

# Set device for both apps (Windows)
$env:ANDROID_SERIAL = "YOUR_DEVICE_SERIAL"
.\gradlew runAndroidIntegrationTests

# Mac/Linux
export ANDROID_SERIAL=YOUR_DEVICE_SERIAL
./gradlew runAndroidIntegrationTests
```

**Option 3: Different Devices (Cross-Device Testing)**
```bash
# Find your devices
adb devices
# Output:
# 28121JEGR14830  device    (Pixel 6a)
# RZCY307XXFH     device    (Galaxy S23)

# Windows PowerShell
$env:NARADA_DEVICE = "28121JEGR14830"  # Broker on Pixel 6a
$env:MAHATI_DEVICE = "RZCY307XXFH"     # Client on Galaxy S23
.\gradlew runAndroidIntegrationTests

# Mac/Linux
export NARADA_DEVICE=28121JEGR14830
export MAHATI_DEVICE=RZCY307XXFH
./gradlew runAndroidIntegrationTests
```

**Environment Variable Priority:**
- `NARADA_DEVICE` (highest priority for Narada)
- `MAHATI_DEVICE` (highest priority for Mahati)
- `ANDROID_SERIAL` (fallback for both if app-specific not set)
- Auto-detect (if single device and no env vars set)

Then run the tests as normal. If `ANDROID_SERIAL` is not set with a single device, that device will be used automatically.

**Running Android Integration Tests:**

Using Gradle tasks (recommended - works on Windows, Mac, Linux):
```bash
# Windows
.\gradlew runAndroidIntegrationTests

# Mac/Linux
./gradlew runAndroidIntegrationTests
```

This single command will:
1. Build debug APKs for both Narada and Mahati
2. Install them on connected device/emulator
3. Run the integration tests
4. Generate test reports

Manual step-by-step execution:
```bash
# Windows
.\gradlew buildIntegrationTestApks
.\gradlew installIntegrationTestApks
.\gradlew :narada:connectedAndroidTest

# Mac/Linux
./gradlew buildIntegrationTestApks
./gradlew installIntegrationTestApks
./gradlew :narada:connectedAndroidTest
```

**Test Reports:**
After running, view the HTML report at:
`narada/build/reports/androidTests/connected/index.html`

### 2. Desktop Integration Tests

The desktop integration tests provide comprehensive automated coverage of all passing CSV test cases from `tests/narada_mahati_testcases.csv`.

**Test Files:**
- `narada/src/desktopTest/kotlin/in/eswarm/narada/DesktopBrokerClientIntegrationTest.kt` - Original smoke test
- `narada/src/desktopTest/kotlin/in/eswarm/narada/NaradaBrokerTests.kt` - 9 Narada broker tests (TC-N1 through TC-N10)
- `narada/src/desktopTest/kotlin/in/eswarm/narada/MahatiClientTests.kt` - 11 Mahati client tests (TC-M2 through TC-M15)
- `narada/src/desktopTest/kotlin/in/eswarm/narada/BrokerTestBase.kt` - Shared test infrastructure
- `narada/src/desktopTest/kotlin/in/eswarm/narada/TestInterceptHandler.kt` - Broker event logging for verification

**CSV Test Case Coverage:**

| CSV Test ID | Test Method | Status |
|-------------|-------------|--------|
| TC-N1 | `NaradaBrokerTests.testBrokerStartStop()` | ✅ Automated |
| TC-N2 | `NaradaBrokerTests.testBrokerStartStop()` | ✅ Automated |
| TC-N3 | `NaradaBrokerTests.testClientConnectsToBroker()` | ✅ Automated |
| TC-N4 | `NaradaBrokerTests.testPublishSubscribeLoopbackWithLogs()` | ✅ Automated |
| TC-N5 | `NaradaBrokerTests.testPublishSubscribeLoopbackWithLogs()` | ✅ Automated |
| TC-N6 | `NaradaBrokerTests.testMultipleClientsStability()` | ✅ Automated |
| TC-N7 | `NaradaBrokerTests.testBrokerRestartWithClients()` | ✅ Automated |
| TC-N9 | `NaradaBrokerTests.testAuthenticationFailure()` | ✅ Automated |
| TC-N10 | `NaradaBrokerTests.testWebSocketConnection()` | ✅ Automated |
| TC-M2 | `MahatiClientTests.testClientConnectionStates()` | ✅ Automated |
| TC-M3 | `MahatiClientTests.testSubscribeAndChatForTopic()` | ✅ Automated |
| TC-M4 | `MahatiClientTests.testSubscribeAndChatForTopic()` | ✅ Automated |
| TC-M5 | `MahatiClientTests.testPublishMessageFromChat()` | ✅ Automated |
| TC-M6 | `MahatiClientTests.testMessageRouting()` | ✅ Automated |
| TC-M7 | `MahatiClientTests.testNotificationDeepLinkSimulation()` | ✅ Automated |
| TC-M8 | `MahatiClientTests.testWildcardPlusFilter()` | ✅ Automated |
| TC-M9 | `MahatiClientTests.testWildcardHashFilter()` | ✅ Automated |
| TC-M13 | `MahatiClientTests.testOfflinePublishFailure()` | ✅ Automated |
| TC-M14 | `MahatiClientTests.testReconnectAutoSubscribe()` | ✅ Automated |
| TC-M15 | `MahatiClientTests.testUnsubscribeFlow()` | ✅ Automated |

**What they test:**
- Broker lifecycle (start/stop/restart)
- Client connections and disconnections
- MQTT publish/subscribe with multiple clients
- Topic-based message routing
- MQTT wildcard subscriptions (`+` and `#`)
- Authentication with valid/invalid credentials
- WebSocket connections
- Broker stability under load
- Offline publish handling
- Subscription persistence after reconnect
- Unsubscribe behavior
- Broker event logging and monitoring

**Prerequisites:**
- Desktop builds compile successfully
- Java/JVM properly configured

**Running Desktop Integration Tests:**

Using Gradle tasks (works on Windows, Mac, Linux):
```bash
# Run all desktop integration tests (21 tests total)
# Windows
.\gradlew runDesktopIntegrationTests

# Mac/Linux
./gradlew runDesktopIntegrationTests
```

**Running specific test classes:**
```bash
# Run only broker tests (9 tests)
.\gradlew :narada:desktopTest --tests "in.eswarm.narada.NaradaBrokerTests"

# Run only client tests (11 tests)
.\gradlew :narada:desktopTest --tests "in.eswarm.narada.MahatiClientTests"

# Run a specific test method
.\gradlew :narada:desktopTest --tests "in.eswarm.narada.MahatiClientTests.testWildcardPlusFilter"
```

Or run all integration tests (Android + Desktop):
```bash
# Windows
.\gradlew runAllIntegrationTests

# Mac/Linux
./gradlew runAllIntegrationTests
```

**Note:** If tests fail with Java-related errors, ensure JAVA_HOME is set:
```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Mac/Linux
export JAVA_HOME="/path/to/jdk"
```

**Test Reports:**
After running, view the HTML report at:
`narada/build/reports/tests/desktopTest/index.html`

## Troubleshooting

### Android Tests

**Issue: Want to test broker and client on different devices**
- Solution: Use `NARADA_DEVICE` and `MAHATI_DEVICE`:
  ```bash
  # Find both device serials
  adb devices
  
  # Set different device for each app
  $env:NARADA_DEVICE = "DEVICE_1"  # Broker device
  $env:MAHATI_DEVICE = "DEVICE_2"  # Client device
  .\gradlew runAndroidIntegrationTests
  ```

**Issue: "more than one device/emulator" error**
- You have multiple devices/emulators connected
- Solution: Set device selection environment variables:
  ```bash
  # Option 1: Same device for both apps
  $env:ANDROID_SERIAL = "YOUR_DEVICE_SERIAL"
  
  # Option 2: Different device for each app
  $env:NARADA_DEVICE = "DEVICE_1"
  $env:MAHATI_DEVICE = "DEVICE_2"
  ```

**Issue: Apps installing on wrong device**
- Check environment variable priority:
  - `NARADA_DEVICE` takes precedence over `ANDROID_SERIAL` for Narada
  - `MAHATI_DEVICE` takes precedence over `ANDROID_SERIAL` for Mahati
- Verify which variables are set:
  ```bash
  # Windows
  Get-ChildItem Env: | Select-String "DEVICE|SERIAL"
  
  # Mac/Linux
  env | grep -E "DEVICE|SERIAL"
  ```

**Issue: Apps not installing**
- Ensure Android device/emulator is connected: `adb devices`
- Check that APKs built successfully in `build/outputs/apk/`
- Try uninstalling manually: `adb uninstall in.eswarm.narada && adb uninstall in.eswarm.mahati`

**Issue: Tests timing out**
- Increase timeout values in `BrokerClientIntegrationTest.kt`
- Ensure device has sufficient resources
- Try running on a faster emulator or physical device

**Issue: Cannot find UI elements**
- UI automation depends on text/descriptions in the apps
- Verify the apps have appropriate content descriptions
- Update selectors in the test to match current UI

### Desktop Tests

**Issue: Broker not starting**
- Check that port 1883 is not already in use
- Verify JAVA_HOME is set correctly
- Check console output for broker startup errors
- Try: `netstat -ano | findstr :1883` to see if port is in use

**Issue: Connection refused**
- Ensure broker has enough time to start (increase `brokerStartupWaitMs` in test)
- Check firewall settings
- Review standard error output in test report

**Issue: Message not received**
- Check MQTT client connectivity
- Verify topic names match exactly
- Increase test timeout value
- Review any RxJava exceptions in stderr output

## Test Architecture

### Android (UI Automator Approach)
- Uses `UiDevice` to control both apps from outside
- Black-box testing - no shared code
- Mimics real user interaction
- Tests actual APK integration

### Desktop (In-Process Broker Approach)
- Starts Moquette broker directly in test process
- Uses MQTT client library for programmatic verification
- Headless testing (no UI interaction needed)
- Faster execution and easier to debug
- Tests core broker functionality with real MQTT protocol

## Dependencies Added

### narada/build.gradle.kts

**androidInstrumentedTest:**
- `androidx.test.ext:junit` - Android test framework
- `androidx.test:core` - Test utilities
- `androidx.test:runner` - Test runner
- `androidx.test.uiautomator:uiautomator` - Cross-app UI automation

**desktopTest:**
- `kotlin-test-junit` - JUnit for Kotlin
- `hivemq-mqtt-client` - MQTT client for testing
- `kotlinx-coroutines-core` - Coroutines support

## CI/CD Integration

To run these tests in CI/CD:

**GitHub Actions example:**
```yaml
- name: Run Android Integration Tests
  run: |
    # Start emulator
    $ANDROID_SDK_ROOT/emulator/emulator -avd test_device -no-window &
    adb wait-for-device
    
    # Run tests
    ./gradlew runAndroidIntegrationTests

- name: Run Desktop Integration Tests  
  run: ./gradlew runDesktopIntegrationTests
  
- name: Run All Integration Tests
  run: ./gradlew runAllIntegrationTests
```

## Future Enhancements

Potential improvements:
- Add more comprehensive test scenarios
- Test WebSocket connections
- Test SSL/TLS connections
- Test authentication scenarios
- Test reconnection logic
- Add performance benchmarks
- Test with multiple concurrent clients








