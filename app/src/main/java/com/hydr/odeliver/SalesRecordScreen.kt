package com.hydr.odeliver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hydr.odeliver.ui.utils.formatCurrency
import com.hydr.odeliver.ui.utils.formatDisplayDate
import com.hydr.odeliver.ui.utils.formatDisplayTime
import com.hydr.odeliver.ui.utils.SegmentedInputField
import com.hydr.odeliver.ui.utils.MoneySegmentedInput
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRecordScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingSale by remember { mutableStateOf<SaleUiModel?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<SaleUiModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Records", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor =  Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sale")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Reports.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.BarChart, null) },
                    label = { Text("Reports") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
                    label = { Text("Sales") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        if (uiState.salesRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No sales records found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.salesRecords) { sale ->
                    SaleItem(
                        sale = sale,
                        onEdit = { editingSale = sale },
                        onDelete = { saleToDelete = sale }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        SaleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { customer, productNum, price, qty, date, time ->
                viewModel.addSale(customer, productNum, price, qty, date, time)
                showAddDialog = false
            }
        )
    }

    if (editingSale != null) {
        SaleDialog(
            sale = editingSale,
            onDismiss = { editingSale = null },
            onConfirm = { customer, productNum, price, qty, date, time ->
                editingSale?.let {
                    viewModel.updateSale(it.id, customer, productNum, price, qty, date, time)
                }
                editingSale = null
            }
        )
    }

    if (saleToDelete != null) {
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Sale") },
            text = { Text("How would you like to delete this sale?") },
            confirmButton = {
                Button(
                    onClick = {
                        saleToDelete?.let { viewModel.deleteSale(it.id, reverseTransaction = true) }
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete & Reverse", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        saleToDelete?.let { viewModel.deleteSale(it.id, reverseTransaction = false) }
                        saleToDelete = null
                    }) {
                        Text("Delete Record Only")
                    }
                    TextButton(onClick = { saleToDelete = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun SaleItem(sale: SaleUiModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sale.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "Product: ${sale.productNumber}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Qty: ${sale.quantity}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${sale.date.formatDisplayDate()} at ${sale.time.formatDisplayTime()}", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = sale.price.formatCurrency(), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}


