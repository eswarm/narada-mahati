package `in`.eswarm.narada

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test that validates cross-app communication between Narada (MQTT Broker)
 * and Mahati (MQTT Client) as independent applications.
 *
 * This test:
 * 1. Launches Narada and starts the MQTT broker
 * 2. Backgrounds Narada
 * 3. Launches Mahati
 * 4. Connects Mahati to the local broker
 * 5. Verifies successful connection and message publishing
 */
@RunWith(AndroidJUnit4::class)
class BrokerClientIntegrationTest {

    private lateinit var device: UiDevice
    private val context: Context = ApplicationProvider.getApplicationContext()

    // Package names for the apps
    private val naradaPackage = "in.eswarm.narada"
    private val mahatiPackage = "in.eswarm.mahati"

    // Timeout for UI operations
    private val uiTimeout = 10000L
    private val brokerStartupTimeout = 5000L

    @Before
    fun setUp() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from home screen
        device.pressHome()
        device.waitForIdle()
    }

    @After
    fun tearDown() {
        // Stop both apps
        device.executeShellCommand("am force-stop $naradaPackage")
        device.executeShellCommand("am force-stop $mahatiPackage")
    }

    @Test
    fun testBrokerClientCommunication() {
        // Step 1: Launch Narada
        launchNarada()

        // Step 2: Start the MQTT broker in Narada
        startBroker()

        // Step 3: Background Narada (go to home)
        device.pressHome()
        device.waitForIdle()

        // Give broker a moment to stabilize
        Thread.sleep(brokerStartupTimeout)

        // Step 4: Launch Mahati
        launchMahati()

        // Step 5: Connect to local broker
        connectToLocalBroker()

        // Step 6: Verify connection success
        verifyConnectionSuccess()

        // Step 7: Test publishing a message
        testPublishMessage()
    }

    private fun launchNarada() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(naradaPackage)
        assertNotNull(launchIntent, "Narada app not installed")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launchIntent)

        // Wait for the app to appear
        val naradaLaunched = device.wait(Until.hasObject(By.pkg(naradaPackage).depth(0)), uiTimeout)
        assertTrue(naradaLaunched, "Narada failed to launch")
    }

    private fun startBroker() {
        // Look for the "Start Server" button or similar control
        // This assumes Narada has a button to start the broker
        val startButton = device.wait(
            Until.findObject(By.textContains("Start").clickable(true)),
            uiTimeout
        )

        if (startButton != null) {
            startButton.click()
            device.waitForIdle()
        } else {
            // Try alternative text patterns
            val playButton = device.wait(
                Until.findObject(By.desc("Start Server")),
                uiTimeout
            )
            playButton?.click()
            device.waitForIdle()
        }

        // Wait for broker to start (look for status indicator)
        Thread.sleep(2000)
    }

    private fun launchMahati() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(mahatiPackage)
        assertNotNull(launchIntent, "Mahati app not installed")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launchIntent)

        // Wait for the app to appear
        val mahatiLaunched = device.wait(Until.hasObject(By.pkg(mahatiPackage).depth(0)), uiTimeout)
        assertTrue(mahatiLaunched, "Mahati failed to launch")
    }

    private fun connectToLocalBroker() {
        // Look for connection settings or connect button
        // This may need to navigate to a connection screen first

        // Try to find a FAB or connect button
        val connectButton = device.wait(
            Until.findObject(By.descContains("Connect").clickable(true)),
            uiTimeout
        )

        if (connectButton != null) {
            connectButton.click()
            device.waitForIdle()
        } else {
            // Look for a FAB (Floating Action Button)
            val fab = device.wait(
                Until.findObject(By.res(mahatiPackage, "fab")),
                uiTimeout
            )

            if (fab != null) {
                fab.click()
                device.waitForIdle()

                // Fill in localhost connection details
                fillConnectionDetails()
            }
        }
    }

    private fun fillConnectionDetails() {
        // Fill broker host (localhost or 127.0.0.1)
        val hostField = device.wait(
            Until.findObject(By.textContains("Host").clickable(true)),
            uiTimeout
        )

        if (hostField != null) {
            hostField.click()
            hostField.text = "127.0.0.1"
        }

        // Fill port (default 1883)
        val portField = device.wait(
            Until.findObject(By.textContains("Port").clickable(true)),
            1000L
        )

        if (portField != null) {
            portField.click()
            portField.text = "1883"
        }

        // Click connect/save button
        val saveButton = device.wait(
            Until.findObject(By.textContains("Connect").clickable(true)),
            1000L
        ) ?: device.wait(
            Until.findObject(By.textContains("Save").clickable(true)),
            1000L
        )

        saveButton?.click()
        device.waitForIdle()
    }

    private fun verifyConnectionSuccess() {
        // Look for connection success indicators
        // This could be a "Connected" status, green indicator, or similar

        val connected = device.wait(
            Until.hasObject(By.textContains("Connected")),
            uiTimeout
        ) || device.wait(
            Until.hasObject(By.descContains("Connected")),
            1000L
        )

        assertTrue(connected, "Failed to connect to local MQTT broker")
    }

    private fun testPublishMessage() {
        // Try to publish a test message
        val publishButton = device.wait(
            Until.findObject(By.descContains("Publish").clickable(true)),
            uiTimeout
        )

        if (publishButton != null) {
            publishButton.click()
            device.waitForIdle()

            // Fill in topic
            val topicField = device.wait(
                Until.findObject(By.textContains("Topic").clickable(true)),
                1000L
            )
            topicField?.let {
                it.text = "test/integration"
            }

            // Fill in message
            val messageField = device.wait(
                Until.findObject(By.textContains("Message").clickable(true)),
                1000L
            )
            messageField?.let {
                it.text = "Integration test message"
            }

            // Send
            val sendButton = device.wait(
                Until.findObject(By.textContains("Send").clickable(true)),
                1000L
            )
            sendButton?.click()
            device.waitForIdle()

            // Verify no error messages
            val hasError = device.wait(
                Until.hasObject(By.textContains("Error")),
                2000L
            ) || device.wait(
                Until.hasObject(By.textContains("Failed")),
                1000L
            )

            assertTrue(!hasError, "Message publishing failed")
        }
    }
}
