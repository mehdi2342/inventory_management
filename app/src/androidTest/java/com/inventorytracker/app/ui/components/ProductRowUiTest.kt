package com.inventorytracker.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.ui.theme.InventoryTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ProductRowUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val product = Product(
        id = 1L,
        barcode = "9780201379624",
        barcodeFormat = BarcodeFormat.EAN_13,
        name = "Kotlin Book",
        quantity = 4,
        lowStockThreshold = 5,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun productRowDisplaysNameBarcodeAndQuantity() {
        composeRule.setContent {
            InventoryTrackerTheme {
                ProductRow(
                    product = product,
                    categoryColor = 0xFF3F51B5,
                    onClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Kotlin Book").assertIsDisplayed()
        composeRule.onNodeWithText("9780201379624 · EAN-13").assertIsDisplayed()
        composeRule.onNodeWithText("4").assertIsDisplayed()
    }

    @Test
    fun productRowClickInvokesCallback() {
        var clicked = false
        composeRule.setContent {
            InventoryTrackerTheme {
                ProductRow(
                    product = product,
                    categoryColor = 0xFF3F51B5,
                    onClick = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("product_row_1").performClick()
        composeRule.waitForIdle()
        assert(clicked)
    }
}
