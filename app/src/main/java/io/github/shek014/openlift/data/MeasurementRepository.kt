package io.github.shek014.openlift.data

import io.github.shek014.openlift.data.local.MeasurementDao
import io.github.shek014.openlift.data.local.MeasurementEntity
import io.github.shek014.openlift.data.model.Measurement
import io.github.shek014.openlift.data.model.MeasurementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepository(private val dao: MeasurementDao) {
    val measurements: Flow<List<Measurement>> = dao.observeAll().map { rows -> rows.map { it.toMeasurement() } }

    /** This type's entries, newest first. */
    fun measurements(type: MeasurementType): Flow<List<Measurement>> =
        dao.observeType(type).map { rows -> rows.map { it.toMeasurement() } }

    suspend fun add(type: MeasurementType, value: Double, measuredAt: Long) =
        dao.insert(MeasurementEntity(type = type, value = value, measuredAt = measuredAt))

    suspend fun delete(id: Long) = dao.delete(id)
}

private fun MeasurementEntity.toMeasurement() = Measurement(id, type, value, measuredAt)
