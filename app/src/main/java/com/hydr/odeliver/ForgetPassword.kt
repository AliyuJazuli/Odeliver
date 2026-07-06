package com.hydr.odeliver

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState

@Composable
fun ForgetPassword(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: AuthViewModel
){
    val context = LocalContext.current
    var alert by remember { mutableStateOf(false) }
    val email by viewModel.email.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row() {
                IconButton(
                    modifier = Modifier.padding(top = 40.dp, start = 3.dp, end = 40.dp),
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    modifier = Modifier.padding(top = 40.dp, start = 290.dp),
                    onClick = onThemeToggle
                ) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                text = "Forgot Password",
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                modifier = Modifier.padding(top = 5.dp, start = 20.dp),
                text = "Don't worry we got you",
                fontSize = 17.sp,
                color = if (darkTheme) Color.White else Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.padding(top = 20.dp))

            Image(
                modifier = Modifier.padding(vertical = 30.dp).fillMaxWidth().height(250.dp),
                painter = painterResource(R.drawable.group_80),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            Text(
                modifier = Modifier.padding(top = 45.dp, start = 23.dp),
                text = "Email Address",
                fontSize = 22.sp,
                color = if (darkTheme) Color.White else Color.Black,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = {Text(text= "Enter email address", fontSize = 18.sp, fontWeight = FontWeight.Thin)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color(0xFFD9E1E2),
                    unfocusedContainerColor = Color(0xFFD9E1E2)
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 23.dp, top = 10.dp, end = 10.dp).size(340.dp, 59.dp)
            )
            
            if (alert)
                AlertDialog(
                    title = {Text("Confirm To Reset")},
                    text = {Text("Check email and reset password, not found? Check spam")},
                    onDismissRequest = { alert = false },
                    confirmButton = { Button(onClick = { navController.popBackStack() }) { Text("OK") } }
                )

            Button(
                onClick = {
                    if (email.isNotEmpty())
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task -> if (task.isSuccessful) alert = true }
                },
                modifier = Modifier.padding(start = 35.dp, top = 45.dp).size(364.dp, 67.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(text = "Reset Password", fontWeight = FontWeight.Bold, fontSize = 21.sp)
            }

            Row(modifier = Modifier.padding(top = 100.dp).align(Alignment.CenterHorizontally)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { navController.popBackStack() })
                Text(modifier = Modifier.clickable { navController.popBackStack() }, text = " Back to login", fontWeight = FontWeight.Light, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
