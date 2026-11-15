package com.example.nabjaki

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class NabjakiService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: TextView? = null
    private lateinit var windowManager: WindowManager

    // Samsung One UI Home is included. Other launchers may need their package name added.
    private val launcherPackages = setOf(
        "com.sec.android.app.launcher",
        "com.google.android.apps.nexuslauncher"
    )

    private val checkTask = object : Runnable {
        override fun run() {
            updateOverlayVisibility()
            handler.postDelayed(this, 1200L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, buildNotification())
        handler.post(checkTask)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel("nabjaki_service", "납작이 실행", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, "nabjaki_service")
            .setContentTitle("납작이 실행 중")
            .setContentText("홈 화면 상태를 확인하고 있어요.")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }

    private fun isHomeScreenVisible(): Boolean {
        val usage = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val events = usage.queryEvents(end - 10_000L, end)
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            ) {
                foregroundPackage = event.packageName
            }
        }
        return foregroundPackage in launcherPackages
    }

    private fun updateOverlayVisibility() {
        if (isHomeScreenVisible()) {
            showOverlay()
        } else {
            hideOverlay()
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val view = TextView(this).apply {
            text = "납작이 이미지 연결 예정"
            textSize = 18f
            setTextColor(0xFF222222.toInt())
            setPadding(24, 16, 24, 16)
            setBackgroundColor(0x00FFFFFF)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 100
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (_: Exception) {
            overlayView = null
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        overlayView = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkTask)
        hideOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
