package com.offerverse.switchlab

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class Scenario(val base: Double, val total: Double, val delta: Double)
data class Result(val stay: Double, val beforeSwitch: Double, val selectedSwitch: Double, val breakEven: Double, val scenarios: List<Scenario>)

object Calc {
    fun run(
        join: LocalDate,
        switchDate: LocalDate,
        end: LocalDate,
        currentBase: Double,
        currentTake: Double,
        newTake: Double,
        firstBonus: Double,
        secondBonus: Double,
        rsuShares: Double,
        rsuPrice: Double,
        relocationReceived: Double,
        relocationRepay: Double,
        newRelocation: Double,
        selectedBase: Double,
        ladder: List<Double>
    ): Result {
        fun years(a: LocalDate, b: LocalDate): Double {
            if (!b.isAfter(a)) return 0.0
            var y = b.year - a.year
            while (a.plusYears(y.toLong()).isAfter(b)) y--
            val anchor = a.plusYears(y.toLong())
            return y + ChronoUnit.DAYS.between(anchor, b).coerceAtLeast(0).toDouble() / 365.0
        }
        fun secondEarned(asOf: LocalDate): Double {
            val s = join.plusYears(1)
            val e = join.plusYears(2)
            if (!asOf.isAfter(s)) return 0.0
            val total = max(1, ChronoUnit.DAYS.between(s, e).toInt())
            val worked = ChronoUnit.DAYS.between(s, minOf(asOf, e)).coerceIn(0, total.toLong())
            return secondBonus * worked / total
        }
        fun rsuNet(asOf: LocalDate): Double {
            val schedule = listOf(
                LocalDate.of(2026, 10, 15) to .05,
                LocalDate.of(2027, 10, 15) to .15,
                LocalDate.of(2028, 4, 15) to .20,
                LocalDate.of(2028, 10, 15) to .20,
                LocalDate.of(2029, 4, 15) to .20,
                LocalDate.of(2029, 10, 15) to .20
            )
            return schedule.filter { !it.first.isAfter(asOf) }.sumOf { rsuShares * it.second * rsuPrice * currentTake }
        }
        fun current(asOf: LocalDate, leaving: Boolean): Double {
            val salaryNet = currentBase * years(join, asOf) * currentTake
            val firstNet = firstBonus * currentTake
            val secondNet = secondEarned(asOf) * currentTake
            val relocationNet = relocationReceived - if (leaving) relocationRepay else 0.0
            return salaryNet + firstNet + secondNet + rsuNet(asOf) + relocationNet
        }
        val stay = current(end, false)
        val before = current(switchDate, true)
        val newYears = years(switchDate, end)
        val scenarios = ladder.distinct().sorted().map { base ->
            val total = before + base * newYears * newTake + newRelocation
            Scenario(base, total, total - stay)
        }
        val selected = before + selectedBase * newYears * newTake + newRelocation
        val breakEven = (stay - before - newRelocation) / (newYears * newTake)
        return Result(stay, before, selected, breakEven, scenarios)
    }
}
