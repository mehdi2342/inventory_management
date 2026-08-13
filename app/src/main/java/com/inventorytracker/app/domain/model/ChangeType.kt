package com.inventorytracker.app.domain.model

/**
 * The kind of inventory adjustment recorded in an [InventoryLog].
 */
enum class ChangeType {
    INCREASE,
    DECREASE,
    SET,
    UNDO,
}
