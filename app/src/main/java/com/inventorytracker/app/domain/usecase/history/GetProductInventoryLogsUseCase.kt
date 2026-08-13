package com.inventorytracker.app.domain.usecase.history

import com.inventorytracker.app.domain.model.InventoryLog
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductInventoryLogsUseCase @Inject constructor(
    private val repository: InventoryLogRepository,
) {
    operator fun invoke(productId: Long): Flow<List<InventoryLog>> =
        repository.observeByProduct(productId)
}
