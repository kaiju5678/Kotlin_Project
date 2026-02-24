package com.example.exercise1.order

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.exercise1.R

@Composable
fun OrderDetail(
    sharedViewModel: SharedViewModel,
    orderViewModel: OrderViewModel2 ,
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        val selectedOption = sharedViewModel.size
        val quantity = sharedViewModel.num
        val detailbox = sharedViewModel.note
        Image(
            modifier = Modifier.aspectRatio(16 / 9f),
            painter = painterResource(R.drawable.image_bg),
            contentDescription = "Milkteashop",
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "รายการที่สั่ง", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "ขนาด: $selectedOption")
        Text(text = "จำนวน: $quantity")
        Text(text = "รายละเอียดเพิ่มเติม: $detailbox")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                orderViewModel.insertOrder(
                    size = selectedOption,
                    num = quantity,
                    note = detailbox
                )
            }) {
                Text("สั่งเลย")
            }
            Button(onClick = {navController.navigate("home")}) {Text("ยกเลิก") }
        }
    }
}