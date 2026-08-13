package com.inventorytracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.inventorytracker.app.ui.addeditproduct.AddEditProductScreen
import com.inventorytracker.app.ui.categories.CategoryManagementScreen
import com.inventorytracker.app.ui.history.InventoryHistoryScreen
import com.inventorytracker.app.ui.home.HomeScreen
import com.inventorytracker.app.ui.productdetail.ProductDetailScreen
import com.inventorytracker.app.ui.productlist.ProductListScreen
import com.inventorytracker.app.ui.scanner.ScannerScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate("${Routes.PRODUCTS}?categoryId=$categoryId")
                },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetail(productId))
                },
                onViewAllHistory = { navController.navigate(Routes.HISTORY) },
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                onProductFound = { productId ->
                    navController.navigate(Routes.productDetail(productId))
                },
                onNewBarcode = { barcode, format ->
                    navController.navigate(
                        "${Routes.ADD_EDIT_PRODUCT}?barcode=$barcode&format=${format.name}"
                    )
                },
            )
        }

        composable(
            route = "${Routes.PRODUCTS}?categoryId={categoryId}",
            arguments = listOf(
                navArgument("categoryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            ProductListScreen(
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetail(productId))
                },
                onAddProduct = { navController.navigate(Routes.ADD_EDIT_PRODUCT) },
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.LongType },
            ),
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onEditProduct = { productId ->
                    navController.navigate(
                        "${Routes.ADD_EDIT_PRODUCT}?productId=$productId"
                    )
                },
            )
        }

        composable(Routes.CATEGORIES) {
            CategoryManagementScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            InventoryHistoryScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetail(productId))
                },
            )
        }

        composable(
            route = "${Routes.ADD_EDIT_PRODUCT}?barcode={barcode}&format={format}&productId={productId}",
            arguments = listOf(
                navArgument("barcode") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("format") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            AddEditProductScreen(
                onBack = { navController.popBackStack() },
                onSaved = { productId ->
                    navController.popBackStack()
                    navController.navigate(Routes.productDetail(productId))
                },
            )
        }
    }
}
