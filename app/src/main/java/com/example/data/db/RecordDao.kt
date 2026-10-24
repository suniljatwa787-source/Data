package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DataRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM data_records ORDER BY updatedAt DESC")
    fun getAllRecords(): Flow<List<DataRecord>>

    @Query("SELECT * FROM data_records WHERE id = :id LIMIT 1")
    fun getRecordById(id: Long): Flow<DataRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DataRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<DataRecord>)

    @Update
    suspend fun updateRecord(record: DataRecord)

    @Delete
    suspend fun deleteRecord(record: DataRecord)

    @Query("DELETE FROM data_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM data_records WHERE id IN (:ids)")
    suspend fun deleteMultipleByIds(ids: List<Long>)

    @Query("DELETE FROM data_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM data_records")
    suspend fun getCount(): Int
}
