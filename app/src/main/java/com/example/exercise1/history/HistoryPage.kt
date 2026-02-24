package com.example.exercise1.history

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.exercise1.order.OrderEntity
import com.example.exercise1.order.OrderViewModel2

@Composable
fun Historypage(viewModel: OrderViewModel2, navController: NavController ){
    val order by viewModel.drinks.collectAsState(initial = emptyList())

    var deleteOrder by remember { mutableStateOf<OrderEntity?>(null) }
    LazyColumn {
        items(order){ orders ->
            Row() {


                Text("Size: ${orders.size}, Qty: ${orders.num}, Note: ${orders.note}")
                IconButton(onClick = {navController.navigate("edit/${orders.id}")}) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                }
                IconButton(onClick = {
                    deleteOrder = orders
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }


            deleteOrder?.let { orders ->
                AlertDialog(
                    onDismissRequest = {deleteOrder = null},
                    title = { Text("ยืนยันการลบ")},
                    text = { Text("แน่ใจว่าต้องการลบรายการนี้")},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteDrink(orders)
                                deleteOrder = null
                            }
                        ) { Text("ลบ") }
                    }
                )
            }


//            Card(modifier = Modifier
//                .padding(8.dp)
//                .fillMaxWidth()) {
//                Text(text = "Size : ${orders.size}",  fontWeight = FontWeight.Bold)
//                Text(text = "Quantity : ${orders.num}")
//                orders.note?.let {
//                    Text(text = "Note :$it")
//                }
//            }
        }
    }
}