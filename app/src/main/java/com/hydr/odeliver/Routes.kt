package com.hydr.odeliver

sealed class Screen(val route: String){
    object Onboarding : Screen("onboarding")
    object LoginScreen : Screen("loginScreen")
    object SignupScreen : Screen("signupScreen")
    object ForgetPassword : Screen("forgetPassword")
    object HomeScreen : Screen("homeScreen")

    object Profile : Screen("profile")
    object AddDelivery : Screen("addDelivery")
    object DeliveriesList : Screen("deliveriesList")
    object SalesRecord : Screen("salesRecord")

}
