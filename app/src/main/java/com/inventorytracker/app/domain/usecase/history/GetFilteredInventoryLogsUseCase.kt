package com.inventorytracker.app.domain.usecase.history

import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Filtered history by time range. A null bound means unbounded on that side.
 */
class GetFilteredInventoryLogsUseCase @Inject constructor(
    private val repository: InventoryLogRepository,
) {
    operator fun invoke(
        startTime: Long?,
        endTime: Long?,
    ): Flow<List<InventoryLogWithProduct>> = when {
        startTime != null && endTime != null -> repository.observeByTimeRange(startTime, endTime)
        else -> repository.observeAll()
    }
}
