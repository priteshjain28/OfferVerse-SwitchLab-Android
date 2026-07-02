package com.offerverse.switchlab

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import kotlin.math.abs

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = Calc.run(
            LocalDate.of(2025, 10, 20),
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2029, 11, 1),
            110000.0,
            0.76,
            0.70,
            18200.0,
            13500.0,
            170.0,
            241.70,
            7000.0,
            3500.0,
            0.0,
            140000.0,
            listOf(120000.0, 125000.0, 130000.0, 135000.0, 140000.0, 145000.0, 150000.0)
        )
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(28, 28, 28, 28)
        root.setBackgroundColor(Color.rgb(5, 7, 19))
        val scroll = ScrollView(this)
        scroll.addView(root)

        root.addView(title("OfferVerse SwitchLab", 30, true))
        root.addView(text("Native job switch salary estimator", 15, Color.rgb(156, 163, 175)))
        root.addView(space(18))

        val row1 = row()
        row1.addView(card("Stay total", money(result.stay), false), weight())
        row1.addView(card("Switch total", money(result.selectedSwitch), false), weight())
        root.addView(row1)

        val row2 = row()
        row2.addView(card("Delta", signed(result.selectedSwitch - result.stay), result.selectedSwitch >= result.stay), weight())
        row2.addView(card("Break-even base", money(result.breakEven), true), weight())
        root.addView(row2)

        root.addView(section("Scenario graph"))
        root.addView(ChartView(this, result.scenarios))
        root.addView(section("What-if salary ladder"))
        result.scenarios.forEach { s ->
            root.addView(text(money0(s.base) + "  ->  " + signed(s.delta), 16, if (s.delta >= 0) Color.rgb(34, 197, 94) else Color.rgb(239, 68, 68)))
        }
        root.addView(space(16))
        root.addView(text("Defaults: Amazon base 110k, 76 percent take-home, Cap One 70 percent take-home, 18.2k first bonus, 13.5k second bonus, 170 RSUs, 241.70 RSU price, 3.5k relocation repayment.", 13, Color.rgb(156, 163, 175)))
        setContentView(scroll)
    }

    private fun row(): LinearLayout {
        val v = LinearLayout(this)
        v.orientation = LinearLayout.HORIZONTAL
        v.setPadding(0, 8, 0, 8)
        return v
    }

    private fun weight(): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        p.setMargins(8, 8, 8, 8)
        return p
    }

    private fun title(value: String, size: Int, bold: Boolean): TextView {
        val t = text(value, size, Color.WHITE)
        if (bold) t.setTypeface(t.typeface, android.graphics.Typeface.BOLD)
        return t
    }

    private fun section(value: String): TextView {
        val t = title(value, 20, true)
        t.setPadding(0, 28, 0, 8)
        return t
    }

    private fun text(value: String, size: Int, color: Int): TextView {
        val t = TextView(this)
        t.text = value
        t.textSize = size.toFloat()
        t.setTextColor(color)
        t.setPadding(4, 4, 4, 4)
        return t
    }

    private fun space(h: Int): View {
        val v = View(this)
        v.layoutParams = LinearLayout.LayoutParams(1, h)
        return v
    }

    private fun card(label: String, value: String, good: Boolean): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.setPadding(20, 18, 20, 18)
        c.setBackgroundColor(if (good) Color.rgb(16, 42, 36) else Color.rgb(22, 28, 49))
        c.addView(text(label, 12, Color.rgb(156, 163, 175)))
        c.addView(title(value, 20, true))
        return c
    }
}

class ChartView(context: android.content.Context, private val scenarios: List<Scenario>) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 300)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mid = height / 2f
        paint.color = Color.rgb(100, 116, 139)
        paint.strokeWidth = 3f
        canvas.drawLine(0f, mid, width.toFloat(), mid, paint)
        val maxAbs = scenarios.map { abs(it.delta) }.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        val barW = width / (scenarios.size * 1.6f)
        scenarios.forEachIndexed { idx, s ->
            val h = (abs(s.delta) / maxAbs * height * 0.42).toFloat()
            val x = idx * width / scenarios.size + barW * 0.3f
            val top = if (s.delta >= 0) mid - h else mid
            val bottom = if (s.delta >= 0) mid else mid + h
            paint.color = if (s.delta >= 0) Color.rgb(34, 197, 94) else Color.rgb(239, 68, 68)
            canvas.drawRoundRect(x, top, x + barW, bottom, 14f, 14f, paint)
        }
    }
}
