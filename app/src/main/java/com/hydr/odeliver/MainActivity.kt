package com.hydr.odeliver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.hydr.odeliver.ui.theme.OdeliverTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hydr.odeliver.ui.theme.DarkColorScheme
import com.hydr.odeliver.ui.theme.LightColorScheme
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val themeManager = ThemeManager(this)
        val isDark = themeManager.isDarkTheme()
        
        // Explicitly set the splash theme based on user preference to override system theme
        if (isDark) {
            setTheme(R.style.Theme_App_Starting_Dark)
        } else {
            setTheme(R.style.Theme_App_Starting)
        }

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val themeManagerInner = remember { themeManager }
            var darkTheme by remember { mutableStateOf(isDark) }

            OdeliverTheme(darkTheme = darkTheme) {
                MainScreen(
                    darkTheme = darkTheme,
                    onThemeToggle = {
                        val newTheme = !darkTheme
                        darkTheme = newTheme
                        themeManagerInner.setDarkTheme(newTheme)
                    }
                )
            }
        }
    }
}



@Composable
fun MainScreen(
     modifier: Modifier = Modifier,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,

) {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDest = if (currentUser != null) Screen.HomeScreen.route else Screen.Onboarding.route

    NavHost(
      navController = navController, startDestination = startDest, builder = {
          composable(Screen.Onboarding.route) {
              OnboardingScreen( navController, darkTheme, onThemeToggle)
          }
          composable(Screen.LoginScreen.route) {
              val viewModel: AuthViewModel = viewModel()

              LoginScreen(
                  navController,
                  darkTheme,
                  onThemeToggle,
                  viewModel = viewModel
              )
          }
          composable(Screen.SignupScreen.route) {
              val viewModel: AuthViewModel = viewModel()

              SignupScreen(
                  navController,
                  darkTheme,
                  onThemeToggle,
                  viewModel = viewModel
              )
          }
          composable(Screen.ForgetPassword.route) {
              val viewModel: AuthViewModel = viewModel()
              ForgetPassword(
                  navController,
                  darkTheme,
                  onThemeToggle,
                  viewModel = viewModel
              )
          }
          composable(Screen.HomeScreen.route) {
              val viewModel : HomeViewModel = viewModel()
              HomeScreen(
                  navController,
                  darkTheme,
                  onThemeToggle,
                  viewModel = viewModel
              )
          }
          composable(Screen.Profile.route) {
            val viewModel : AuthViewModel = viewModel()
            ProfileScreen(
                navController,
                darkTheme,
                onThemeToggle,
                viewModel = viewModel
                )
            }
          composable(Screen.AddDelivery.route) {
              val viewModel: HomeViewModel = viewModel()
              AddDeliveryScreen(
                  navController,
                  darkTheme,
                  onThemeToggle,
                  viewModel
              )
          }
          composable(Screen.DeliveriesList.route) {
              val viewModel: HomeViewModel = viewModel()
              DeliveriesListScreen(navController, viewModel)
          }
          composable(Screen.SalesRecord.route) {
              val viewModel: HomeViewModel = viewModel()
              SalesRecordScreen(navController, viewModel)
          }

          composable(Screen.Reports.route) {
              val viewModel: HomeViewModel = viewModel()
              ReportsScreen(navController, viewModel)
          }

      }
  )
}
