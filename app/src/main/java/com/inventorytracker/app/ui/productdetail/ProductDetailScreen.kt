package com.inventorytracker.app.ui.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventorytracker.app.R
import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.InventoryLog
import com.inventorytracker.app.ui.components.EmptyState
import com.inventorytracker.app.ui.components.categoryColor
import com.inventorytracker.app.ui.components.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onEditProduct: (Long) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val product = uiState.product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (product != null) {
                        MoreMenu(
                            onEdit = { onEditProduct(product.id) },
                            onDelete = { showDeleteDialog = true },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (product == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.error_product_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ProductInfoCard(
                    product = product,
                    categoryName = uiState.category?.name ?: stringResource(R.string.categories_uncategorized),
                    categoryColor = categoryColor(uiState.category),
                )
            }
            item {
                AdjustCard(viewModel = viewModel)
            }
            item {
                Text(
                    text = stringResource(R.string.product_history),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (uiState.logs.isEmpty()) {
                item {
                    EmptyState(
                        message = stringResource(R.string.product_no_history),
                        icon = Icons.Outlined.History,
                    )
                }
            } else {
                items(uiState.logs, key = { it.id }) { log ->
                    LogRow(log = log)
                }
            }
        }
    }

    if (showDeleteDialog && product != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.product_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.product_delete_confirm_message,
                        product.name,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete()
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun MoreMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_edit)) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = { expanded = false; onEdit() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}

@Composable
private fun ProductInfoCard(
    product: com.inventorytracker.app.domain.model.Product,
    categoryName: String,
    categoryColor: Long,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = stringResource(R.string.product_barcode), value = product.barcode)
            InfoRow(label = stringResource(R.string.product_barcode_format), value = product.barcodeFormat.displayName)
            InfoRow(label = stringResource(R.string.product_category), value = categoryName)
            product.description?.let {
                InfoRow(label = stringResource(R.string.product_description), value = it)
            }
            InfoRow(
                label = stringResource(R.string.product_low_stock_threshold),
                value = "${product.lowStockThreshold} ${product.unit}",
            )
            InfoRow(label = stringResource(R.string.product_created), value = formatTimestamp(product.createdAt))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.product_quantity),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${product.quantity} ${product.unit}",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (product.isLowStock) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                    )
                }
                if (product.isLowStock) {
                    Text(
                        text = stringResource(R.string.product_low_stock),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(130.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AdjustCard(viewModel: ProductDetailViewModel) {
    var amount by rememberSaveable { mutableStateOf("") }
    var reason by rememberSaveable { mutableStateOf("") }
    var setValue by rememberSaveable { mutableStateOf("") }

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.product_adjust),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.product_adjust_increase),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAmountChip("+1") { viewModel.adjust(1, reason.ifBlank { null }) }
                QuickAmountChip("+5") { viewModel.adjust(5, reason.ifBlank { null }) }
                QuickAmountChip("+10") { viewModel.adjust(10, reason.ifBlank { null }) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.product_adjust_decrease),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAmountChip("-1") { viewModel.adjust(-1, reason.ifBlank { null }) }
                QuickAmountChip("-5") { viewModel.adjust(-5, reason.ifBlank { null }) }
                QuickAmountChip("-10") { viewModel.adjust(-10, reason.ifBlank { null }) }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(stringResource(R.string.product_reason_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.product_adjust_set),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = setValue,
                    onValueChange = { input -> setValue = input.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.product_quantity)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        setValue.toIntOrNull()?.let { viewModel.setQuantity(it, reason.ifBlank { null }) }
                    },
                    enabled = setValue.toIntOrNull() != null,
                ) {
                    Text(stringResource(R.string.product_adjust_set))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAmountChip(label: String, onClick: () -> Unit) {
    InputChip(
        selected = false,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun LogRow(log: InventoryLog) {
    val sign = when (log.changeType) {
        ChangeType.INCREASE -> "+"
        ChangeType.DECREASE -> "-"
        else -> ""
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = changeTypeDisplay(log.changeType),
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

@Composable
private fun changeTypeDisplay(type: ChangeType): String = when (type) {
    ChangeType.INCREASE -> stringResource(R.string.change_increase)
    ChangeType.DECREASE -> stringResource(R.string.change_decrease)
    ChangeType.SET -> stringResource(R.string.change_set)
    ChangeType.UNDO -> stringResource(R.string.change_undo)
}
