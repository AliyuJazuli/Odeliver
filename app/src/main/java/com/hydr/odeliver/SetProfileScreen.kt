package com.hydr.odeliver

import android.R.attr.name
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SetProfileScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: HomeViewModel = viewModel()
){

    val name by viewModel.name.collectAsState()
    val shopName by viewModel.shopName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val address by viewModel.address.collectAsState()
    val bio by viewModel.bio.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        if (!isError) {
                            viewModel.saveUser {
                                navController.navigate(Screen.HomeScreen.route) {
                                    popUpTo(Screen.SetProfileScreen.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isError) "OK" else "Continue")
                }
            },
            dismissButton = if (!isError) {
                {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("Cancel")
                    }
                }
            } else null
        )
    }

    Surface(

    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((-16).dp),
            modifier = Modifier.padding(top = 60.dp, start = 80.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.size(82.dp)
            )
            Text(
                text = "Deliver",
                fontSize = 45.sp,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 170.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(topStart = 60.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(25.dp)
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background,
                    )
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                Text("Welcome to odeliver", fontSize = 28.sp, modifier = Modifier.padding(16.dp))
                Text("Set Profile to continue", fontSize = 18.sp, modifier = Modifier.padding(start = 20.dp, bottom = 5.dp))

                Spacer(modifier = Modifier.height(26.dp))


                SetProfileTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    placeholder = "Full Name",
                    icon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetProfileTextField(
                    value = shopName,
                    onValueChange = { viewModel.onShopNameChange(it) },
                    placeholder = "Shop Name",
                    icon = Icons.Default.Store,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetProfileTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    placeholder = "Email Address",
                    icon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetProfileTextField(
                    value = phoneNumber,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    placeholder = "Phone Number",
                    icon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetProfileTextField(
                    value = address,
                    onValueChange = { viewModel.onAddressChange(it) },
                    placeholder = "Business Address",
                    icon = Icons.Default.LocationOn,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    darkTheme = darkTheme
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetProfileTextField(
                    value = bio,
                    onValueChange = { viewModel.onBioChange(it) },
                    placeholder = "Bio",
                    icon = Icons.Default.Info,
                    singleLine = false,
                    darkTheme = darkTheme,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val missingRequired = name.isEmpty() || shopName.isEmpty()
                        val missingOptional = email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || bio.isEmpty()

                        if (missingRequired) {
                            dialogTitle = "Missing Required Info"
                            dialogMessage = "Name and Shop Name are required to create your profile."
                            isError = true
                            showConfirmationDialog = true
                        } else if (missingOptional) {
                            dialogTitle = "Missing Optional Info"
                            val fields = mutableListOf<String>()
                            if (email.isEmpty()) fields.add("Email")
                            if (phoneNumber.isEmpty()) fields.add("Phone")
                            if (address.isEmpty()) fields.add("Address")
                            if (bio.isEmpty()) fields.add("Bio")
                            
                            dialogMessage = "You haven't set your ${fields.joinToString(", ")}. Would you like to continue to home anyway?"
                            isError = false
                            showConfirmationDialog = true
                        } else {
                            dialogTitle = "Profile Complete"
                            dialogMessage = "You have completed everything on your profile. Continue to home?"
                            isError = false
                            showConfirmationDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("create Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black)
                }
            }
        }
    }

}
@Composable
fun SetProfileTextField(
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