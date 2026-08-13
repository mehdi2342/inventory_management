package com.inventorytracker.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val PRODUCTS = "products"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CATEGORIES = "categories"
    const val HISTORY = "history"
    const val ADD_EDIT_PRODUCT = "addEditProduct"

    const val ARG_PRODUCT_ID = "productId"
    const val ARG_BARCODE = "barcode"
    const val ARG_BARCODE_FORMAT = "format"

    fun productDetail(productId: Long): String = "product/$productId"
}
