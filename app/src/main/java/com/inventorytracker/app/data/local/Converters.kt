package com.inventorytracker.app.data.local

import androidx.room.TypeConverter
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.ChangeType

class Converters {

    @TypeConverter
    fun barcodeFormatToString(format: BarcodeFormat): String = format.name

    @TypeConverter
    fun stringToBarcodeFormat(value: String): BarcodeFormat =
        BarcodeFormat.entries.firstOrNull { it.name == value } ?: BarcodeFormat.UNKNOWN

    @TypeConverter
    fun changeTypeToString(type: ChangeType): String = type.name

    @TypeConverter
    fun stringToChangeType(value: String): ChangeType =
        ChangeType.entries.firstOrNull { it.name == value } ?: ChangeType.SET
}
