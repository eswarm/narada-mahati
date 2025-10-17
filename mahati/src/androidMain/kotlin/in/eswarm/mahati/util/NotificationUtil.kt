package `in`.eswarm.mahati.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `in`.eswarm.mahati.Main
import `in`.eswarm.mahati.R
import `in`.eswarm.mahati.navigation.Screen
import `in`.eswarm.mahati.resources.Res
import `in`.eswarm.mahati.resources.fg_channel_description
import `in`.eswarm.mahati.resources.fg_channel_name
import `in`.eswarm.mahati.resources.message_channel_description
import `in`.eswarm.mahati.resources.message_channel_name
import org.jetbrains.compose.resources.getString
import androidx.core.net.toUri

object NotificationUtil {

    const val FG_SERVICE_CHANNEL = "foreground_service_channel"
    const val MESSAGE_CHANNEL = "message_channel"
    private const val MESSAGE_NOTIFICATION_ID = 1001

    suspend fun createNotificationChannel(context: Context) {
        val name = getString(Res.string.fg_channel_name)
        val descriptionText = getString(Res.string.fg_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(FG_SERVICE_CHANNEL, name, importance)
        channel.description = descriptionText
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    suspend fun createMessageChannel(context: Context) {
        val name = getString(Res.string.message_channel_name)
        val descriptionText = getString(Res.string.message_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(MESSAGE_CHANNEL, name, importance)
        channel.description = descriptionText
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun sendNotification(
        context: Context, title: String, message: String, clientID: String, topicName: String
    ) {
        val intent = Intent(context, Main::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Screen.Chat.createDeepLink(clientID, topicName).toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder = NotificationCompat.Builder(context, MESSAGE_CHANNEL)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }

            notify(MESSAGE_NOTIFICATION_ID, builder.build())
        }
    }
}
