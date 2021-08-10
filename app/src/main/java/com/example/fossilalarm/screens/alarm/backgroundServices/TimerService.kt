package com.example.fossilalarm.screens.alarm.backgroundServices

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.fossilalarm.R
import com.example.fossilalarm.screens.alarm.AlarmActivity
import com.example.fossilalarm.screens.alarm.convertMilisecondToDurationHHMMSS
import com.example.fossilalarm.screens.alarm.entity.TimerPayload
import com.example.fossilalarm.screens.alarm.timerSubject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class TimerService : Service() {

    private val timePayload = TimerPayload()
    private val ringtoneUri by lazy { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) }
    private val ringtone by lazy { RingtoneManager.getRingtone(this, ringtoneUri) }

    private val vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    private val CHANNEL_ID = "timer"
    private val NOTIFICATION_ID = 100

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.getStringExtra("state")

        timePayload.totalDurationInMs =
            intent?.getLongExtra("totalDuration", timePayload.totalDurationInMs)
                ?: timePayload.totalDurationInMs
        timePayload.currentCountingDownInMs = intent?.getLongExtra("currentCountingDown", 0) ?: 0

        timePayload.timeToDisplay =
            timePayload.currentCountingDownInMs.convertMilisecondToDurationHHMMSS()

        timePayload.isRunning = command == "running"
        if(timePayload.isRunning) {
            showNotification("Timer is running")
        } else {
            showNotification("Timer has stopped")
        }
        timePayload.isOver = false

        timerSubject.onNext(
            timePayload
        )
        stopRingtone()

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startTimer()
    }

    private fun startTimer() {
        Observable.interval(1, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (timePayload.isRunning) {
                    countDown()
                }
            }, {})
    }

    private fun countDown() {
        if (timePayload.currentCountingDownInMs >= 0) {
            timePayload.currentCountingDownInMs--
            timePayload.isRunning = timePayload.currentCountingDownInMs > 0

            timePayload.timeToDisplay =
                timePayload.currentCountingDownInMs.convertMilisecondToDurationHHMMSS()

            if (timePayload.currentCountingDownInMs == 0L) {
                //Reset counter
                timePayload.currentCountingDownInMs = timePayload.totalDurationInMs
                timePayload.isOver = true
                showNotification("Time over. Click to stop", NotificationManager.IMPORTANCE_HIGH)
                playRingtone()
            }

            timerSubject.onNext(
                timePayload
            )
        }
    }

    private fun playRingtone() {
        ringtone.play()
        vibrator.vibrate(longArrayOf(0, 100, 1000), 0)
    }

    private fun stopRingtone() {
        ringtone.stop()
        vibrator.cancel()
    }

    private fun showNotification(message: String, importance: Int = NotificationManager.IMPORTANCE_DEFAULT) {
        //Create notification builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle("Timer")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
        val notificationIntent = Intent(this, AlarmActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        builder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance)
            notificationManager.createNotificationChannel(notificationChannel)
            startForeground(NOTIFICATION_ID, builder.build())
        } else {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }
}