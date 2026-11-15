package com.example.nabjaki

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::unused.isInitialized) { }
        showScreen()
    }

    private val unused = lazy { Unit }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val info = TextView(this).apply {
            text = "납작이를 홈 화면에서 실행하려면 아래 두 권한이 필요해요.\n\n1. 다른 앱 위에 표시: 홈 화면에 납작이를 띄우기 위해 필요해요.\n2. 사용정보 접근: 현재 홈 화면인지 확인하기 위해 필요해요."
            textSize = 17f
        }
        layout.addView(info)

        val overlayButton = Button(this).apply {
            text = if (Settings.canDrawOverlays(this@MainActivity)) "오버레이 권한 허용됨" else "다른 앱 위에 표시 허용하기"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        }
        layout.addView(overlayButton)

        val usageButton = Button(this).apply {
            text = if (hasUsageAccess()) "사용정보 접근 허용됨" else "사용정보 접근 허용하기"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        layout.addView(usageButton)

        val startButton = Button(this).apply {
            text = "납작이 시작"
            isEnabled = Settings.canDrawOverlays(this@MainActivity) && hasUsageAccess()
            setOnClickListener {
                ContextCompat.startForegroundService(
                    this@MainActivity,
                    Intent(this@MainActivity, NabjakiService::class.java)
                )
                finish()
            }
        }
        layout.addView(startButton)

        setContentView(layout)
    }
}
