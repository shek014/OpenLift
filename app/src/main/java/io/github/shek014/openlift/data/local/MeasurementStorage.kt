package io.github.shek014.openlift.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import io.github.shek014.openlift.data.model.MeasurementType
import kotlinx.coroutines.flow.Flow

/** One body measurement, stored in kilograms, centimeters or percent depending on its type. */
@Entity(tableName = "body_measurements", indices = [Index("type")])
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: MeasurementType,
    val value: Double,
    val measuredAt: Long,
)

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY measuredAt DESC, id DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE type = :type ORDER BY measuredAt DESC, id DESC")
    fun observeType(type: MeasurementType): Flow<List<MeasurementEntity>>

    @Insert
    suspend fun insert(measurement: MeasurementEntity)

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun delete(id: Long)
}
