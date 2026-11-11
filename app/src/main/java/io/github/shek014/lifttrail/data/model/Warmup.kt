package io.github.shek014.lifttrail.data.model

import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToLong

/** One warm-up set. The weight is in the unit being shown, not necessarily kilograms. */
data class WarmupSet(val weight: Double, val reps: Int)

/** The percentages of the working weight to warm up through, with reps that get fewer as they get heavier. */
private val WarmupSteps = listOf(0.4 to 5, 0.6 to 3, 0.8 to 2)

/** The smallest jump in weight that's practical to load. */
fun WeightUnit.warmupIncrement(): Double = if (this == WeightUnit.Kg) 2.5 else 5.0

/** The usual weight of a bar, in this unit. */
fun WeightUnit.defaultBarWeight(): Double = if (this == WeightUnit.Kg) 20.0 else 45.0

/** Whether this equipment is loaded with plates on a bar. */
val Equipment.usesBar: Boolean
    get() = this == Equipment.Barbell || this == Equipment.SmithMachine || this == Equipment.EzBar

/**
 * The warm-up sets leading up to [workingWeight], lightest first. With a bar they start with the
 * empty bar. Anything too close to the working weight, or too light to load, gets left out.
 */
fun warmupSets(workingWeight: Double, increment: Double, barWeight: Double?): List<WarmupSet> {
    if (workingWeight <= 0) return emptyList()
    val lightest = barWeight ?: 0.0
    val steps = WarmupSteps.mapNotNull { (share, reps) ->
        val weight = (workingWeight * share / increment).roundToLong() * increment
        WarmupSet(weight, reps).takeIf { weight > lightest && weight < workingWeight }
    }.distinctBy { it.weight }
    val emptyBar = barWeight?.takeIf { it > 0 && it < workingWeight }?.let { WarmupSet(it, 10) }
    return listOfNotNull(emptyBar) + steps
}

/** The plates this unit's gyms usually have, heaviest first. */
private fun WeightUnit.plates(): List<Double> =
    if (this == WeightUnit.Kg) {
        listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    } else {
        listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
    }

/**
 * The plates to put on each side of the bar to reach [weight], heaviest first. If it can't be
 * matched exactly, this gets as close as it can without going over.
 */
fun platesPerSide(weight: Double, barWeight: Double, unit: WeightUnit): List<Double> {
    var remaining = (weight - barWeight) / 2
    if (remaining <= 0) return emptyList()
    return buildList {
        unit.plates().forEach { plate ->
            val count = floor((remaining + 0.0001) / plate).toInt()
            repeat(count) { add(plate) }
            remaining -= count * plate
        }
    }
}

/** "20 + 10 + 2.5", or "Empty bar" when there's nothing to load. */
fun formatPlates(plates: List<Double>): String =
    if (plates.isEmpty()) {
        "Empty bar"
    } else {
        plates.joinToString(" + ") { String.format(Locale.ROOT, "%.2f", it).trimEnd('0').trimEnd('.') }
    }
