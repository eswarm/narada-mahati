# Android Cross-App Testing Notes

## Current Status

The Android cross-app integration test (UIAutomator-based) encounters permission issues when run through Gradle's `connectedDebugAndroidTest` task.

### Error
```
java.lang.SecurityException: Calling from not trusted UID!
at android.app.UiAutomationConnection.throwIfCalledByNotTrustedUidLocked
```

### Root Cause

UIAutomator requires special permissions to control other apps that are only available when:
1. Running with shell-level permissions
2. The app is granted accessibility service permissions

Standard instrumentation tests (via `connectedDebugAndroidTest`) run in the app's sandbox and cannot control other applications.

## Workarounds

### Option 1: Manual Shell-Based Testing
Run the test with shell permissions:

```bash
# Build and install test APK
.\gradlew :narada:assembleDebugAndroidTest

# Install APKs
adb install -r narada\build\outputs\apk\debug\narada-debug.apk
adb install -r mahati\build\outputs\apk\debug\mahati-debug.apk
adb install -r narada\build\outputs\apk\androidTest\debug\narada-debug-androidTest.apk

# Run test with shell permissions
adb shell am instrument -w -r -e debug false -e class in.eswarm.narada.BrokerClientIntegrationTest \
  in.eswarm.narada.test/androidx.test.runner.AndroidJUnitRunner
```

### Option 2: Single-App Integration Tests
Instead of cross-app UI testing, create:
- **Narada unit/integration tests**: Test broker functionality programmatically
- **Mahati unit/integration tests**: Test client functionality programmatically  
- **Desktop cross-app tests**: Use ProcessBuilder approach (already working ✓)

### Option 3: Manual Testing Checklist
Since automated cross-app testing has permission limitations, maintain a manual test checklist:

1. Install both APKs on device(s)
2. Launch Narada, start broker
3. Note the broker IP address
4. Background Narada
5. Launch Mahati, connect to broker IP
6. Verify connection success
7. Publish test message
8. Verify message appears in logs

## Current Recommendation

**Use Desktop Integration Tests for automated end-to-end testing.**

The desktop integration test (`DesktopBrokerClientIntegrationTest`) provides:
- ✓ Full broker-client communication testing
- ✓ Automated and repeatable
- ✓ No permission issues
- ✓ Fast execution
- ✓ Easy to run in CI/CD pipelines

For Android, rely on:
- Unit tests for individual components
- Manual testing for cross-app scenarios
- Desktop tests for end-to-end validation

## Future Improvements

Consider:
1. **Espresso Multi-Process**: Limited support for testing across processes
2. **Test Orchestrator**: Better isolation but still same permission model
3. **Rooted Device / Emulator**: Grant elevated permissions for testing
4. **Separate Integration Test App**: Create a standalone test app with required permissions

## Related Files

- Desktop test: `narada/src/desktopTest/kotlin/in/eswarm/narada/DesktopBrokerClientIntegrationTest.kt`
- Android test: `narada/src/androidInstrumentedTest/kotlin/in/eswarm/narada/BrokerClientIntegrationTest.kt`
- Build tasks: `build.gradle.kts` (root)
- Integration testing guide: `INTEGRATION_TESTING.md`

