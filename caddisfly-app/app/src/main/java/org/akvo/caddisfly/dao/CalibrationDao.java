package org.akvo.caddisfly.dao;

import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CalibrationDao {

    @Query("SELECT * FROM calibration WHERE uid = :uuid ORDER BY value")
    List<Calibration> getAll(String uuid);

    @Query("SELECT * FROM calibrationdetail WHERE uid = :uuid")
    CalibrationDetail getCalibrationDetails(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Calibration calibration);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalibrationDetail calibrationDetail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Calibration> calibrations);

    @Update
    void update(Calibration calibration);

    @Delete
    void delete(Calibration calibration);

    @Query("DELETE FROM calibration WHERE uid = :uuid")
    void deleteCalibrations(String uuid);
}