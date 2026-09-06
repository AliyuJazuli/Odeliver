package com.hydr.odeliver

sealed class Screen(val route: String){
    object HomeScreen : Screen("homeScreen")

    object Profile : Screen("profile")
    object AddDelivery : Screen("addDelivery")
    object DeliveriesList : Screen("deliveriesList")
    object SalesRecord : Screen("salesRecord")
    object SetProfileScreen : Screen("setProfileScreen")
    object Reports : Screen("reports") {
        fun createRoute(period: String? = null) = if (period != null) "reports?period=$period" else "reports"
    }

    object Onboarding : Screen("onboarding")
}
