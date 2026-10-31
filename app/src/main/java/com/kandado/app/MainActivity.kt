package com.kandado.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private var authed = false
    private var toSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onResume() {
        super.onResume()
        toSettings = false
        when {
            !Store.hasCode(this) -> newCodeFlow("Gumawa ng code", null) { authed = true; apps() }
            !authed -> gate()
            else -> apps()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!toSettings) authed = false
    }

    private fun gate() {
        setContentView(
            CodeEntryView(this, "🔐 Vault", "Ilagay ang code.", CODE_LEN, null, null) { v, c ->
                if (Store.check(this, c)) { authed = true; apps() } else v.fail("Maling code. Subukan ulit.")
            }
        )
    }

    /** Pumili ng code, tapos ulitin para makumpirma. Ginagamit sa unang setup at sa pagpapalit. */
    private fun newCodeFlow(title: String, cancel: (() -> Unit)?, saved: () -> Unit) {
        val sub = "Pumili ng $CODE_LEN na numero. Mapapalitan mo ito sa loob ng Vault."
        setContentView(
            CodeEntryView(this, title, sub, CODE_LEN, if (cancel != null) "Kanselahin" else null, cancel) { _, c1 ->
                setContentView(
                    CodeEntryView(this, "Ulitin ang code", "Ilagay ulit para makumpirma.", CODE_LEN, "Bumalik",
                        { newCodeFlow(title, cancel, saved) }) { v, c2 ->
                        if (c1 == c2) { Store.setCode(this, c1); saved() }
                        else v.fail("Hindi magkapareho. Subukan ulit.")
                    }
                )
            }
        )
    }

    private fun serviceOn(): Boolean {
        val s = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        return s.split(':').any { it.startsWith("$packageName/") && it.endsWith("LockService") }
    }

    private fun label(t: String, size: Float, col: Int, bold: Boolean = false) = TextView(this).apply {
        text = t; textSize = size; setTextColor(col)
        if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private fun apps() {
        val pm = packageManager
        val list = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
        val locked = Store.locked(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(44), dp(18), dp(28))
        }
        root.addView(label("🔐 Vault", 24f, Colors.INK, true))

        if (serviceOn()) {
            root.addView(label("Naka-on ang proteksyon.", 13f, Colors.OK).apply { setPadding(0, dp(8), 0, 0) })
        } else {
            root.addView(label("Hindi pa naka-on ang proteksyon. Walang mala-lock hangga’t hindi ito naka-on.", 13f, Colors.BAD).apply { setPadding(0, dp(8), 0, dp(8)) })
            root.addView(Button(this).apply {
                text = "I-on sa Accessibility"; isAllCaps = false
                setTextColor(Colors.ON_BRASS); setBackgroundColor(Colors.BRASS)
                setOnClickListener {
                    toSettings = true
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            })
        }

        root.addView(Button(this).apply {
            text = "Palitan ang code"; isAllCaps = false
            setTextColor(Colors.INK); setBackgroundColor(Colors.TILE)
            setOnClickListener { newCodeFlow("Bagong code", { apps() }) { authed = true; apps() } }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

        root.addView(label("I-on ang switch ng app na gusto mong i-lock:", 13f, Colors.MUTE).apply { setPadding(0, dp(20), 0, dp(6)) })

        for (ri in list) {
            val pkg = ri.activityInfo.packageName
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(8))
            }
            row.addView(ImageView(this).apply { setImageDrawable(ri.loadIcon(pm)) }, LinearLayout.LayoutParams(dp(40), dp(40)))
            row.addView(
                label(ri.loadLabel(pm).toString(), 16f, Colors.INK).apply { setPadding(dp(14), 0, dp(8), 0) },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
            row.addView(Switch(this).apply {
                isChecked = pkg in locked
                setOnCheckedChangeListener { _, on -> Store.setLocked(this@MainActivity, pkg, on) }
            })
            root.addView(row)
        }

        setContentView(ScrollView(this).apply { setBackgroundColor(Colors.BG); addView(root) })
    }
}
