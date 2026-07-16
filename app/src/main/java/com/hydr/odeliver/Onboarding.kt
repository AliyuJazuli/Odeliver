package com.hydr.odeliver

import android.R.attr.contentDescription
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hydr.odeliver.ui.theme.OdeliverTheme



@Composable
fun OnboardingScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(top = 70.dp, start = 40.dp),
                        text = "Welcome",
                        fontSize = 39.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )


                    IconButton(
                        modifier = Modifier.padding(top = 70.dp, end = 20.dp),
                        onClick = onThemeToggle
                    ) {
                        if (darkTheme)
                            Icon(
                                imageVector =  Icons.Outlined.LightMode  ,
                                contentDescription = "Toggle Theme"
                        )
                        else
                            Icon(
                                imageVector =  Icons.Outlined.DarkMode ,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                    }
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, start = 40.dp, bottom = 20.dp),
                    text = "Login or Signup to continue",
                    fontSize = 19.sp,
                    color = if (darkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Image(
                    modifier = Modifier
                        .padding(vertical = 30.dp)
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(120.dp)),
                    painter = painterResource(R.drawable.onboardingpicture),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy((-6).dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Deliver",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 16.dp, bottom = 0.dp),
                    text = "Track your sales, deliveries, and",
                    fontSize = 19.sp,
                    color = if (darkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    modifier = Modifier.padding(top = 0.dp, bottom = 40.dp),
                    text = "expenses in one place.",
                    fontSize = 19.sp,
                    color = if (darkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = {
                        navController.navigate(Screen.SignupScreen.route){
                            popUpTo(Screen.Onboarding.route)
                        }
                    },
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .size(314.dp, 59.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(
                        text = "Create account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.LoginScreen.route){

                        }
                    },
                    modifier = Modifier
                        .padding(top = 25.dp)
                        .size(314.dp, 59.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFD9E1E2)
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                ) {
                    Text(
                        text = "Already have an account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(
                    onClick = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = "Continue as guest?",
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
}
