package com.inventorytracker.app.domain.usecase.categories

import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class DeleteCategoryUseCaseTest {

    private val repository = mockk<CategoryRepository>()
    private val useCase = DeleteCategoryUseCase(repository)

    private fun category(id: Long, name: String) = Category(
        id = id,
        name = name,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `deleting a regular category reassigns and deletes`() = runTest {
        coEvery { repository.getById(3L) } returns category(3L, "Electronics")
        coEvery { repository.deleteCategory(3L) } returns Unit

        val result = useCase(3L)

        assertInstanceOf(DeleteCategoryUseCase.Result.Success::class.java, result)
        coVerify(exactly = 1) { repository.deleteCategory(3L) }
    }

    @Test
    fun `uncategorized cannot be deleted`() = runTest {
        coEvery { repository.getById(1L) } returns category(1L, "Uncategorized")

        val result = useCase(1L)

        assertInstanceOf(DeleteCategoryUseCase.Result.CannotDeleteDefault::class.java, result)
        coVerify(exactly = 0) { repository.deleteCategory(any()) }
    }

    @Test
    fun `missing category returns not found`() = runTest {
        coEvery { repository.getById(9L) } returns null

        val result = useCase(9L)

        assertInstanceOf(DeleteCategoryUseCase.Result.NotFound::class.java, result)
    }
}
