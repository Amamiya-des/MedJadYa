package com.example.medjadya

sealed class Screen(val route: String, val name: String) {
    data object Login : Screen(route = "login_screen", name = "Login")
    data object Register : Screen(route = "register_screen", name = "Register")
    data object Profile : Screen(route = "profile_screen", name = "Profile")
}
