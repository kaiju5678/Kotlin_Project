package com.example.exercise1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.exercise1.api_screen.AllProductsScreen
import com.example.exercise1.api_screen.TeamScreen
import com.example.exercise1.history.EditScreen
import com.example.exercise1.history.Historypage
import com.example.exercise1.order.OrderDetail
import com.example.exercise1.order.OrderViewModel
import com.example.exercise1.order.OrderViewModelFactory
import com.example.exercise1.order.SharedViewModel
import com.example.exercise1.ui.theme.Exercise1Theme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Exercise1Theme {
                val context = LocalContext.current
                val orderviewModel: OrderViewModel = viewModel(
                    factory = OrderViewModelFactory(context)
                )
                val sharedViewModel: SharedViewModel = viewModel()
                val navController = rememberNavController()
                var selecteditem by remember { mutableStateOf(0) }
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.History
                )
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("Shop App")
                            },
                            actions = {
                                // Place IconButton(s) inside this RowScope
                                IconButton({}) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = "Settings"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF6B4A2D), // Set the background color
                                titleContentColor = Color.White, // Optional: Change title color for contrast
                                navigationIconContentColor = Color.White, // Optional: Change navigation icon color
                                actionIconContentColor = Color.White // Optional: Change action icons color
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF6E5034),
                            contentColor = Color.Blue
                        ) {
                            icons.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = icons[index],
                                            contentDescription = if (index == 0) "Home" else "History"
                                        )
                                    },
                                    selected = selecteditem == index,
                                    onClick = {
                                        selecteditem = index
                                        when (index) {
                                            0 -> navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                            }

                                            1 -> navController.navigate("history")
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color.Gray,
                                        indicatorColor = Color(0xFFEFE185)
                                    )
                                )
                            }
                        }
                    },

                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = "new_shop"
                    ) {
                        composable("home") {
                            MilkteaShop(navController = navController, sharedViewModel)
                        }
                        composable(
                            route = "confirm"
                        ) {
                            OrderDetail(
                                sharedViewModel = sharedViewModel,
                                navController = navController,
                                orderViewModel = orderviewModel
                            )
                        }
                        composable(
                            route = "history"
                        ) {
                            Historypage(orderviewModel, navController = navController)
                        }
                        composable("edit/{id}", arguments = listOf(navArgument("id") {
                            type = NavType.IntType
                        })) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            EditScreen(
                                drinkID = id,
                                viewModel = orderviewModel,
                                navController = navController,
                                sharedViewModel = sharedViewModel
                            )
                        }
                        composable("product") {
                            AllProductsScreen()
                        }
                        composable("team") {
                            TeamScreen()
                        }
                        // 1. เพิ่ม Route สำหรับหน้าเลือกซื้อสินค้า (อันใหม่ที่เราเพิ่งสร้าง)
                        composable("new_shop") {
                            com.example.exercise1.cart.NewShoppingScreen(navController)
                        }

                        // 2. เพิ่ม Route สำหรับหน้าตะกร้า
                        composable("my_cart_page") {
                            com.example.exercise1.cart.MyCartPage(navController)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MilkteaShop(navController: NavController, sharedViewModel: SharedViewModel) {
    val radioOptions = listOf("S", "M", "L")
    var selectedOption by remember { mutableStateOf(radioOptions[0]) }
    var detailbox by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Image(
            modifier = Modifier.aspectRatio(16 / 9f),
            painter = painterResource(R.drawable.image_bg),
            contentDescription = "Milkteashop",
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "ชานมข้าวหอม", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Rice Milk Tea")

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "ขนาด: ")
            radioOptions.forEach { size ->
                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    RadioButton(
                        selected = (selectedOption == size),
                        onClick = { selectedOption = size }
                    )
                    Text(
                        text = size
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "รายละเอียดเพิ่มเติม")

        OutlinedTextField(
            value = detailbox,
            onValueChange = { detailbox = it },
            label = { Text("หวานน้อย เพิ่มหวาน") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "จำนวน :")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(onClick = {
                if (quantity > 1) quantity--
            }) {
                Icon(
                    imageVector = Icons.Outlined.RemoveCircleOutline,
                    contentDescription = "minus"
                )
            }
            Text(quantity.toString(), modifier = Modifier.padding(horizontal = 64.dp))
            IconButton(onClick = {
                quantity++
            }) {
                Icon(
                    imageVector = Icons.Outlined.AddCircleOutline,
                    contentDescription = "add"
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .clickable {
                    sharedViewModel.setOrder(
                        size = selectedOption,
                        num = quantity,
                        note = detailbox
                    )
                    navController.navigate("confirm")
                }
                .background(Color(0xff5DD3B6))
                .padding(15.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "ใส่ตระกร้า", color = Color.White)
        }
    }
}


