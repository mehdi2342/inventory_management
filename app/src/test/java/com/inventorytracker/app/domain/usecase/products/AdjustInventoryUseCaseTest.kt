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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdjustInventoryUseCaseTest {

    private val repository = mockk<ProductRepository>()
    private val useCase = AdjustInventoryUseCase(repository)

    private fun product(quantity: Int) = Product(
        id = 1L,
        barcode = "9780201379624",
        barcodeFormat = BarcodeFormat.EAN_13,
        name = "Book",
        quantity = quantity,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `increase updates quantity and records INCREASE log`() = runTest {
        coEvery { repository.getById(1L) } returns product(10)
        coEvery { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) } returns Unit

        val result = useCase(1L, 5, reason = "restock")

        val success = assertInstanceOf(AdjustInventoryUseCase.Result.Success::class.java, result)
        assertEquals(15, success.product.quantity)
        coVerify(exactly = 1) {
            repository.adjustQuantity(
                productId = 1L,
                changeType = ChangeType.INCREASE,
                amount = 5,
                previousQuantity = 10,
                newQuantity = 15,
                reason = "restock",
            )
        }
    }

    @Test
    fun `decrease records DECREASE log`() = runTest {
        coEvery { repository.getById(1L) } returns product(10)
        coEvery { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) } returns Unit

        val result = useCase(1L, -4)

        val success = assertInstanceOf(AdjustInventoryUseCase.Result.Success::class.java, result)
        assertEquals(6, success.product.quantity)
        coVerify(exactly = 1) {
            repository.adjustQuantity(
                productId = 1L,
                changeType = ChangeType.DECREASE,
                amount = 4,
                previousQuantity = 10,
                newQuantity = 6,
                reason = null,
            )
        }
    }

    @Test
    fun `zero amount is silently ignored without creating a log`() = runTest {
        coEvery { repository.getById(1L) } returns product(10)

        val result = useCase(1L, 0)

        assertInstanceOf(AdjustInventoryUseCase.Result.ZeroAdjustment::class.java, result)
        coVerify(exactly = 0) { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `negative stock is blocked`() = runTest {
        coEvery { repository.getById(1L) } returns product(2)

        val result = useCase(1L, -5)

        assertInstanceOf(AdjustInventoryUseCase.Result.NegativeStock::class.java, result)
        coVerify(exactly = 0) { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `exactly zero after decrease is allowed`() = runTest {
        coEvery { repository.getById(1L) } returns product(5)
        coEvery { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) } returns Unit

        val result = useCase(1L, -5)

        assertInstanceOf(AdjustInventoryUseCase.Result.Success::class.java, result)
    }

    @Test
    fun `quantity overflow is blocked`() = runTest {
        coEvery { repository.getById(1L) } returns product(9_999_999)

        val result = useCase(1L, 1)

        assertInstanceOf(AdjustInventoryUseCase.Result.QuantityOverflow::class.java, result)
    }

    @Test
    fun `missing product aborts with error`() = runTest {
        coEvery { repository.getById(1L) } returns null

        val result = useCase(1L, 5)

        assertInstanceOf(AdjustInventoryUseCase.Result.ProductNotFound::class.java, result)
        coVerify(exactly = 0) { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `adjustment is idempotent to repeated calls`() = runTest {
        coEvery { repository.getById(1L) } returnsMany listOf(product(10), product(15))
        coEvery { repository.adjustQuantity(any(), any(), any(), any(), any(), any()) } returns Unit

        val first = useCase(1L, 5) as AdjustInventoryUseCase.Result.Success
        val second = useCase(1L, 5) as AdjustInventoryUseCase.Result.Success

        assertEquals(15, first.product.quantity)
        assertEquals(20, second.product.quantity)
        assertTrue(first.product.quantity < second.product.quantity)
    }
}
