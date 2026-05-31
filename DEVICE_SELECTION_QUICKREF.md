# Device Selection Quick Reference

## Available Environment Variables

| Variable | Purpose | Priority |
|----------|---------|----------|
| `NARADA_DEVICE` | Specify device for Narada (broker) | Highest for Narada |
| `MAHATI_DEVICE` | Specify device for Mahati (client) | Highest for Mahati |
| `ANDROID_SERIAL` | Default device for both apps | Fallback |

## Common Usage Scenarios

### Scenario 1: Single Device (Auto-Detect)
**When:** You have only one device/emulator connected

```bash
# No environment variables needed
.\gradlew runAndroidIntegrationTests
```

**Result:** Both apps installed on the single connected device

---

### Scenario 2: Multiple Devices - Same Device for Both Apps
**When:** You want both apps on the same device, but have multiple devices connected

```bash
# Windows
$env:ANDROID_SERIAL = "28121JEGR14830"
.\gradlew runAndroidIntegrationTests

# Mac/Linux
export ANDROID_SERIAL=28121JEGR14830
./gradlew runAndroidIntegrationTests
```

**Result:** Both apps installed on device `28121JEGR14830`

---

### Scenario 3: Cross-Device Testing (RECOMMENDED for Integration Tests)
**When:** You want realistic broker-client testing across two devices

```bash
# 1. Find your devices
adb devices
# Output:
# 28121JEGR14830  device    (Pixel 6a)
# RZCY307XXFH     device    (Galaxy S23)

# 2. Set different device for each app

# Windows PowerShell
$env:NARADA_DEVICE = "28121JEGR14830"  # Broker on Pixel 6a
$env:MAHATI_DEVICE = "RZCY307XXFH"     # Client on Galaxy S23
.\gradlew runAndroidIntegrationTests

# Mac/Linux
export NARADA_DEVICE=28121JEGR14830
export MAHATI_DEVICE=RZCY307XXFH
./gradlew runAndroidIntegrationTests
```

**Result:** 
- Narada (broker) on Pixel 6a
- Mahati (client) on Galaxy S23

---

### Scenario 4: Mixed Configuration
**When:** You want to specify one app's device, let the other use fallback

```bash
# Narada on specific device, Mahati uses ANDROID_SERIAL fallback
$env:ANDROID_SERIAL = "RZCY307XXFH"
$env:NARADA_DEVICE = "28121JEGR14830"
.\gradlew installIntegrationTestApks
```

**Result:**
- Narada on `28121JEGR14830` (from NARADA_DEVICE)
- Mahati on `RZCY307XXFH` (from ANDROID_SERIAL)

---

## Priority Resolution

### For Narada Installation:
```
NARADA_DEVICE → ANDROID_SERIAL → Auto-detect
   (highest)        (medium)       (lowest)
```

### For Mahati Installation:
```
MAHATI_DEVICE → ANDROID_SERIAL → Auto-detect
   (highest)        (medium)       (lowest)
```

---

## Quick Commands

### Check Current Environment
```bash
# Windows PowerShell
Get-ChildItem Env: | Select-String "DEVICE|SERIAL"

# Mac/Linux
env | grep -E "DEVICE|SERIAL"
```

### List Connected Devices
```bash
adb devices -l
```

### Clear Environment Variables
```bash
# Windows PowerShell
Remove-Item Env:\NARADA_DEVICE -ErrorAction SilentlyContinue
Remove-Item Env:\MAHATI_DEVICE -ErrorAction SilentlyContinue
Remove-Item Env:\ANDROID_SERIAL -ErrorAction SilentlyContinue

# Mac/Linux
unset NARADA_DEVICE MAHATI_DEVICE ANDROID_SERIAL
```

### Verify Installation
After running the install tasks, check the console output:
```
Installing Narada on device: 28121JEGR14830 (from NARADA_DEVICE)
Installing Mahati on device: RZCY307XXFH (from MAHATI_DEVICE)
```

The source indicator shows which environment variable was used.

---

## Gradle Tasks

| Task | Description |
|------|-------------|
| `installNaradaApk` | Install only Narada on selected device |
| `installMahatiApk` | Install only Mahati on selected device |
| `installIntegrationTestApks` | Install both apps on selected devices |
| `runAndroidIntegrationTests` | Build, install, and run integration tests |

---

## Troubleshooting

### Error: "more than one device/emulator"
**Problem:** Multiple devices connected, no device specified

**Solution:** Set at least `ANDROID_SERIAL` or use app-specific variables

### Apps installed on wrong devices
**Problem:** Priority confusion

**Solution:** Check which env vars are set:
- `NARADA_DEVICE` overrides `ANDROID_SERIAL` for Narada
- `MAHATI_DEVICE` overrides `ANDROID_SERIAL` for Mahati

### Want to reset to defaults
**Problem:** Old env vars affecting behavior

**Solution:** Clear all device-related env vars (see "Clear Environment Variables" above)

---

## Examples by Use Case

### Local Development (One Device)
```bash
# Just run - auto-detects your device
.\gradlew runAndroidIntegrationTests
```

### CI/CD Pipeline (Specific Device)
```yaml
# .github/workflows/integration-test.yml
env:
  ANDROID_SERIAL: ${{ secrets.TEST_DEVICE_SERIAL }}
run: ./gradlew runAndroidIntegrationTests
```

### QA Team (Cross-Device Verification)
```bash
# Test realistic broker-client scenario
$env:NARADA_DEVICE = "BROKER_DEVICE_SERIAL"
$env:MAHATI_DEVICE = "CLIENT_DEVICE_SERIAL"
.\gradlew runAndroidIntegrationTests
```

### Manual Testing (Install Only)
```bash
# Install without running tests
$env:NARADA_DEVICE = "28121JEGR14830"
$env:MAHATI_DEVICE = "RZCY307XXFH"
.\gradlew installIntegrationTestApks
```

---

**For more details, see [INTEGRATION_TESTING.md](INTEGRATION_TESTING.md)**

