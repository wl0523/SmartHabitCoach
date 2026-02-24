package com.example.smarthabitcoach.data.di

// Data module previously provided Hilt bindings for Room. To avoid cross-module annotation processing order issues
// we keep implementation here but move Hilt @Provides to the app module where Hilt is centrally processed.

// The concrete Room classes remain in this module (HabitDatabase, HabitDao). Providers live in :app.

import android.content.Context
import androidx.room.Room
import com.example.smarthabitcoach.data.local.HabitDatabase
import com.example.smarthabitcoach.data.local.HabitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(context, HabitDatabase::class.java, "habits.db").build()

    @Provides
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()
}
