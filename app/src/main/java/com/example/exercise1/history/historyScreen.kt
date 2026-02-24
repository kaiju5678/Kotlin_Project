package com.example.exercise1.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exercise1.firebasedb.Order
import com.example.exercise1.firebasedb.OrderViewModel
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier) {
    //------------------- dummy ข้อมูล -------------------
//    val orders = listOf(
//        "001" to Triple("M", 2, "หวาน 25%"),
//        "002" to Triple("S", 3, "เพิ่มไขมุก"),
//        "003" to Triple("L", 2, "-")
//    )

    val orderVm = viewModel<OrderViewModel>()
    val orders by orderVm.orders.collectAsState(initial = emptyList())
    var deleteOrder by remember { mutableStateOf<Order?>(null) }


    var showDeleteDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("ประวัติการสั่งซื้อ", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีการสั่งซื้อ", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders, key = { it.id }) { order ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            value != SwipeToDismissBoxValue.Settled
                        }
                    )
                    //------------------- Popup ยืนยันการลบ -------------------
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("ยืนยันการลบ") },
                            text = { Text("แน่ใจว่าต้องการลบรายการนี้") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        deleteOrder?.let { orderVm.deleteOrder(it.id) }
                                        showDeleteDialog = false
                                    }
                                ) { Text("ลบ") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDeleteDialog = false }
                                ) { Text("ยกเลิก") }
                            }
                        )
                    }
                    //------------------- Swipe ปุ่มแก้ไข/ลบ -------------------
                    val scope = rememberCoroutineScope()
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF5F5F5))
                                    .padding(end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch { dismissState.reset() }
                                        onEditClick(order.id)
                                    },
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFFFC107),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = {
                                        scope.launch { dismissState.reset() }
                                        deleteOrder = order
                                        showDeleteDialog = true },
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFF44336),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        content = { OrderCard(order = order) }
                    )
                }
            }
        }
    }
}

//------------------- แสดงข้อมูลบนการ์ด -------------------
@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ขนาด: ${order.size}", fontWeight = FontWeight.Bold)
                Text("จำนวน: ${order.qty}")
                Text("หมายเหตุ: ${order.note ?: "-"}", color = Color.Gray)
            }
        }
    }
}