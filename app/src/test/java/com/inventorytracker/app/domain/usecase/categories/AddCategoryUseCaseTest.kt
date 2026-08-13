package com.inventorytracker.app.domain.usecase.categories

import com.inventorytracker.app.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class AddCategoryUseCaseTest {

    private val repository = mockk<CategoryRepository>()
    private val useCase = AddCategoryUseCase(repository)

    @Test
    fun `valid category is created with normalized name`() = runTest {
        coEvery { repository.isNameAvailable("Electronics", any()) } returns true
        coEvery { repository.addCategory(any()) } returns 42L

        val result = useCase("  Electronics  ", null, "#3F51B5", now = 123L)

        val success = assertInstanceOf(AddCategoryUseCase.Result.Success::class.java, result)
        assertEquals(42L, success.categoryId)
        coVerify {
            repository.addCategory(
                match {
                    it.name == "Electronics" &&
                        it.colorHex == "#3F51B5" &&
                        it.createdAt == 123L &&
                        it.updatedAt == 123L
                }
            )
        }
    }

    @Test
    fun `duplicate name is rejected`() = runTest {
        coEvery { repository.isNameAvailable("Groceries", any()) } returns false

        val result = useCase("Groceries", null, "#4CAF50")

        assertInstanceOf(AddCategoryUseCase.Result.NameAlreadyExists::class.java, result)
        coVerify(exactly = 0) { repository.addCategory(any()) }
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val result = useCase("   ", null, "#808080")

        assertInstanceOf(AddCategoryUseCase.Result.NameAlreadyExists::class.java, result)
    }
}
