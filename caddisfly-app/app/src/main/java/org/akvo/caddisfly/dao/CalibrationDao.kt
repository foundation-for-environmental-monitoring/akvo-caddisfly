package org.akvo.caddisfly.dao

import androidx.room.*
import org.akvo.caddisfly.entity.Calibration
import org.akvo.caddisfly.entity.CalibrationDetail

@Dao
interface CalibrationDao {
    @Query("SELECT * FROM calibration WHERE uid = :uuid ORDER BY value")
    fun getAll(uuid: String?): List<Calibration?>?

    @Query("SELECT * FROM calibrationdetail WHERE uid = :uuid")
    fun getCalibrationDetails(uuid: String?): CalibrationDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calibration: Calibration?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calibrationDetail: CalibrationDetail?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(calibrations: List<Calibration?>?)

    @Update
    fun update(calibration: Calibration?)

    @Delete
    fun delete(calibration: Calibration?)

    @Query("DELETE FROM calibration WHERE uid = :uuid")
    fun deleteCalibrations(uuid: String?)
}