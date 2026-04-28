package com.vn.kaygv.notetaking.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vn.kaygv.notetaking.data.db.dao.FolderDao
import com.vn.kaygv.notetaking.data.db.dao.NoteDao
import com.vn.kaygv.notetaking.data.db.dao.ReminderDao
import com.vn.kaygv.notetaking.data.db.entity.FolderEntity
import com.vn.kaygv.notetaking.data.db.entity.NoteEntity
import com.vn.kaygv.notetaking.data.db.entity.ReminderEntity

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        ReminderEntity::class],
    exportSchema = false,
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun reminderDao(): ReminderDao
}