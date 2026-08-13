package com.inventorytracker.app.data.mapper

import com.inventorytracker.app.data.local.entity.CategoryEntity
import com.inventorytracker.app.data.local.entity.InventoryLogEntity
import com.inventorytracker.app.data.local.entity.ProductEntity
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.model.InventoryLog
import com.inventorytracker.app.domain.model.Product

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    barcode = barcode,
    barcodeFormat = barcodeFormat,
    name = name,
    description = description,
    quantity = quantity,
    categoryId = categoryId,
    lowStockThreshold = lowStockThreshold,
    unit = unit,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    barcode = barcode,
    barcodeFormat = barcodeFormat,
    name = name,
    description = description,
    quantity = quantity,
    categoryId = categoryId,
    lowStockThreshold = lowStockThreshold,
    unit = unit,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun InventoryLogEntity.toDomain(): InventoryLog = InventoryLog(
    id = id,
    productId = productId,
    changeType = changeType,
    amount = amount,
    previousQuantity = previousQuantity,
    newQuantity = newQuantity,
    reason = reason,
    timestamp = timestamp,
)

fun InventoryLog.toEntity(): InventoryLogEntity = InventoryLogEntity(
    id = id,
    productId = productId,
    changeType = changeType,
    amount = amount,
    previousQuantity = previousQuantity,
    newQuantity = newQuantity,
    reason = reason,
    timestamp = timestamp,
)
