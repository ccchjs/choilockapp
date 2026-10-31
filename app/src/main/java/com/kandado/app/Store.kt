package com.kandado.app

import android.content.Context
import java.security.MessageDigest

const val CODE_LEN = 4

object Store {
    private fun p(c: Context) = c.getSharedPreferences("kandado", Context.MODE_PRIVATE)

    private fun hash(code: String): String =
        MessageDigest.getInstance("SHA-256").digest("kandado:$code".toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun hasCode(c: Context) = p(c).contains("code")
    fun setCode(c: Context, code: String) = p(c).edit().putString("code", hash(code)).apply()
    fun check(c: Context, code: String) = p(c).getString("code", null) == hash(code)

    fun locked(c: Context): Set<String> = p(c).getStringSet("locked", emptySet()) ?: emptySet()
    fun setLocked(c: Context, pkg: String, on: Boolean) {
        val s = locked(c).toMutableSet()
        if (on) s.add(pkg) else s.remove(pkg)
        p(c).edit().putStringSet("locked", s).apply()
    }
}

object Session {
    var unlocked: String? = null
    var showing = false
}
