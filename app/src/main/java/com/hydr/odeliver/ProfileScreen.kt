package com.hydr.odeliver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: AuthViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scrollState = rememberScrollState()

    val name by viewModel.name.collectAsState()
    val shopName by viewModel.shopName.collectAsState()
    val email by viewModel.email.collectAsState()
    val address by viewModel.address.collectAsState()
    val bio by viewModel.bio.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var showGuestDialog by remember { mutableStateOf(false) }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text("Save Profile") },
            text = { Text("Please sign up or login to save your profile.") },
            confirmButton = {
                TextButton(onClick = {
                    showGuestDialog = false
                    navController.navigate(Screen.SignupScreen.route) {
                         popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Sign Up")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showGuestDialog = false
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Login")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditMode && user != null) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )
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
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.SalesRecord.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
                    label = { Text("Sales") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditMode) {
                Text(text = name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = shopName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InfoCard(icon = Icons.Default.Email, label = "Email", value = user?.email ?: email)
                InfoCard(icon = Icons.Default.LocationOn, label = "Address", value = address)
                InfoCard(icon = Icons.Default.Info, label = "Bio", value = bio)
                
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                ProfileTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    placeholder = "Full Name",
                    icon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = shopName,
                    onValueChange = { viewModel.onShopNameChange(it) },
                    placeholder = "Shop Name",
                    icon = Icons.Default.Store,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = user?.email ?: email,
                    onValueChange = { },
                    placeholder = "Email Address",
                    icon = Icons.Default.Email,
                    enabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = address,
                    onValueChange = { viewModel.onAddressChange(it) },
                    placeholder = "Business Address",
                    icon = Icons.Default.LocationOn,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = bio,
                    onValueChange = { viewModel.onBioChange(it) },
                    placeholder = "Bio",
                    icon = Icons.Default.Info,
                    singleLine = false,
                    darkTheme = darkTheme,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified),
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isEditMode) {
                Button(
                    onClick = { 
                       if (user == null) {
                           showGuestDialog = true
                       } else {
                           viewModel.saveUserToRoom {
                               isEditMode = false
                           }
                       }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value.ifEmpty { "Not set" }, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder:  String,
    icon: ImageVector,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    darkTheme: Boolean,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}
