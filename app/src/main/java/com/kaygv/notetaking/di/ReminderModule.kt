package com.kaygv.notetaking.di

import com.kaygv.notetaking.domain.reminder.ReminderScheduler
import com.kaygv.notetaking.domain.reminder.ReminderSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds
    abstract fun bindReminderScheduler(
        impl: ReminderSchedulerImpl
    ): ReminderScheduler
}