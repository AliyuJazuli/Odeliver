package com.hydr.odeliver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hydr.odeliver.ui.utils.formatCurrency
import com.hydr.odeliver.ui.utils.SegmentedInputField
import com.hydr.odeliver.ui.utils.MoneySegmentedInput
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDialog(
    sale: SaleUiModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String, String) -> Unit
) {
    var customerName by remember { mutableStateOf(sale?.customerName ?: "") }
    var productNumber by remember { mutableStateOf(sale?.productNumber ?: "") }
    var price by remember { mutableStateOf(sale?.price?.toString() ?: "") }
    
    val initialQuantityType = when {
        sale == null -> "one"
        sale.quantity == "one" -> "one"
        sale.quantity == "bulk" -> "bulk"
        else -> "specific"
    }
    var quantityType by remember { mutableStateOf(initialQuantityType) }
    var specificQuantity by remember { mutableStateOf(if (initialQuantityType == "specific") sale?.quantity ?: "" else "") }
    var isPricePerItem by remember { mutableStateOf(false) }
    
    val sdfDate = remember { SimpleDateFormat("ddMMyyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HHmm", Locale.getDefault()) }
    
    val currentDay = remember { sdfDate.format(Date()) }
    val currentTime = remember { sdfTime.format(Date()) }
    
    var date by remember { mutableStateOf(sale?.date ?: "") }
    var time by remember { mutableStateOf(sale?.time ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (sale == null) "Add New Sale" else "Edit Sale") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") }, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                OutlinedTextField(value = productNumber, onValueChange = { productNumber = it }, label = { Text("Product Name") }, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isPricePerItem) "Price Each" else "Total Price",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            MoneySegmentedInput(
                                value = price,
                                onValueChange = { price = it }
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Per Item", style = MaterialTheme.typography.labelSmall)
                            Checkbox(checked = isPricePerItem, onCheckedChange = { isPricePerItem = it })
                        }
                    }
                }

                if (isPricePerItem) {
                    val qtyValue = when (quantityType) {
                        "one" -> 1.0
                        "bulk" -> 1.0
                        "specific" -> specificQuantity.toDoubleOrNull() ?: 1.0
                        else -> 1.0
                    }
                    val total = (price.toDoubleOrNull() ?: 0.0) * qtyValue
                    Text(
                        text = "Total: ${total.formatCurrency()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
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
            Button(
                onClick = {
                    val rawPrice = price.toDoubleOrNull() ?: 0.0
                    if (customerName.isNotBlank() && productNumber.isNotBlank() && rawPrice > 0) {
                        val qtyValue = when (quantityType) {
                            "one" -> 1.0
                            "bulk" -> 1.0
                            "specific" -> specificQuantity.toDoubleOrNull() ?: 1.0
                            else -> 1.0
                        }
                        val finalPrice = if (isPricePerItem) rawPrice * qtyValue else rawPrice
                        
                        val finalQuantity = if (quantityType == "specific") specificQuantity else quantityType
                        val finalDate = date.ifBlank { currentDay }
                        val finalTime = time.ifBlank { currentTime }
                        onConfirm(customerName, productNumber, finalPrice, finalQuantity, finalDate, finalTime)
                    }
                },
                enabled = customerName.isNotBlank() && productNumber.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(if (sale == null) "Add" else "Update", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSaleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, String, String, String) -> Unit
) {
    SaleDialog(
        onDismiss = onDismiss,
        onConfirm = onAdd
    )
}
