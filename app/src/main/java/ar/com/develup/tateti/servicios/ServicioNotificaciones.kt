package ar.com.develup.tateti.servicios

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ar.com.develup.tateti.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// TODO-07-NOTIFICATION
/*
class ServicioNotificaciones : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data != null) {
            mostrarNotificacion(remoteMessage.data)
        }
    }

    private fun mostrarNotificacion(notificationData: Map<String, String>) {
        val intent = Intent(this, ActividadInicial::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val titulo = notificationData["title"]
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(titulo)
                .setContentText(notificationData["body"])
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationData["body"]))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}

 */

class ServicioNotificaciones : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.description = "Description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            channel.canBypassDnd()
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        notificationBuilder.setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.naranjaFirebase))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(remoteMessage!!.notification!!.body)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)


        notificationManager.notify(1000, notificationBuilder.build())

    }
}
