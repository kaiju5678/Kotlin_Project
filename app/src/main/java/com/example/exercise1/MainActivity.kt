package com.example.exercise1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.exercise1.history.HistoryScreen
import com.example.exercise1.order.EditOrderScreen
import com.example.exercise1.order.OrderScreen
import com.example.exercise1.ui.theme.Exercise1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Exercise1Theme() {
                LayoutScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "History")
    val iconsmenu = listOf(Icons.Default.Home, Icons.Default.History)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6D9E51),
                    titleContentColor = Color(0xFFFEFFD3)
                ),
                title = { Text("Shop App") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF6D9E51),
                contentColor = Color(0xFFFEFFD3)
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(iconsmenu[index], contentDescription = item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index
                            when(index) {
                                0 -> navController.navigate("home")
                                1 -> navController.navigate("history")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White,
                            indicatorColor = Color(0xFFCDB885)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if(currentRoute == "home" || currentRoute == "history") {
                FloatingActionButton(
                    onClick = { navController.navigate("order") },
                    containerColor = Color(0xFF6D9E51)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "สั่งเพิ่ม",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("order") { OrderScreen(
                onOrderClick = { navController.navigate("history") }
            ) }
            composable("history") { HistoryScreen(
                onEditClick = { orderId ->
                    navController.navigate("edit_order/$orderId")
                }
            ) }
            composable("edit_order/{orderid}",
                arguments = listOf(navArgument("orderid") { type = NavType.StringType })) {
                    stackEntry -> val orderid = stackEntry.arguments?.getString("orderid") ?: ""
                EditOrderScreen(orderID = orderid, onBack = { navController.popBackStack() } )
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("หน้าแรก")
        }
    }
}


