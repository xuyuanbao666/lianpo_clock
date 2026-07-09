package com.lianpo.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lianpo.clock.MainActivity
import com.lianpo.clock.R
import com.lianpo.clock.util.TimerState
import com.lianpo.clock.util.TimerType

class TimerService : Service() {

    private val binder = TimerBinder()
    private lateinit var notificationManager: NotificationManager

    private var timerState = TimerState.IDLE
    private var timerType = TimerType.WORK
    private var timeRemaining = 0
    private var totalTime = 0
    private var onUpdate: ((TimerState, TimerType, Int, Int) -> Unit)? = null

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "计时器通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示计时器状态"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setOnUpdateListener(listener: (TimerState, TimerType, Int, Int) -> Unit) {
        onUpdate = listener
    }

    fun updateTimerState(state: TimerState, type: TimerType, remaining: Int, total: Int) {
        timerState = state
        timerType = type
        timeRemaining = remaining
        totalTime = total

        onUpdate?.invoke(state, type, remaining, total)

        if (state == TimerState.RUNNING) {
            startForeground(NOTIFICATION_ID, createNotification())
        } else if (state == TimerState.IDLE || state == TimerState.FINISHED) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeText = when (timerType) {
            TimerType.WORK -> "专注中"
            TimerType.SHORT_BREAK -> "短休息"
            TimerType.LONG_BREAK -> "长休息"
        }

        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        val timeText = "%02d:%02d".format(minutes, seconds)

        val stateText = when (timerState) {
            TimerState.RUNNING -> "进行中"
            TimerState.PAUSED -> "已暂停"
            else -> ""
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🍅 $typeText $stateText")
            .setContentText("剩余时间 $timeText")
            .setOngoing(timerState == TimerState.RUNNING || timerState == TimerState.PAUSED)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }
}