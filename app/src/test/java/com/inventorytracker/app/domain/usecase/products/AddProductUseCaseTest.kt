package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class AddProductUseCaseTest {

    private val repository = mockk<ProductRepository>()
    private val useCase = AddProductUseCase(repository)

    @Test
    fun `new barcode creates product with normalized fields`() = runTest {
        coEvery { repository.getByBarcode("9780201379624") } returns null
        coEvery { repository.addProduct(any()) } returns 5L

        val result = useCase(
            barcode = " 9780201379624 ",
            barcodeFormat = BarcodeFormat.EAN_13,
            name = "  Kotlin Book  ",
            description = "A great read",
            quantity = 3,
            lowStockThreshold = 2,
            unit = "pcs",
            now = 999L,
        )

        val success = assertInstanceOf(AddProductUseCase.Result.Success::class.java, result)
        assertEquals(5L, success.productId)
        coEvery { repository.addProduct(any()) }
    }

    @Test
    fun `duplicate barcode is rejected`() = runTest {
        val existing = Product(
            id = 1L,
            barcode = "9780201379624",
            barcodeFormat = BarcodeFormat.EAN_13,
            name = "Book",
            createdAt = 0L,
            updatedAt = 0L,
        )
        coEvery { repository.getByBarcode("9780201379624") } returns existing

        val result = useCase(
            barcode = "9780201379624",
            barcodeFormat = BarcodeFormat.EAN_13,
            name = "Duplicate",
        )

        assertInstanceOf(AddProductUseCase.Result.BarcodeAlreadyExists::class.java, result)
    }

    @Test
    fun `blank name is rejected`() = runTest {
        coEvery { repository.getByBarcode("12345") } returns null

        val result = useCase(
            barcode = "12345",
            barcodeFormat = BarcodeFormat.CODE_128,
            name = "   ",
        )

        assertInstanceOf(AddProductUseCase.Result.BarcodeAlreadyExists::class.java, result)
    }
}
