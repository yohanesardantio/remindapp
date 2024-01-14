package com.mobileprak.remindapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mobileprak.remindapp.R
import com.mobileprak.remindapp.activity.MainActivity
import com.mobileprak.remindapp.database.DatabaseHandler
import com.mobileprak.remindapp.models.Reminder
import java.util.*

class ReminderService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("ReminderService", "onCreate called")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("ReminderService", "onBind called")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ReminderService", "onStartCommand called")
        val reminderId = intent?.getLongExtra("reminderId", 0)
        val databaseHandler = DatabaseHandler(this)
        val reminder = databaseHandler.getReminderById(reminderId ?: 0)
        showAlarmNotification(reminder)

        val speakText = reminder.title + " " + reminder.description
        tts = TextToSpeech(this,
            TextToSpeech.OnInitListener {
                if (it != TextToSpeech.ERROR) {
                    tts?.language = Locale.US
                    tts?.speak(speakText, TextToSpeech.QUEUE_ADD, null, null)
                } else {
                    Log.d("ReminderService", "Error: $it")
                }
            })
        Log.d("ReminderService", speakText)
        return START_STICKY
    }

    private fun showAlarmNotification(reminder: Reminder) {
        Log.d("ReminderService", "showAlarmNotification called")

        createNotificationChannel(reminder.id.toInt())

        // build notification
        val builder = NotificationCompat.Builder(this, reminder.id.toString())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(reminder.title)
            .setContentText(reminder.description)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.putExtra("reminderId", reminder.id)
        notificationIntent.putExtra("from", "Notification")

        // Menggunakan FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.setContentIntent(pendingIntent)
        val notification = builder.build()

        // Add as notification
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminder.id.toInt(), notification)
    }

    private fun createNotificationChannel(id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                id.toString(),
                "Reminder Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        Log.d("ReminderService", "onDestroy called")
        super.onDestroy()

        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        tts?.stop()
        tts?.shutdown()
    }
}
