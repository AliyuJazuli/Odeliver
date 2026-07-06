package com.hydr.odeliver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
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
import com.hydr.odeliver.ui.utils.toDisplayText
import com.hydr.odeliver.ui.utils.SegmentedInputField
import com.hydr.odeliver.ui.utils.MoneySegmentedInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeliveryScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val sdfDate = remember { SimpleDateFormat("ddMMyyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HHmm", Locale.getDefault()) }
    val currentDay = remember { sdfDate.format(Date()) }
    val currentTime = remember { sdfTime.format(Date()) }

    var itemName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var isOutgoing by remember { mutableStateOf(true) }
    var cost by remember { mutableStateOf("") }
    var numberOfProducts by remember { mutableStateOf("1") }
    var isPricePerItem by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(DeliveryStatus.PENDING) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Delivery",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (itemName.isNotBlank() && customerName.isNotBlank()) {
                        viewModel.addDelivery(
                            time = time.ifBlank { currentTime },
                            date = date.ifBlank { currentDay },
                            itemName = itemName,
                            customerName = customerName,
                            cost = cost.toDoubleOrNull() ?: 0.0,
                            numberOfProducts = numberOfProducts.toIntOrNull() ?: 1,
                            isPricePerItem = isPricePerItem,
                            status = selectedStatus,
                            isOutgoing = isOutgoing,
                            notes = notes
                        )
                        navController.popBackStack()
                    }
                },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save Delivery") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = if (darkTheme) MaterialTheme.colorScheme.onPrimary else Color.Black
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (darkTheme) Color.White.copy(.6f) else Color.Black.copy(.5f)
                )
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (darkTheme) Color.White.copy(.6f) else Color.Black.copy(.5f)
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = numberOfProducts,
                    onValueChange = { numberOfProducts = it },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (darkTheme) Color.White.copy(.6f) else Color.Black.copy(.5f)
                    )
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Price Type", style = MaterialTheme.typography.labelSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPricePerItem, onCheckedChange = { isPricePerItem = it })
                        Text(if (isPricePerItem) "Per Item" else "Total Price", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Column {
                Text(
                    text = if (isPricePerItem) "Price Each" else "Total Cost",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                MoneySegmentedInput(
                    value = cost,
                    onValueChange = { cost = it }
                )
            }

            Text("Delivery Type", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isOutgoing,
                    onClick = { isOutgoing = true },
                    label = { Text("Outgoing") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !isOutgoing,
                    onClick = { isOutgoing = false },
                    label = { Text("Incoming") },
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                Text("Date (DD/MM/YYYY)", style = MaterialTheme.typography.labelLarge)
                SegmentedInputField(
                    value = date,
                    onValueChange = { if (it.length <= 8) date = it },
                    length = 8,
                    mask = "##/##/####",
                    placeholder = currentDay
                )
            }

            Column {
                Text("Time (HH:MM)", style = MaterialTheme.typography.labelLarge)
                SegmentedInputField(
                    value = time,
                    onValueChange = { if (it.length <= 4) time = it },
                    length = 4,
                    mask = "##:##",
                    placeholder = currentTime
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedStatus.toDisplayText(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Initial Status") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(.9f)
                ) {
                    DeliveryStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.toDisplayText()) },
                            onClick = {
                                selectedStatus = status
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
