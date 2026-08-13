package com.inventorytracker.app.domain.usecase.history

import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllInventoryLogsUseCase @Inject constructor(
    private val repository: InventoryLogRepository,
) {
    operator fun invoke(): Flow<List<InventoryLogWithProduct>> = repository.observeAll()
}
