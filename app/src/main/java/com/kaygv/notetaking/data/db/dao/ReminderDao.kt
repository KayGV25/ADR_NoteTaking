package com.kaygv.notetaking.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kaygv.notetaking.data.db.entity.ReminderEntity

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE noteId = :noteId LIMIT 1")
    suspend fun getReminder(noteId: Long): ReminderEntity?

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteReminder(noteId: Long)

    @Insert
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

}