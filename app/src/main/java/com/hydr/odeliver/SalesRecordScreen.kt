package com.hydr.odeliver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRecordScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

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
                    SaleItem(sale = sale, onDelete = { viewModel.deleteSale(sale.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddSaleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { customer, productNum, price, qty, date, time ->
                viewModel.addSale(customer, productNum, price, qty, date, time)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SaleItem(sale: SaleUiModel, onDelete: () -> Unit) {
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
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(onDismiss: () -> Unit, onAdd: (String, String, Double, String, String, String) -> Unit) {
    var customerName by remember { mutableStateOf("") }
    var productNumber by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantityType by remember { mutableStateOf("one") }
    var specificQuantity by remember { mutableStateOf("") }
    
    val sdfDate = remember { SimpleDateFormat("ddMMyyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HHmm", Locale.getDefault()) }
    
    val currentDay = remember { sdfDate.format(Date()) }
    val currentTime = remember { sdfTime.format(Date()) }
    
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Sale") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") }, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                OutlinedTextField(value = productNumber, onValueChange = { productNumber = it }, label = { Text("Product Name") }, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                
                Text("Quantity Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("one", "bulk", "specific").forEach { qty ->
                        FilterChip(
                            selected = quantityType == qty,
                            onClick = { quantityType = qty },
                            label = { Text(qty) }
                        )
                    }
                }

                if (quantityType == "specific") {
                    OutlinedTextField(
                        value = specificQuantity,
                        onValueChange = { specificQuantity = it },
                        label = { Text("Enter Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                }
                
                Column {
                    Text("Date (DD/MM/YYYY)", style = MaterialTheme.typography.labelSmall)
                    SegmentedInputField(
                        value = date,
                        onValueChange = { if (it.length <= 8) date = it },
                        length = 8,
                        mask = "##/##/####",
                        placeholder = currentDay
                    )
                }

                Column {
                    Text("Time (HH:MM)", style = MaterialTheme.typography.labelSmall)
                    SegmentedInputField(
                        value = time,
                        onValueChange = { if (it.length <= 4) time = it },
                        length = 4,
                        mask = "##:##",
                        placeholder = currentTime
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalQuantity = if (quantityType == "specific") specificQuantity else quantityType
                val finalDate = date.ifBlank { currentDay }
                val finalTime = time.ifBlank { currentTime }
                onAdd(customerName, productNumber, price.toDoubleOrNull() ?: 0.0, finalQuantity, finalDate, finalTime)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
