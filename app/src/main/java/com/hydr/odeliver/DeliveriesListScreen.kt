package com.hydr.odeliver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesListScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDelivery by remember { mutableStateOf<DeliveryUiModel?>(null) }
    val sheetState = rememberModalBottomSheetState()
    
    var selectedTypeTab by remember { mutableIntStateOf(0) } // 0: All, 1: Incoming, 2: Outgoing
    var selectedStatusTab by remember { mutableIntStateOf(0) }
    
    val statusCategories = listOf("All", "Pending", "On Way", "Delivered", "Cancelled")
    val typeCategories = listOf("All Types", "Incoming", "Outgoing")

    val filteredDeliveries = remember(uiState.allDeliveries, selectedTypeTab, selectedStatusTab) {
        uiState.allDeliveries.filter { delivery ->
            val matchesType = when (selectedTypeTab) {
                1 -> !delivery.isOutgoing
                2 -> delivery.isOutgoing
                else -> true
            }
            val matchesStatus = when (selectedStatusTab) {
                1 -> delivery.statusEnum == DeliveryStatus.PENDING
                2 -> delivery.statusEnum == DeliveryStatus.ON_THE_WAY
                3 -> delivery.statusEnum == DeliveryStatus.DELIVERED
                4 -> delivery.statusEnum == DeliveryStatus.CANCELLED
                else -> true
            }
            matchesType && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("All Deliveries", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                
                // Type Filter
                ScrollableTabRow(
                    selectedTabIndex = selectedTypeTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTypeTab]),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                ) {
                    typeCategories.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTypeTab == index,
                            onClick = { selectedTypeTab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }

                // Status Filter
                ScrollableTabRow(
                    selectedTabIndex = selectedStatusTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    divider = {}
                ) {
                    statusCategories.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedStatusTab == index,
                            onClick = { selectedStatusTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddDelivery.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Delivery")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.HomeScreen.route) }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.Reports.route) }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Reports") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.SalesRecord.route) }, icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) }, label = { Text("Sales") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.Profile.route) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        if (filteredDeliveries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No matching deliveries found.")
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
                items(filteredDeliveries) { delivery ->
                    UpcomingDeliveryItem(
                        time = delivery.time,
                        date = delivery.date,
                        itemName = delivery.itemName,
                        customerName = delivery.customerName,
                        status = delivery.status,
                        statusColor = delivery.statusColor,
                        isOutgoing = delivery.isOutgoing,
                        onClick = {
                            selectedDelivery = delivery
                            showBottomSheet = true
                        }
                    )
                }
            }
        }

        if (showBottomSheet && selectedDelivery != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
            ) {
                DeliveryUpdateSheet(
                    delivery = selectedDelivery!!,
                    onUpdate = { status, isLate, isOutgoing ->
                        viewModel.updateDelivery(selectedDelivery!!, status, isLate, isOutgoing)
                        showBottomSheet = false
                    },
                    onDelete = { id ->
                        viewModel.deleteDelivery(id)
                        showBottomSheet = false
                    },
                    onDismiss = { showBottomSheet = false }
                )
            }
        }
    }
}
