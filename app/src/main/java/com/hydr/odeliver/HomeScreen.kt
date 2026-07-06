package com.hydr.odeliver

import android.R.attr.tint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hydr.odeliver.ui.utils.toDisplayText
import com.hydr.odeliver.ui.utils.formatCurrency
import com.hydr.odeliver.ui.utils.formatDisplayDate
import com.hydr.odeliver.ui.utils.formatDisplayTime
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showAddOptions by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedDelivery by remember { mutableStateOf<DeliveryUiModel?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showAddSaleDialog by remember { mutableStateOf(false) }

    if (showAddSaleDialog) {
        AddSaleDialog(
            onDismiss = { showAddSaleDialog = false },
            onAdd = { customer, product, price, qty, date, time ->
                viewModel.addSale(customer, product, price, qty, date, time)
                showAddSaleDialog = false
            }
        )
    }

    if (showAddOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptions = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "What would you like to add?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    onClick = {
                        showAddOptions = false
                        navController.navigate(Screen.AddDelivery.route)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (darkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("New Delivery", fontWeight = FontWeight.Bold)
                            Text("Record a new incoming or outgoing delivery", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Surface(
                    onClick = {
                        showAddOptions = false
                        showAddSaleDialog = true
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (darkTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("New Sale", fontWeight = FontWeight.Bold)
                            Text("Record a finished transaction/sale", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Financial Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Sales (+)")
                        Text(uiState.totalSalesAmount.formatCurrency(), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Expenses (-)")
                        Text(uiState.spent.formatCurrency(), color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Balance", fontWeight = FontWeight.Bold)
                        Text(uiState.netSpent.formatCurrency(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            },
            confirmButton = { Button(onClick = { showReportDialog = false }) { Text("Close") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ODeliver",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, contentDescription = "Theme")
                    }
                    BadgedBox(badge = { Badge { Text("3") } }, modifier = Modifier.padding(end = 16.dp)) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddOptions = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add New") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = if (darkTheme) MaterialTheme.colorScheme.onPrimary else Color.Black,
                expanded = true
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Reports") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { navController.navigate(Screen.SalesRecord.route) }, icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) }, label = { Text("Sales") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { navController.navigate(Screen.Profile.route) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(scrollState).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = if (darkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.shopName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color =  MaterialTheme.colorScheme.onPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.businessAddress,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sales Revenue",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = uiState.totalSalesAmount.formatCurrency(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Incoming Cost",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = uiState.incomingCost.formatCurrency(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Net Balance", uiState.netSpent.formatCurrency(), Icons.Default.Scale, MaterialTheme.colorScheme.secondaryContainer, if (darkTheme) Color.White else Color.Black, onClick = { showReportDialog = true })
                StatCard(Modifier.weight(1f), "Total Expenses", uiState.spent.formatCurrency(), Icons.Default.Payments, Color.Magenta.copy(.4f), if (darkTheme) Color.White else Color.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Sales Activity", "${uiState.salesRecords.size} Recorded", Icons.Default.TrendingUp, Color.Blue.copy(.5f), if (darkTheme) Color.White else Color.Black, onClick = { navController.navigate(Screen.SalesRecord.route) })
                StatCard(Modifier.weight(1f), "All Orders", "${uiState.deliveries}", Icons.Default.LocalShipping, Color.Cyan.copy(.3f), if (darkTheme) Color.White else Color.Black, onClick = { navController.navigate(Screen.DeliveriesList.route) })
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Upcoming Deliveries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            if (uiState.upcomingDeliveries.isEmpty()) {
                Text(text = "No upcoming deliveries.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                uiState.upcomingDeliveries.forEach { delivery ->
                    UpcomingDeliveryItem(
                        time = delivery.time,
                        date = delivery.date,
                        itemName = delivery.itemName,
                        customerName = delivery.customerName,
                        status = delivery.status,
                        statusColor = delivery.statusColor,
                        isOutgoing = delivery.isOutgoing,
                        onClick = { selectedDelivery = delivery; showBottomSheet = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showBottomSheet && selectedDelivery != null) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
                DeliveryUpdateSheet(delivery = selectedDelivery!!, onUpdate = { s, l, o -> viewModel.updateDelivery(selectedDelivery!!, s, l, o); showBottomSheet = false }, onDelete = { id -> viewModel.deleteDelivery(id); showBottomSheet = false }, onDismiss = { showBottomSheet = false })
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, containerColor: Color, contentColor: Color, onClick: () -> Unit = {} ) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor =  contentColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall)
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UpcomingDeliveryItem(time: String, date: String, itemName: String, customerName: String, status: String, statusColor: Color, isOutgoing: Boolean = true, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isOutgoing) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Magenta.copy(alpha = 0.1f),
                        contentColor = if (isOutgoing) MaterialTheme.colorScheme.primary else Color.Magenta,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isOutgoing) "OUT" else "IN",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = itemName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "${if (isOutgoing) "For" else "From"} $customerName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${time.formatDisplayTime()} • ${date.formatDisplayDate()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Surface(color = statusColor.copy(alpha = 0.2f), contentColor = statusColor, shape = RoundedCornerShape(12.dp)) {
                Text(text = status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeliveryUpdateSheet(
    delivery: DeliveryUiModel,
    onUpdate: (DeliveryStatus, Boolean, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(delivery.statusEnum) }
    var isLate by remember { mutableStateOf(delivery.isLate) }
    var isOutgoing by remember { mutableStateOf(delivery.isOutgoing) }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Update Delivery", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDelete(delivery.id) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
        Text(
            text = "${delivery.itemName} ${if (isOutgoing) "for" else "from"} ${delivery.customerName}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text("Delivery Type", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = isOutgoing, onClick = { isOutgoing = true }, label = { Text("Outgoing") }, modifier = Modifier.weight(1f))
            FilterChip(selected = !isOutgoing, onClick = { isOutgoing = false }, label = { Text("Incoming") }, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DeliveryStatus.entries.forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s.toDisplayText()) }) }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mark as Late", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isLate, onCheckedChange = { isLate = it })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onUpdate(status, isLate, isOutgoing) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Update Status") }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
