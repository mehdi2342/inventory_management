package com.inventorytracker.app.di

import com.inventorytracker.app.data.repository.CategoryRepositoryImpl
import com.inventorytracker.app.data.repository.InventoryLogRepositoryImpl
import com.inventorytracker.app.data.repository.ProductRepositoryImpl
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindInventoryLogRepository(impl: InventoryLogRepositoryImpl): InventoryLogRepository
}
