package com.kaygv.notetaking.di

import android.content.Context
import androidx.room.Room
import com.kaygv.notetaking.data.db.AppDatabase
import com.kaygv.notetaking.data.db.dao.FolderDao
import com.kaygv.notetaking.data.db.dao.NoteDao
import com.kaygv.notetaking.data.db.dao.ReminderDao
import com.kaygv.notetaking.domain.reminder.ReminderScheduler
import com.kaygv.notetaking.domain.repository.FolderRepository
import com.kaygv.notetaking.domain.repository.FolderRepositoryImpl
import com.kaygv.notetaking.domain.repository.NoteRepository
import com.kaygv.notetaking.domain.repository.NoteRepositoryImpl
import com.kaygv.notetaking.domain.repository.ReminderRepository
import com.kaygv.notetaking.domain.repository.ReminderRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notes.db"
        ).build()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase) = db.noteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao
    ): NoteRepository {
        return NoteRepositoryImpl(noteDao)
    }

    @Provides
    fun provideFolderDao(db: AppDatabase) = db.folderDao()

    @Provides
    @Singleton
    fun provideFolderRepository(
        folderDao: FolderDao
    ): FolderRepository {
        return FolderRepositoryImpl(folderDao)
    }

    @Provides
    fun provideReminderDao(db: AppDatabase) = db.reminderDao()

    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderDao: ReminderDao,
        scheduler: ReminderScheduler
    ): ReminderRepository {
        return ReminderRepositoryImpl(reminderDao, scheduler)
    }
}