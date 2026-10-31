package com.kandado.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager

class LockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        Session.showing = true
        val pkg = intent.getStringExtra("pkg") ?: run { finish(); return }
        val label = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) { pkg }

        setContentView(
            CodeEntryView(this, "🔒 $label", "Ilagay ang code para buksan.", CODE_LEN, "Umalis", { goHome() }) { v, code ->
                if (Store.check(this, code)) {
                    Session.unlocked = pkg
                    finish()
                } else {
                    v.fail("Maling code. Subukan ulit.")
                }
            }
        )
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { goHome() }

    override fun onStop() {
        super.onStop()
        Session.showing = false
        finish()
    }

    override fun onDestroy() {
        Session.showing = false
        super.onDestroy()
    }
}
