package com.inventorytracker.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inventorytracker.app.data.local.dao.CategoryDao
import com.inventorytracker.app.data.local.dao.InventoryLogDao
import com.inventorytracker.app.data.local.dao.ProductDao
import com.inventorytracker.app.data.local.entity.CategoryEntity
import com.inventorytracker.app.data.local.entity.InventoryLogEntity
import com.inventorytracker.app.data.local.entity.ProductEntity
import com.inventorytracker.app.data.repository.ProductRepositoryImpl
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.ChangeType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var productDao: ProductDao
    private lateinit var logDao: InventoryLogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        categoryDao = database.categoryDao()
        productDao = database.productDao()
        logDao = database.inventoryLogDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndFindProductByBarcode() = runBlocking {
        val categoryId = categoryDao.insert(
            CategoryEntity(name = "Electronics", colorHex = "#3F51B5", createdAt = 1L, updatedAt = 1L)
        )
        val productId = productDao.insert(
            ProductEntity(
                barcode = "9780201379624",
                barcodeFormat = BarcodeFormat.EAN_13,
                name = "Kotlin Book",
                quantity = 10,
                categoryId = categoryId,
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        val found = productDao.getByBarcode("9780201379624")
        assertNotNull(found)
        assertEquals(productId, found!!.id)
        assertEquals("Kotlin Book", found.name)
    }

    @Test
    fun updateQuantityPersistsChange() = runBlocking {
        val productId = productDao.insert(
            ProductEntity(
                barcode = "012345678905",
                barcodeFormat = BarcodeFormat.UPC_A,
                name = "Widget",
                quantity = 5,
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        productDao.updateQuantity(productId, 12, 2L)

        val updated = productDao.getById(productId)!!
        assertEquals(12, updated.quantity)
        assertEquals(2L, updated.updatedAt)
    }

    @Test
    fun adjustQuantityIsAtomicAndLogsEntry() = runBlocking {
        val repository = ProductRepositoryImpl(database, productDao, logDao)
        val productId = productDao.insert(
            ProductEntity(
                barcode = "ABC123",
                barcodeFormat = BarcodeFormat.CODE_128,
                name = "Container",
                quantity = 0,
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        repository.adjustQuantity(
            productId = productId,
            changeType = ChangeType.INCREASE,
            amount = 7,
            previousQuantity = 0,
            newQuantity = 7,
            reason = "receiving",
        )

        val product = productDao.getById(productId)!!
        assertEquals(7, product.quantity)

        val logs = logDao.observeByProduct(productId).first()
        assertEquals(1, logs.size)
        assertEquals(ChangeType.INCREASE, logs[0].changeType)
        assertEquals(7, logs[0].newQuantity)
        assertEquals("receiving", logs[0].reason)
    }

    @Test
    fun deletingCategoryReassignsProducts() = runBlocking {
        val electronicsId = categoryDao.insert(
            CategoryEntity(name = "Electronics", colorHex = "#3F51B5", createdAt = 1L, updatedAt = 1L)
        )
        val uncategorizedId = categoryDao.insert(
            CategoryEntity(name = "Uncategorized", colorHex = "#9E9E9E", createdAt = 1L, updatedAt = 1L)
        )
        val productId = productDao.insert(
            ProductEntity(
                barcode = "QR001",
                barcodeFormat = BarcodeFormat.QR_CODE,
                name = "Gadget",
                categoryId = electronicsId,
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        productDao.reassignCategory(electronicsId, uncategorizedId)
        categoryDao.delete(categoryDao.getById(electronicsId)!!)

        val product = productDao.getById(productId)!!
        assertEquals(uncategorizedId, product.categoryId)
    }

    @Test
    fun searchMatchesNameAndBarcode() = runBlocking {
        productDao.insert(
            ProductEntity(
                barcode = "9780201379624",
                barcodeFormat = BarcodeFormat.EAN_13,
                name = "Kotlin Book",
                createdAt = 1L,
                updatedAt = 1L,
            )
        )
        productDao.insert(
            ProductEntity(
                barcode = "3000012345678",
                barcodeFormat = BarcodeFormat.EAN_13,
                name = "Milk",
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        val byName = productDao.observeSearch("kotlin").first()
        assertEquals(1, byName.size)
        assertEquals("Kotlin Book", byName[0].name)

        val byBarcode = productDao.observeSearch("3000").first()
        assertEquals(1, byBarcode.size)
        assertEquals("Milk", byBarcode[0].name)
    }

    @Test
    fun lowStockQueryReturnsOnlyProductsAtOrBelowThreshold() = runBlocking {
        productDao.insert(
            ProductEntity(
                barcode = "1",
                barcodeFormat = BarcodeFormat.CODE_39,
                name = "Low",
                quantity = 3,
                lowStockThreshold = 5,
                createdAt = 1L,
                updatedAt = 1L,
            )
        )
        productDao.insert(
            ProductEntity(
                barcode = "2",
                barcodeFormat = BarcodeFormat.CODE_39,
                name = "High",
                quantity = 50,
                lowStockThreshold = 5,
                createdAt = 1L,
                updatedAt = 1L,
            )
        )

        val lowStock = productDao.observeLowStock().first()
        assertEquals(1, lowStock.size)
        assertEquals("Low", lowStock[0].name)
    }

    @Test
    fun deletedProductRemovesLogsViaCascade() = runBlocking {
        val repository = ProductRepositoryImpl(database, productDao, logDao)
        val productId = productDao.insert(
            ProductEntity(
                barcode = "CASCADE1",
                barcodeFormat = BarcodeFormat.CODE_128,
                name = "Temp",
                createdAt = 1L,
                updatedAt = 1L,
            )
        )
        repository.adjustQuantity(
            productId = productId,
            changeType = ChangeType.INCREASE,
            amount = 1,
            previousQuantity = 0,
            newQuantity = 1,
            reason = null,
        )
        assertTrue(logDao.observeByProduct(productId).first().isNotEmpty())

        productDao.delete(productDao.getById(productId)!!)

        assertNull(productDao.getById(productId))
        assertTrue(logDao.observeByProduct(productId).first().isEmpty())
    }
}
