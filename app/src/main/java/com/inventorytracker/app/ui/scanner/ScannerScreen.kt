package com.inventorytracker.app.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventorytracker.app.R
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.scanner.BarcodeAnalyzer

private val RETICLE_SIZE = 250.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onProductFound: (Long) -> Unit,
    onNewBarcode: (String, BarcodeFormat) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var torchEnabled by rememberSaveable { mutableStateOf(false) }
    var manualBarcode by rememberSaveable { mutableStateOf("") }
    var manualFormat by rememberSaveable { mutableStateOf(BarcodeFormat.EAN_13) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionRequested = true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionRequested) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val analyzer = remember(viewModel) {
        BarcodeAnalyzer { result -> viewModel.onBarcodeDetected(result) }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is ScannerViewModel.NavigationEvent.Found -> onProductFound(event.productId)
                is ScannerViewModel.NavigationEvent.NewBarcode ->
                    onNewBarcode(event.barcode, event.format)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scanner_title)) },
                actions = {
                    if (hasCameraPermission) {
                        IconButton(onClick = { torchEnabled = !torchEnabled }) {
                            Icon(
                                imageVector = if (torchEnabled) Icons.Filled.FlashlightOff else Icons.Filled.FlashlightOn,
                                contentDescription = stringResource(R.string.scanner_torch),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        analyzer = analyzer,
                        torchEnabled = torchEnabled,
                        modifier = Modifier.fillMaxSize(),
                    )
                    ScanReticle(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                ManualEntry(
                    barcode = manualBarcode,
                    onBarcodeChange = { manualBarcode = it },
                    format = manualFormat,
                    onFormatChange = { manualFormat = it },
                    onLookup = {
                        viewModel.onManualEntry(manualBarcode, manualFormat)
                        manualBarcode = ""
                    },
                )
            }
        }
    }
}

@Composable
private fun ScanReticle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(RETICLE_SIZE)
            .drawBehind {
                val corner = size.width * 0.15f
                val stroke = 6f
                val color = Color.White
                val start = 0f
                val end = size.width
                // top-left
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, corner), end = androidx.compose.ui.geometry.Offset(0f, 0f), strokeWidth = stroke)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(corner, 0f), strokeWidth = stroke)
                // top-right
                drawLine(color, start = androidx.compose.ui.geometry.Offset(end - corner, 0f), end = androidx.compose.ui.geometry.Offset(end, 0f), strokeWidth = stroke)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(end, 0f), end = androidx.compose.ui.geometry.Offset(end, corner), strokeWidth = stroke)
                // bottom-left
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, end - corner), end = androidx.compose.ui.geometry.Offset(0f, end), strokeWidth = stroke)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, end), end = androidx.compose.ui.geometry.Offset(corner, end), strokeWidth = stroke)
                // bottom-right
                drawLine(color, start = androidx.compose.ui.geometry.Offset(end, end - corner), end = androidx.compose.ui.geometry.Offset(end, end), strokeWidth = stroke)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(end - corner, end), end = androidx.compose.ui.geometry.Offset(end, end), strokeWidth = stroke)
            },
    )
}

@Composable
private fun ManualEntry(
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    format: BarcodeFormat,
    onFormatChange: (BarcodeFormat) -> Unit,
    onLookup: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.scanner_permission_denied),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = barcode,
            onValueChange = onBarcodeChange,
            label = { Text(stringResource(R.string.scanner_manual_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manual_barcode_input"),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.scanner_manual_format),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        BarcodeFormatSelector(format = format, onFormatChange = onFormatChange)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLookup,
            enabled = barcode.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.scanner_manual_lookup))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeFormatSelector(
    format: BarcodeFormat,
    onFormatChange: (BarcodeFormat) -> Unit,
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        items(BarcodeFormat.entries) { entry ->
            if (entry != BarcodeFormat.UNKNOWN) {
                FilterChip(
                    selected = format == entry,
                    onClick = { onFormatChange(entry) },
                    label = { Text(entry.displayName) },
                )
            }
        }
    }
}