package com.inventorytracker.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.inventorytracker.app.data.local.AppDatabase
import com.inventorytracker.app.data.local.PrepopulateCallback
import com.inventorytracker.app.data.local.dao.CategoryDao
import com.inventorytracker.app.data.local.dao.InventoryLogDao
import com.inventorytracker.app.data.local.dao.ProductDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addCallback(PrepopulateCallback())
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideInventoryLogDao(database: AppDatabase): InventoryLogDao = database.inventoryLogDao()
}
