package com.hydr.odeliver

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import com.hydr.odeliver.ui.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

enum class ReportPeriod { DAILY, WEEKLY, MONTHLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTHLY) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Filtering Logic
    val filteredSales = remember(uiState.salesRecords, selectedPeriod) {
        uiState.salesRecords.filter { isDateInPeriod(it.date, selectedPeriod) }
    }
    val filteredDeliveries = remember(uiState.allDeliveries, selectedPeriod) {
        uiState.allDeliveries.filter { isDateInPeriod(it.date, selectedPeriod) }
    }

    val periodRevenue = filteredSales.sumOf { it.price }
    val periodExpenses = filteredDeliveries.sumOf { it.cost }
    val periodNet = periodRevenue - periodExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.HomeScreen.route) }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Reports") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.SalesRecord.route) }, icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) }, label = { Text("Sales") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.Profile.route) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Period Selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ReportPeriod.entries.forEachIndexed { index, period ->
                    SegmentedButton(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ReportPeriod.entries.size)
                    ) {
                        Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // Ordinary Net Balance Card
            SummaryCard(
                revenue = periodRevenue,
                expenses = periodExpenses,
                net = periodNet,
                period = selectedPeriod
            )

            // Tabs for different views
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Financials", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Inventory", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (selectedTab == 0) {
                    FinancialDetails(filteredSales, filteredDeliveries)
                } else {
                    InventoryInsights(filteredDeliveries)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(revenue: Double, expenses: Double, net: Double, period: ReportPeriod) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "${period.name} Summary",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Net Balance", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = net.formatCurrency(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (net >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                Icon(
                    imageVector = if (net >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (net >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Revenue", style = MaterialTheme.typography.labelSmall)
                    Text(revenue.formatCurrency(), fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Expenses", style = MaterialTheme.typography.labelSmall)
                    Text(expenses.formatCurrency(), fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun FinancialDetails(sales: List<SaleUiModel>, deliveries: List<DeliveryUiModel>) {
    Text("Insights & Business Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    
    val topSale = sales.maxByOrNull { it.price }
    val avgSale = if (sales.isNotEmpty()) sales.map { it.price }.average() else 0.0
    val topCustomer = sales.groupBy { it.customerName }.maxByOrNull { it.value.size }?.key ?: "N/A"
    
    val totalRevenue = sales.sumOf { it.price }
    val totalCost = deliveries.sumOf { it.cost }
    val profitMargin = if (totalRevenue > 0) ((totalRevenue - totalCost) / totalRevenue * 100) else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Avg. Sale",
                value = avgSale.formatCurrency(),
                icon = Icons.Default.Payments,
                color = Color.Blue
            )
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Profit Margin",
                value = String.format(Locale.getDefault(), "%.1f%%", profitMargin),
                icon = Icons.Default.Percent,
                color = if (profitMargin >= 0) Color(0xFF4CAF50) else Color.Red
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Top Customer",
                value = topCustomer,
                icon = Icons.Default.Person,
                color = Color(0xFF673AB7)
            )
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Transaction Count",
                value = sales.size.toString(),
                icon = Icons.Default.Receipt,
                color = Color(0xFFFF9800)
            )
        }
    }

    if (topSale != null) {
        BreakdownCard(
            title = "Highest Revenue Sale",
            value = topSale.price.formatCurrency(),
            description = "${topSale.productNumber} sold to ${topSale.customerName}",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            color = Color(0xFF4CAF50)
        )
    }

    Text("Recent Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    
    val allTransactions = (sales.map { it to "Sale" } + deliveries.filter { it.cost > 0 }.map { it to "Delivery" })
        .sortedByDescending { /* In a real app, parse date/time to sort */ 0 }

    if (allTransactions.isEmpty()) {
        Text("No activity recorded for this period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        allTransactions.take(8).forEach { (item, type) ->
            val title = if (item is SaleUiModel) item.productNumber else (item as DeliveryUiModel).itemName
            val subtitle = if (item is SaleUiModel) item.customerName else (item as DeliveryUiModel).customerName
            val amount = if (item is SaleUiModel) item.price else (item as DeliveryUiModel).cost
            val isIncome = type == "Sale" || (item is DeliveryUiModel && !item.isOutgoing)

            TransactionRow(title, subtitle, amount.formatCurrency(), isIncome)
        }
    }
}

@Composable
fun TransactionRow(title: String, subtitle: String, amount: String, isIncome: Boolean) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Text(
                text = (if (isIncome) "+" else "-") + amount,
                color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun InventoryInsights(deliveries: List<DeliveryUiModel>) {
    Text("Logistics & Stock Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    
    val pendingCount = deliveries.count { it.statusEnum == DeliveryStatus.PENDING || it.statusEnum == DeliveryStatus.ON_THE_WAY }
    val expensiveDelivery = deliveries.maxByOrNull { it.cost }
    val pendingValue = deliveries.filter { it.statusEnum == DeliveryStatus.PENDING || it.statusEnum == DeliveryStatus.ON_THE_WAY }.sumOf { it.cost }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Total Orders",
                value = deliveries.size.toString(),
                icon = Icons.Default.LocalShipping,
                color = Color.Blue
            )
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Active Orders",
                value = pendingCount.toString(),
                icon = Icons.Default.PendingActions,
                color = Color.DarkGray
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Pending Value",
                value = pendingValue.formatCurrency(),
                icon = Icons.Default.Inventory,
                color = Color(0xFF607D8B)
            )
            InfoMiniCard(
                modifier = Modifier.weight(1f),
                label = "Completion Rate",
                value = if (deliveries.isNotEmpty()) String.format(Locale.getDefault(), "%.0f%%", (deliveries.count { it.statusEnum == DeliveryStatus.DELIVERED }.toFloat() / deliveries.size * 100)) else "0%",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50)
            )
        }
    }

    if (expensiveDelivery != null) {
        BreakdownCard(
            title = "Highest Inventory Cost",
            value = expensiveDelivery.cost.formatCurrency(),
            description = "${expensiveDelivery.itemName} from ${expensiveDelivery.customerName}",
            icon = Icons.Default.LocalShipping,
            color = Color.Red
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Delivery Ratio", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val outgoing = deliveries.count { it.isOutgoing }
            val incoming = deliveries.count { !it.isOutgoing }
            val total = outgoing + incoming
            
            if (total > 0) {
                LinearProgressIndicator(
                    progress = { outgoing.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.secondary
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Outgoing ($outgoing)", style = MaterialTheme.typography.labelSmall)
                    Text("Incoming ($incoming)", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Text("No deliveries for this period.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun InfoMiniCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun BreakdownCard(title: String, value: String, description: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

fun isDateInPeriod(dateStr: String, period: ReportPeriod): Boolean {
    val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    val date = try { sdf.parse(dateStr) } catch (e: Exception) { null } ?: return false
    val calendar = Calendar.getInstance()
    val now = Calendar.getInstance()
    calendar.time = date
    
    return when (period) {
        ReportPeriod.DAILY -> {
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        }
        ReportPeriod.WEEKLY -> {
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
        }
        ReportPeriod.MONTHLY -> {
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }
    }
}
