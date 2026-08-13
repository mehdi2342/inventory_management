package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class SetInventoryUseCaseTest {

    private val repository = mockk<ProductRepository>()
    private val useCase = SetInventoryUseCase(repository)

    private fun product(quantity: Int) = Product(
        id = 7L,
        barcode = "012345678905",
        barcodeFormat = BarcodeFormat.UPC_A,
        name = "Widget",
        quantity = quantity,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `set absolute quantity records SET log`() = runTest {
        coEvery { repository.getById(7L) } returns product(3)
        coEvery { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) } returns Unit

        val result = useCase(7L, 25, reason = "stocktake")

        val success = assertInstanceOf(SetInventoryUseCase.Result.Success::class.java, result)
        assertEquals(25, success.product.quantity)
        coVerify(exactly = 1) {
            repository.adjustQuantity(
                productId = 7L,
                changeType = ChangeType.SET,
                amount = 22,
                previousQuantity = 3,
                newQuantity = 25,
                reason = "stocktake",
            )
        }
    }

    @Test
    fun `negative set is blocked`() = runTest {
        coEvery { repository.getById(7L) } returns product(3)

        val result = useCase(7L, -1)

        assertInstanceOf(SetInventoryUseCase.Result.NegativeStock::class.java, result)
    }

    @Test
    fun `overflow set is blocked`() = runTest {
        coEvery { repository.getById(7L) } returns product(1)

        val result = useCase(7L, 10_000_000)

        assertInstanceOf(SetInventoryUseCase.Result.QuantityOverflow::class.java, result)
    }

    @Test
    fun `missing product aborts`() = runTest {
        coEvery { repository.getById(7L) } returns null

        val result = useCase(7L, 10)

        assertInstanceOf(SetInventoryUseCase.Result.ProductNotFound::class.java, result)
    }
}
