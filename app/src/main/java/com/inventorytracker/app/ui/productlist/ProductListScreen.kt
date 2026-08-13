package com.inventorytracker.app.ui.productlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventorytracker.app.R
import com.inventorytracker.app.ui.components.EmptyState
import com.inventorytracker.app.ui.components.ProductRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onProductClick: (Long) -> Unit,
    onAddProduct: () -> Unit,
    onManageCategories: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.products_title)) },
                actions = {
                    IconButton(onClick = onManageCategories) {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = stringResource(R.string.categories_manage),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.products_add)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
            )
            CategoryFilterRow(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onSelect = viewModel::onCategoryFilterChange,
            )
            SortBar(
                sortOrder = uiState.sortOrder,
                onSortChange = viewModel::onSortOrderChange,
            )

            if (uiState.items.isEmpty()) {
                EmptyState(
                    message = if (uiState.searchQuery.isNotBlank()) {
                        stringResource(R.string.products_no_result)
                    } else {
                        stringResource(R.string.products_empty)
                    },
                    icon = Icons.Outlined.Inventory2,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.items, key = { it.product.id }) { item ->
                        ProductRow(
                            product = item.product,
                            categoryColor = item.categoryColor,
                            onClick = { onProductClick(item.product.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    categories: List<com.inventorytracker.app.domain.model.Category>,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.products_all_categories)) },
            )
        }
        items(categories.size) { index ->
            val category = categories[index]
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
            )
        }
    }
}

@Composable
private fun SortBar(
    sortOrder: SortOrder,
    onSortChange: (SortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.products_sort_label),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(0.dp))
        Box {
            IconButton(onClick = { expanded = true }) {
                Text(
                    text = when (sortOrder) {
                        SortOrder.NAME -> stringResource(R.string.products_sort_name)
                        SortOrder.QUANTITY -> stringResource(R.string.products_sort_quantity)
                        SortOrder.RECENT -> stringResource(R.string.products_sort_recent)
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.products_sort_name)) },
                    onClick = { onSortChange(SortOrder.NAME); expanded = false },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.products_sort_quantity)) },
                    onClick = { onSortChange(SortOrder.QUANTITY); expanded = false },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.products_sort_recent)) },
                    onClick = { onSortChange(SortOrder.RECENT); expanded = false },
                )
            }
        }
    }
}
