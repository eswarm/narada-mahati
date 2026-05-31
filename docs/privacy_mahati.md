# Privacy Policy for Mahati

**Last Updated:** May 31, 2026

## Introduction

Mahati ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how our MQTT client application handles information when you use our app.

## Information Collection and Use

### Data We DO NOT Collect

Mahati is designed with privacy as a priority. We do **not** collect, store, or transmit any of the following:

- Personal identification information
- Location data
- Usage analytics or statistics
- Crash reports or diagnostics
- Advertising identifiers
- Contact information
- Photos, videos, or media files
- Device identifiers (beyond what's required by Android)

### Data Stored Locally on Your Device

Mahati stores the following information **locally on your device only**:

1. **MQTT Connection Details:**
   - Broker hostname/IP address
   - Port number
   - Client ID
   - Username and password (if provided)
   - Connection preferences

2. **Topic Subscriptions:**
   - Topics you subscribe to
   - Topic-specific settings

3. **Message History:**
   - MQTT messages received and sent
   - Message timestamps

4. **App Settings:**
   - User interface preferences
   - App configuration

**Important:** All this data is stored in a local SQLite database on your device. It is **never transmitted to us or any third party**.

## Data You Share with Third Parties

When you use Mahati to connect to an MQTT broker, you are establishing a direct connection between your device and that broker. Any data transmitted to or from the broker is subject to the broker operator's privacy policy, not ours.

**We do not:**
- Operate MQTT brokers
- Have access to your MQTT communications
- Monitor your connections
- Intercept your messages

## Data Security

### On-Device Security
- All connection credentials are stored in the app's private database
- The database is protected by Android's app sandboxing
- Only Mahati can access this data on your device

### Network Security
- When you enable TLS/SSL for MQTT connections, your communications are encrypted in transit
- We strongly recommend using TLS/SSL for all connections to protect your data

## Data Retention and Deletion

### User Control
You have complete control over your data:

- **Delete Specific Connections:** Remove individual MQTT connection configurations within the app
- **Clear Message History:** Delete message logs from within the app
- **Complete Data Removal:** Uninstall the app or clear app data through Android settings to permanently delete all data

### No Server-Side Storage
Since we don't collect or store data on our servers, there is no data retention on our end.

## Permissions

Mahati requests the following Android permissions:

### Camera Permission (Optional)
- **Purpose:** To scan QR codes for quick broker configuration
- **Usage:** Only when you explicitly use the QR scanner feature
- **Can be denied:** The app works without this permission; you can manually enter connection details

### Internet Permission (Required)
- **Purpose:** To connect to MQTT brokers
- **Usage:** Only for establishing MQTT connections to brokers you specify
- **Cannot be denied:** This is essential for app functionality

## Third-Party Services

Mahati uses the following third-party libraries:

### HiveMQ MQTT Client
- **Purpose:** MQTT protocol implementation
- **Data Access:** Only the connection data you explicitly configure
- **Privacy Policy:** [HiveMQ Privacy Policy](https://www.hivemq.com/privacy-policy/)

**Note:** This library runs entirely on your device and does not transmit data to third-party services.

## Children's Privacy

Mahati is a developer tool and not directed at children under 13. We do not knowingly collect information from children. If you believe a child has provided information through our app, please contact us, though note that we don't collect such data in any case.

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. Changes will be reflected in the "Last Updated" date at the top of this policy. We encourage you to review this policy periodically.

Significant changes will be communicated through:
- App update release notes
- In-app notifications (if implemented)

## Your Rights

Depending on your jurisdiction, you may have rights regarding your data:

- **Access:** You have full access to all data through the app interface
- **Deletion:** You can delete data at any time through the app or Android settings
- **Portability:** Your data is stored in a standard SQLite format on your device

## International Users

Mahati is available globally. Since all data is stored locally on your device, there are no international data transfers involving us.

## Contact Us

If you have questions about this Privacy Policy or Mahati's privacy practices, please contact us:

**Email:** [eswar.malla+support@gmail.com]  
**GitHub:** [https://github.com/eswarm/narada-mahati] 

## Consent

By using Mahati, you consent to this Privacy Policy.

---

## Summary (TL;DR)

✅ **We don't collect your data**  
✅ **Everything stays on your device**  
✅ **No tracking or analytics**  
✅ **No ads**  
✅ **You can delete everything anytime**  
✅ **Direct MQTT connections only**  
✅ **Your privacy matters**

---

**Mahati - Privacy-First MQTT Client**

*This privacy policy was generated on May 31, 2026 and reflects the practices of Mahati version 1.1 and all subsequent versions unless otherwise noted.*


