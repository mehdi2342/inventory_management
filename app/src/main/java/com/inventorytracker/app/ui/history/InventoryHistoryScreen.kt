package com.inventorytracker.app.ui.history

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import com.inventorytracker.app.ui.components.EmptyState
import com.inventorytracker.app.ui.components.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryHistoryScreen(
    onBack: () -> Unit,
    onProductClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TimeRangeRow(
                selected = uiState.timeRange,
                onSelect = viewModel::onTimeRangeChange,
            )
            ProductFilterDropdown(
                products = uiState.products,
                selectedProductId = uiState.selectedProductId,
                onSelect = viewModel::onProductFilterChange,
            )

            if (uiState.entries.isEmpty()) {
                EmptyState(
                    message = stringResource(R.string.history_no_results),
                    icon = Icons.Outlined.History,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.entries, key = { it.log.id }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onClick = { entry.product?.let { onProductClick(it.id) } },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeRangeRow(selected: TimeRange, onSelect: (TimeRange) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == TimeRange.ALL,
                onClick = { onSelect(TimeRange.ALL) },
                label = { Text(stringResource(R.string.history_all_time)) },
            )
        }
        item {
            FilterChip(
                selected = selected == TimeRange.TODAY,
                onClick = { onSelect(TimeRange.TODAY) },
                label = { Text("Today") },
            )
        }
        item {
            FilterChip(
                selected = selected == TimeRange.WEEK,
                onClick = { onSelect(TimeRange.WEEK) },
                label = { Text("7 days") },
            )
        }
        item {
            FilterChip(
                selected = selected == TimeRange.MONTH,
                onClick = { onSelect(TimeRange.MONTH) },
                label = { Text("30 days") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFilterDropdown(
    products: List<com.inventorytracker.app.domain.model.Product>,
    selectedProductId: Long?,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = products.find { it.id == selectedProductId }?.name
        ?: stringResource(R.string.history_all_products)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.products_filter_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.history_all_products)) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            products.sortedBy { it.name.lowercase() }.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                        onSelect(product.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    entry: InventoryLogWithProduct,
    onClick: () -> Unit,
) {
    val log = entry.log
    val sign = when (log.changeType) {
        ChangeType.INCREASE -> "+"
        ChangeType.DECREASE -> "-"
        else -> ""
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.product?.name ?: "—",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = formatTimestamp(log.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "$sign${log.amount} → ${log.newQuantity}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (log.changeType == ChangeType.DECREASE) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                )
            }
            log.reason?.let { reason ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
