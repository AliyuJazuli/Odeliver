package com.hydr.odeliver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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

import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.room.util.convertUUIDToByte
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val themeManager = ThemeManager(this)
        val isDark = themeManager.isDarkTheme()
        
        // Explicitly set the splash theme based on user preference to override system theme
        if (isDark) {
            setTheme(R.style.Theme_App_Starting_Dark)
        } else {
            setTheme(R.style.Theme_App_Starting)
        }

        val splashScreen = installSplashScreen()
        
        splashScreen.setKeepOnScreenCondition {
            !viewModel.navigationState.value.isInitialized
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        askNotificationPermission()
        
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
                    },
                    viewModel = viewModel
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
    viewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navState by viewModel.navigationState.collectAsState()

    if (!navState.isInitialized) {
        return
    }
    
    val startDest = when {
        !navState.isOnboardingCompleted -> Screen.Onboarding.route
        !navState.isProfileComplete -> Screen.SetProfileScreen.route
        else -> Screen.HomeScreen.route
    }

    NavHost(
        navController = navController, startDestination = startDest, builder = {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(navController, darkTheme, onThemeToggle, viewModel)
            }
            composable(Screen.HomeScreen.route) {
                HomeScreen(
                    navController,
                    darkTheme,
                    onThemeToggle,
                    viewModel = viewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController,
                    darkTheme,
                    onThemeToggle,
                    viewModel = viewModel
                )
            }
            composable(Screen.AddDelivery.route) {
                AddDeliveryScreen(
                    navController,
                    darkTheme,
                    onThemeToggle,
                    viewModel
                )
            }
            composable(Screen.DeliveriesList.route) {
                DeliveriesListScreen(navController, viewModel)
            }
            composable(Screen.SalesRecord.route) {
                SalesRecordScreen(navController, darkTheme = darkTheme, onThemeToggle = onThemeToggle, viewModel = viewModel)
            }
            composable(Screen.SetProfileScreen.route) {
                SetProfileScreen(
                    navController,
                    darkTheme,
                    onThemeToggle,
                    viewModel
                )
            }

            composable(
                route = "reports?period={period}",
                arguments = listOf(
                    navArgument("period") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val periodStr = backStackEntry.arguments?.getString("period")
                val initialPeriod = when (periodStr) {
                    "WEEKLY" -> ReportPeriod.WEEKLY
                    "DAILY" -> ReportPeriod.DAILY
                    else -> ReportPeriod.MONTHLY
                }
                ReportsScreen(
                    navController,
                    darkTheme = darkTheme,
                    onThemeToggle = onThemeToggle,
                    viewModel = viewModel,
                    initialPeriod = initialPeriod
                )

            }

        }
    )
}
