package com.example.healthyhabits.di

import android.content.Context
import androidx.room.Room
import com.example.healthyhabits.data.HabitDao
import com.example.healthyhabits.data.HabitDatabase
import com.example.healthyhabits.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1) Осигуряваме инстанция на базата данни
    @Provides
    @Singleton
    fun provideHabitDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "healthy_habits_db"   // име на файла за базата
        ).build()
    }

    // 2) Осигуряваме DAO-то
    @Provides
    @Singleton
    fun provideHabitDao(
        db: HabitDatabase
    ): HabitDao = db.habitDao()

    // 3) Осигуряваме Repository-то
    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao
    ): HabitRepository = HabitRepository(habitDao)
}
