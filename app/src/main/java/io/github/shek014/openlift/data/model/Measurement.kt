package io.github.shek014.openlift.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

enum class LengthUnit(val label: String, val centimetersPerUnit: Double) {
    Cm("cm", 1.0),
    In("in", 2.54),
}

enum class MeasurementKind { Weight, Length, Percent }

/** These names are saved in the database, so renaming one needs a database migration. */
enum class MeasurementType(val label: String, val kind: MeasurementKind) {
    BodyWeight("Body weight", MeasurementKind.Weight),
    Height("Height", MeasurementKind.Length),
    BodyFat("Body fat", MeasurementKind.Percent),
    Neck("Neck", MeasurementKind.Length),
    Shoulders("Shoulders", MeasurementKind.Length),
    Chest("Chest", MeasurementKind.Length),
    Waist("Waist", MeasurementKind.Length),
    Hips("Hips", MeasurementKind.Length),
    LeftArm("Left arm", MeasurementKind.Length),
    RightArm("Right arm", MeasurementKind.Length),
    LeftForearm("Left forearm", MeasurementKind.Length),
    RightForearm("Right forearm", MeasurementKind.Length),
    LeftThigh("Left thigh", MeasurementKind.Length),
    RightThigh("Right thigh", MeasurementKind.Length),
    LeftCalf("Left calf", MeasurementKind.Length),
    RightCalf("Right calf", MeasurementKind.Length),
}

/** A measurement, stored in kilograms, centimeters or percent, whatever's being shown. */
data class Measurement(
    val id: Long,
    val type: MeasurementType,
    val value: Double,
    val measuredAt: Long,
)

/** The units to show measurements in. */
data class MeasurementUnits(val weight: WeightUnit, val length: LengthUnit)

fun MeasurementType.unitLabel(units: MeasurementUnits): String = when (kind) {
    MeasurementKind.Weight -> units.weight.label
    MeasurementKind.Length -> units.length.label
    MeasurementKind.Percent -> "%"
}

/** Converts a stored value to the unit it's shown in. */
fun MeasurementType.toShown(value: Double, units: MeasurementUnits): Double = when (kind) {
    MeasurementKind.Weight -> value / units.weight.kilogramsPerUnit
    MeasurementKind.Length -> value / units.length.centimetersPerUnit
    MeasurementKind.Percent -> value
}

/** Converts what someone typed in the shown unit to how it's stored. */
fun MeasurementType.toStored(shown: Double, units: MeasurementUnits): Double = when (kind) {
    MeasurementKind.Weight -> shown * units.weight.kilogramsPerUnit
    MeasurementKind.Length -> shown * units.length.centimetersPerUnit
    MeasurementKind.Percent -> shown
}

/** The number alone in the shown unit, such as "82.5". */
fun MeasurementType.formatNumber(value: Double, units: MeasurementUnits): String =
    formatWeight(toShown(value, units))

/** A stored value as it reads on screen, such as "82.5 kg" or "18%". */
fun MeasurementType.format(value: Double, units: MeasurementUnits): String {
    val number = formatNumber(value, units)
    return if (kind == MeasurementKind.Percent) "$number%" else "$number ${unitLabel(units)}"
}

/** The difference between two stored values, such as "+1.5 kg". */
fun MeasurementType.formatChange(latest: Double, previous: Double, units: MeasurementUnits): String {
    val change = toShown(latest, units) - toShown(previous, units)
    val rounded = Math.round(change * 100) / 100.0
    val sign = if (rounded > 0) "+" else if (rounded < 0) "-" else ""
    val number = formatWeight(kotlin.math.abs(rounded))
    return if (kind == MeasurementKind.Percent) "$sign$number%" else "$sign$number ${unitLabel(units)}"
}

data class LatestMeasurement(val latest: Measurement, val previous: Measurement?)

/** The newest entry for each type that has any, with the one before it for comparing. */
fun latestMeasurements(measurements: List<Measurement>): Map<MeasurementType, LatestMeasurement> =
    measurements
        .groupBy { it.type }
        .mapValues { (_, entries) ->
            val newestFirst = entries.sortedWith(compareByDescending<Measurement> { it.measuredAt }.thenByDescending { it.id })
            LatestMeasurement(newestFirst.first(), newestFirst.getOrNull(1))
        }

/** Weight in kilograms divided by height in meters squared. */
fun bodyMassIndex(weightKg: Double, heightCm: Double): Double? =
    if (weightKg <= 0 || heightCm <= 0) null else weightKg / ((heightCm / 100) * (heightCm / 100))

/** A value someone can sensibly have typed. */
fun isValidMeasurement(value: Double?): Boolean = value != null && value > 0

/**
 * Turns the date picker's result, which is midnight UTC on the chosen day, into midday on that day
 * where the person is, so that the date reads correctly wherever they later view it.
 */
fun pickedDateToMillis(utcMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        .atTime(12, 0)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

/** A date as the date picker wants it: midnight UTC on that day. */
fun dateToPickerMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
