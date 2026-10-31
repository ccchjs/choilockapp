package com.kandado.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.atan2
import kotlin.math.min

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

object Colors {
    val BG = 0xFF27313A.toInt()
    val TILE = 0xFF33414C.toInt()
    val BRASS = 0xFFD6A84A.toInt()
    val BRASS_DARK = 0xFF8D6A1F.toInt()
    val DARK = 0xFF222B32.toInt()
    val INK = 0xFFF1EFE9.toInt()
    val MUTE = 0xFF9AA7B1.toInt()
    val BAD = 0xFFE0604F.toInt()
    val OK = 0xFF58C28A.toInt()
    val ON_BRASS = 0xFF2A1F05.toInt()
}

/** Iniikot na dial na may numerong 0-9, parang combination lock. */
class DialView(c: Context) : View(c) {
    var rot = 0f
    val digit: Int get() = ((Math.round(-rot / 36f) % 10) + 10) % 10

    private val dens = c.resources.displayMetrics.density
    private var last = 0f
    private val brass = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Colors.BRASS }
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.BRASS_DARK; style = Paint.Style.STROKE; strokeWidth = 6f * dens
    }
    private val num = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.ON_BRASS; textAlign = Paint.Align.CENTER
        textSize = 22f * dens; typeface = Typeface.DEFAULT_BOLD
    }
    private val hub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Colors.DARK }
    private val hubText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.INK; textAlign = Paint.Align.CENTER
        textSize = 44f * dens; typeface = Typeface.DEFAULT_BOLD
    }
    private val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Colors.INK }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f * 0.88f
        canvas.save()
        canvas.rotate(rot, cx, cy)
        canvas.drawCircle(cx, cy, r, brass)
        canvas.drawCircle(cx, cy, r, edge)
        val off = -(num.ascent() + num.descent()) / 2f
        for (i in 0..9) {
            canvas.save()
            canvas.rotate(i * 36f, cx, cy)
            canvas.drawText(i.toString(), cx, cy - r * 0.78f + off, num)
            canvas.restore()
        }
        canvas.restore()
        canvas.drawCircle(cx, cy, r * 0.42f, hub)
        canvas.drawText(digit.toString(), cx, cy - (hubText.ascent() + hubText.descent()) / 2f, hubText)
        val tri = Path().apply {
            moveTo(cx - 11f * dens, cy - r - 14f * dens)
            lineTo(cx + 11f * dens, cy - r - 14f * dens)
            lineTo(cx, cy - r + 6f * dens)
            close()
        }
        canvas.drawPath(tri, mark)
    }

    private fun ang(e: MotionEvent): Float =
        Math.toDegrees(atan2((e.y - height / 2f).toDouble(), (e.x - width / 2f).toDouble())).toFloat()

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                last = ang(e)
            }
            MotionEvent.ACTION_MOVE -> {
                val a = ang(e)
                var d = a - last
                if (d > 180f) d -= 360f
                if (d < -180f) d += 360f
                rot += d
                last = a
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> snap()
        }
        return true
    }

    private fun snap() {
        val to = Math.round(rot / 36f) * 36f
        ValueAnimator.ofFloat(rot, to).apply {
            duration = 120
            addUpdateListener { rot = it.animatedValue as Float; invalidate() }
            start()
        }
    }
}

/** Buong screen: pamagat, mga slot ng code, dial, at mga button. */
class CodeEntryView(
    ctx: Context,
    title: String,
    sub: String,
    private val length: Int,
    cancelText: String?,
    onCancel: (() -> Unit)?,
    private val onComplete: (CodeEntryView, String) -> Unit
) : LinearLayout(ctx) {

    private val entered = StringBuilder()
    private val slots = TextView(ctx)
    private val msg = TextView(ctx)
    private val dial = DialView(ctx)

    private fun d(v: Int) = context.dp(v)

    private fun tv(t: String, size: Float, col: Int, bold: Boolean = false) = TextView(context).apply {
        text = t; textSize = size; setTextColor(col); gravity = Gravity.CENTER
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun btn(t: String, primary: Boolean, click: () -> Unit) = Button(context).apply {
        text = t; isAllCaps = false
        setTextColor(if (primary) Colors.ON_BRASS else Colors.INK)
        setBackgroundColor(if (primary) Colors.BRASS else Colors.TILE)
        setOnClickListener { click() }
    }

    init {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setBackgroundColor(Colors.BG)
        setPadding(d(20), d(48), d(20), d(24))
        addView(tv(title, 22f, Colors.INK, true))
        addView(tv(sub, 13f, Colors.MUTE).apply { setPadding(0, d(6), 0, 0) })
        slots.apply { textSize = 28f; setTextColor(Colors.BRASS); gravity = Gravity.CENTER; setPadding(0, d(18), 0, d(12)) }
        addView(slots)
        addView(dial, LinearLayout.LayoutParams(d(280), d(280)))
        msg.apply { textSize = 13f; gravity = Gravity.CENTER; minHeight = d(36); setPadding(0, d(10), 0, d(10)) }
        addView(msg)
        val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        fun lp() = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(d(6), 0, d(6), 0) }
        if (cancelText != null) row.addView(btn(cancelText, false) { onCancel?.invoke() }, lp())
        row.addView(btn("Ilagay", true) { enter() }, lp())
        addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        say("Ikutin ang dial. Pindutin ang “Ilagay” kapag nasa taas na ang numero.", Colors.MUTE)
        refresh()
    }

    private fun refresh() {
        slots.text = (0 until length).joinToString(" ") { if (it < entered.length) "●" else "○" }
    }

    private fun say(t: String, col: Int) { msg.text = t; msg.setTextColor(col) }

    private fun enter() {
        entered.append(dial.digit)
        refresh()
        if (entered.length >= length) onComplete(this, entered.toString())
        else say("Ikutin ulit sa susunod na numero.", Colors.MUTE)
    }

    fun fail(text: String) {
        say(text, Colors.BAD)
        entered.clear()
        refresh()
        ObjectAnimator.ofFloat(dial, "translationX", 0f, -24f, 24f, -14f, 14f, 0f).setDuration(350).start()
    }
}
