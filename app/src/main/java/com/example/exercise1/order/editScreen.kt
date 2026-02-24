package com.example.exercise1.order

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exercise1.firebasedb.OrderViewModel

@Composable
fun EditOrderScreen(orderID: String, onBack: () -> Unit, modifier: Modifier = Modifier) {

    val orderVm = viewModel<OrderViewModel>()
    val orders by orderVm.orders.collectAsState(initial = emptyList())
    val order = orders.find { it.id == orderID }
    if(order == null){
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
        return
    }
    val radioOptions = listOf("S", "M", "L")
    //------------------- ข้อมูลสมมติ -------------------
    var selectedOption by remember { mutableStateOf(order.size) }
    var qty by remember { mutableStateOf(order.qty) }
    var note by remember { mutableStateOf(order.note?:"") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("แก้ไขออเดอร์", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("ขนาด:")
        Row(modifier = Modifier.fillMaxWidth()) {
            radioOptions.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = { selectedOption = option }
                    )
                    Text(option)
                    Spacer(Modifier.width(20.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("จำนวน:")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (qty > 1) qty-- }) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null)
            }
            Text(qty.toString(), fontSize = 18.sp)
            IconButton(onClick = { qty++ }) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("รายละเอียดเพิ่มเติม:")
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("เช่น หวานน้อย, เพิ่มช็อต") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onBack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("ยกเลิก")
            }
            Button(
                onClick = {
                    orderVm.updateOrder(
                        order.copy(
                            size = selectedOption,
                            qty = qty,
                            note = note.ifBlank { null }
                        )
                    )
                    onBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6D9E51)
                )
            ) {
                Text("แก้ไข", color = Color.White)
            }
        }
    }
}