package com.inventorytracker.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventorytracker.app.R
import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.usecase.dashboard.HomeData
import com.inventorytracker.app.ui.components.CategoryCard
import com.inventorytracker.app.ui.components.EmptyState
import com.inventorytracker.app.ui.components.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (Long) -> Unit,
    onProductClick: (Long) -> Unit,
    onViewAllHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val homeData by viewModel.homeData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.home_greeting)) })
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val data = homeData
            if (data == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                HomeContent(
                    data = data,
                    onCategoryClick = onCategoryClick,
                    onProductClick = onProductClick,
                    onViewAllHistory = onViewAllHistory,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    onCategoryClick: (Long) -> Unit,
    onProductClick: (Long) -> Unit,
    onViewAllHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (data.categories.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.nav_products),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    data.categories.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { entry ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CategoryCard(
                                        category = entry.category,
                                        productCount = entry.productCount,
                                        onClick = { onCategoryClick(entry.category.id) },
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_low_stock),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (data.lowStockProducts.isEmpty()) {
            item {
                EmptyState(
                    message = stringResource(R.string.home_no_low_stock),
                    icon = Icons.Outlined.AddAlert,
                )
            }
        } else {
            items(data.lowStockProducts, key = { it.id }) { product ->
                LowStockRow(product = product, onClick = { onProductClick(product.id) })
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_recent_activity),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onViewAllHistory) {
                    Text(stringResource(R.string.home_view_all))
                }
            }
        }
        if (data.recentActivity.isEmpty()) {
            item {
                EmptyState(
                    message = stringResource(R.string.home_no_recent),
                    icon = Icons.Outlined.History,
                )
            }
        } else {
            items(data.recentActivity, key = { it.log.id }) { entry ->
                RecentActivityRow(
                    productName = entry.product?.name ?: "—",
                    changeText = changeText(entry.log.changeType, entry.log.amount, entry.log.newQuantity),
                    timestamp = entry.log.timestamp,
                )
            }
        }
    }
}

@Composable
private fun LowStockRow(product: Product, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(product.name) },
        supportingContent = {
            Text("${product.quantity} ${product.unit} · ${stringResource(R.string.product_low_stock)}")
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        modifier = Modifier.clickable { onClick() },
    )
}

@Composable
private fun RecentActivityRow(
    productName: String,
    changeText: String,
    timestamp: Long,
) {
    ListItem(
        headlineContent = { Text(productName) },
        supportingContent = {
            Text("$changeText · ${formatTimestamp(timestamp)}")
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

private fun changeText(type: ChangeType, amount: Int, newQuantity: Int): String {
    val sign = when (type) {
        ChangeType.INCREASE -> "+"
        ChangeType.DECREASE -> "-"
        else -> ""
    }
    return "$sign$amount → $newQuantity"
}
