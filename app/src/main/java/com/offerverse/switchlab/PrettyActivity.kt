package com.offerverse.switchlab

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import kotlin.math.abs

open class PrettyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BgColor
        window.navigationBarColor = BgColor

        val result = Calc.run(
            LocalDate.of(2025, 10, 20), LocalDate.of(2026, 11, 1), LocalDate.of(2029, 11, 1),
            110000.0, 0.76, 0.70, 18200.0, 13500.0, 170.0, 241.70, 7000.0, 3500.0, 0.0, 140000.0,
            listOf(120000.0, 125000.0, 130000.0, 135000.0, 140000.0, 145000.0, 150000.0)
        )

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(BgColor)
        scroll.isFillViewport = true

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(70), dp(18), dp(28))
        root.setBackgroundColor(BgColor)
        scroll.addView(root, LinearLayout.LayoutParams(-1, -2))

        root.addView(hero())
        root.addView(gap(12))

        val row1 = row()
        row1.addView(metric("Stay total", money(result.stay), false), weight())
        row1.addView(metric("Switch total", money(result.selectedSwitch), false), weight())
        root.addView(row1)

        val delta = result.selectedSwitch - result.stay
        val row2 = row()
        row2.addView(metric("Difference", signed(delta), delta >= 0), weight())
        row2.addView(metric("Break-even base", money(result.breakEven), true), weight())
        root.addView(row2)

        val decision = if (delta >= 0) "At 140k base, switching is basically break-even. A stronger offer starts at 145k or higher." else "Staying wins. Raise base or add relocation/sign-on."
        root.addView(panel("Decision signal", label(decision, 16, if (delta >= 0) GreenColor else RedColor, true)))
        root.addView(panel("Scenario advantage", ProChartView(this, result.scenarios)))
        root.addView(panel("What-if salary ladder", ladder(result.scenarios)))
        root.addView(label("Defaults: Amazon base 110k, 76 percent take-home, Cap One 70 percent take-home, 18.2k first bonus, 13.5k second bonus, 170 RSUs, 241.70 RSU price, 3.5k relocation repayment.", 12, MutedColor, false).apply { setPadding(dp(4), dp(12), dp(4), dp(28)) })
        setContentView(scroll)
    }

    private fun hero(): View {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(20), dp(20), dp(20), dp(20))
        box.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(0xFF4C1D95.toInt(), 0xFF0E7490.toInt(), 0xFF111827.toInt())).apply { cornerRadius = dp(28).toFloat() }
        val pill = label("Amazon to Cap One", 13, 0xFFE0F2FE.toInt(), true)
        pill.gravity = Gravity.CENTER
        pill.setPadding(dp(10), dp(5), dp(10), dp(5))
        pill.background = rounded(0x33111111, dp(20), 0x55FFFFFF)
        box.addView(pill, LinearLayout.LayoutParams(-2, -2))
        box.addView(label("SwitchLab", 34, Color.WHITE, true).apply { setPadding(0, dp(12), 0, 0) })
        box.addView(label("Job switch salary estimator", 16, 0xDDEFFFFFF.toInt(), false))
        box.addView(label("Salary, bonus, RSU, relocation clawback, taxes, and break-even math.", 13, 0xBDEFFFFFF.toInt(), false))
        return box
    }

    private fun metric(title: String, value: String, good: Boolean): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(14), dp(14), dp(14), dp(14))
        box.background = rounded(if (good) 0xFF102A24.toInt() else 0xFF151B2E.toInt(), dp(20), 0x18FFFFFF)
        box.addView(label(title, 12, MutedColor, false))
        box.addView(label(value, 23, if (good) GreenColor else TextColor, true))
        return box
    }

    private fun panel(title: String, child: View): View {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(16), dp(16), dp(16), dp(16))
        box.background = rounded(CardColor, dp(22), 0x1FFFFFFF)
        box.addView(label(title, 18, TextColor, true))
        child.setPadding(0, dp(10), 0, 0)
        box.addView(child)
        return box.apply { layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(7), 0, dp(7)) } }
    }

    private fun ladder(items: List<Scenario>): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        items.forEach { s ->
            val line = LinearLayout(this)
            line.orientation = LinearLayout.HORIZONTAL
            line.setPadding(0, dp(7), 0, dp(7))
            line.addView(label(money0(s.base), 16, TextColor, true), LinearLayout.LayoutParams(0, -2, 1f))
            val right = label(signed(s.delta), 16, if (s.delta >= 0) GreenColor else RedColor, true)
            right.gravity = Gravity.END
            line.addView(right, LinearLayout.LayoutParams(0, -2, 1f))
            box.addView(line)
        }
        return box
    }

    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
    private fun gap(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }
    private fun label(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply { text = value; textSize = size.toFloat(); setTextColor(color); includeFontPadding = true; if (bold) setTypeface(typeface, Typeface.BOLD) }
    private fun rounded(color: Int, radius: Int, stroke: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat(); setStroke(1, stroke) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

class ProChartView(context: android.content.Context, private val scenarios: List<Scenario>) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) { setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (240 * resources.displayMetrics.density).toInt()) }
    override fun onDraw(canvas: Canvas) {
        if (scenarios.isEmpty()) return
        val baseY = height * 0.48f
        val slot = width.toFloat() / scenarios.size
        val barW = slot * 0.52f
        val maxAbs = scenarios.maxOf { abs(it.delta) }.coerceAtLeast(1.0)
        paint.color = 0xFF64748B.toInt()
        paint.strokeWidth = 3f
        canvas.drawLine(0f, baseY, width.toFloat(), baseY, paint)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 24f
        scenarios.forEachIndexed { i, s ->
            val h = (abs(s.delta) / maxAbs * height * 0.34f).toFloat()
            val cx = slot * i + slot / 2f
            val left = cx - barW / 2f
            val top = if (s.delta >= 0) baseY - h else baseY
            val bottom = if (s.delta >= 0) baseY else baseY + h
            paint.color = if (s.delta >= 0) GreenColor else RedColor
            canvas.drawRoundRect(left, top, left + barW, bottom, 18f, 18f, paint)
            paint.color = 0xFFCBD5E1.toInt()
            canvas.drawText((s.base / 1000).toInt().toString() + "k", cx, height - 18f, paint)
        }
    }
}

private const val BgColor = 0xFF050713.toInt()
private const val CardColor = 0xFF0D1324.toInt()
private const val TextColor = 0xFFE5E7EB.toInt()
private const val MutedColor = 0xFF9CA3AF.toInt()
private const val GreenColor = 0xFF22C55E.toInt()
private const val RedColor = 0xFFEF4444.toInt()
