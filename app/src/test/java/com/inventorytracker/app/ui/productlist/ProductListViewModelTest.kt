package com.inventorytracker.app.ui.productlist

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import com.inventorytracker.app.MainDispatcherExtension
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ProductListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val categoryRepository = mockk<CategoryRepository>()
    private val productRepository = mockk<ProductRepository>()

    private val categories = listOf(
        Category(id = 1L, name = "Electronics", colorHex = "#3F51B5", createdAt = 0L, updatedAt = 0L),
        Category(id = 2L, name = "Groceries", colorHex = "#4CAF50", createdAt = 0L, updatedAt = 0L),
    )

    private fun product(id: Long, name: String, barcode: String, categoryId: Long?) = Product(
        id = id,
        barcode = barcode,
        barcodeFormat = BarcodeFormat.EAN_13,
        name = name,
        quantity = 0,
        categoryId = categoryId,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private val products = listOf(
        product(1L, "Banana", "100", 2L),
        product(2L, "Laptop", "200", 1L),
        product(3L, "Milk", "300", 2L),
    )

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        ProductListViewModel(
            savedStateHandle = savedStateHandle,
            productRepository = productRepository,
            categoryRepository = categoryRepository,
        )

    private fun stubRepositories() {
        every { productRepository.observeAll() } returns flowOf(products)
        every { categoryRepository.observeAll() } returns flowOf(categories)
    }

    @Test
    fun `emits all products and categories`() = runTest(mainDispatcher.dispatcher) {
        stubRepositories()

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1) // initial empty state
            val state = awaitItem()
            assertEquals(3, state.totalCount)
            assertEquals(2, state.categories.size)
            assertEquals("Banana", state.items.first().product.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filters by name case-insensitively`() = runTest(mainDispatcher.dispatcher) {
        stubRepositories()

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onSearchQueryChange("ban")
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(1, state.totalCount)
            assertEquals("Banana", state.items.single().product.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `category filter narrows results`() = runTest(mainDispatcher.dispatcher) {
        stubRepositories()

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onCategoryFilterChange(1L)
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(1, state.totalCount)
            assertEquals("Laptop", state.items.single().product.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial categoryId from nav args filters products`() = runTest(mainDispatcher.dispatcher) {
        stubRepositories()

        val vm = viewModel(SavedStateHandle(mapOf("categoryId" to 2L)))
        vm.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(2, state.totalCount)
            assertTrue(state.items.all { it.product.categoryId == 2L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sort by quantity orders ascending`() = runTest(mainDispatcher.dispatcher) {
        val productsWithQty = listOf(
            product(1L, "Banana", "100", null).copy(quantity = 9),
            product(2L, "Laptop", "200", null).copy(quantity = 2),
            product(3L, "Milk", "300", null).copy(quantity = 5),
        )
        every { productRepository.observeAll() } returns flowOf(productsWithQty)
        every { categoryRepository.observeAll() } returns flowOf(emptyList())

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onSortOrderChange(SortOrder.QUANTITY)
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(listOf(2, 5, 9), state.items.map { it.product.quantity })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
