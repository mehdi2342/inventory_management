package com.inventorytracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inventorytracker.app.R
import com.inventorytracker.app.ui.navigation.AppNavHost
import com.inventorytracker.app.ui.navigation.Routes
import com.inventorytracker.app.ui.theme.InventoryTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryTrackerTheme {
                MainScreen()
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Routes.SCANNER, R.string.nav_scanner, Icons.Filled.QrCodeScanner),
    BottomNavItem(Routes.PRODUCTS, R.string.nav_products, Icons.Filled.Inventory2),
)

@Composable
private fun MainScreen(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = bottomNavItems.any { item -> currentDestination.matches(item) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AppNavHost(navController = navController)
        }
        if (showBottomBar) {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination.matches(item),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            }
        }
    }
}

/**
 * Matches a destination against a bottom-nav item, ignoring optional query
 * arguments (e.g. "products?categoryId={categoryId}" matches "products").
 */
private fun androidx.navigation.NavDestination?.matches(item: BottomNavItem): Boolean {
    val route = this?.hierarchy?.any { dest ->
        dest.route?.let { r -> r == item.route || r.startsWith("${item.route}?") } == true
    } ?: false
    return route
}
