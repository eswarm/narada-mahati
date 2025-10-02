package `in`.eswarm.mahati.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.fg_channel_description
import `in`.eswarm.mahati.resources.fg_channel_name
import org.jetbrains.compose.resources.getString

object NotificationUtil {

    const val FG_SERVICE_CHANNEL = "foreground_service_channel"

    suspend fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel
        val name = getString( Res.string.fg_channel_name)
        val descriptionText = getString(Res.string.fg_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(FG_SERVICE_CHANNEL, name, importance)
        channel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}