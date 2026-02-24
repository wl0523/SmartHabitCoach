package com.example.smarthabitcoach.di

import android.content.Context
import androidx.room.Room
import com.example.smarthabitcoach.data.local.HabitDao
import com.example.smarthabitcoach.data.local.HabitDatabase
import com.example.smarthabitcoach.data.repository.HabitRepositoryImpl
import com.example.smarthabitcoach.domain.repository.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindsModule {
    @Binds
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvidesModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(context, HabitDatabase::class.java, "habits.db").build()

    @Provides
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()

    @Provides
    @Singleton
    fun provideHabitRepository(dao: HabitDao): HabitRepositoryImpl = HabitRepositoryImpl(dao)
}
