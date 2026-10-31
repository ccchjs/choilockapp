package com.kandado.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

/** Nakikinig kung anong app ang bumukas. Kapag naka-lock ito, ilalabas ang dial. */
class LockService : AccessibilityService() {
    private var ime: Set<String> = emptySet()

    override fun onServiceConnected() {
        val m = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ime = m.enabledInputMethodList.map { it.packageName }.toSet()
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui" || pkg in ime) return
        if (pkg == Session.unlocked) return
        Session.unlocked = null          // lumipat na sa ibang app, kaya re-lock
        if (pkg in Store.locked(this) && !Session.showing) {
            val i = Intent(this, LockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra("pkg", pkg)
            startActivity(i)
        }
    }

    override fun onInterrupt() {}
}
