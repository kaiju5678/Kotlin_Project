package com.example.exercise1.history

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.exercise1.R
import com.example.exercise1.order.OrderViewModel2
import com.example.exercise1.order.SharedViewModel

@Composable
fun EditScreen(drinkID:Int,
               navController: NavController,
               sharedViewModel: SharedViewModel,
               viewModel: OrderViewModel2
){
    val radioOptions = listOf("S", "M", "L")
    var selectedOption by remember { mutableStateOf(radioOptions[0]) }
    var detailbox by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }

    val drinks by viewModel.getDrinkID(drinkID).collectAsState(initial = null)
    LaunchedEffect(drinks) {
        drinks?.let{
            quantity = it.num
            selectedOption = it.size
            detailbox = it.note.toString()
        }
    }


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

        Button(onClick = {
            drinkID?.let{
                viewModel.updateDrink(
                    id = drinkID,
                    size = selectedOption,
                    num = quantity,
                    note = detailbox
                )
            }
            navController.navigate("history")
        },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5DD3B6),
                contentColor = Color.White
            )
        ) {
            Text("แก้ไขข้อมูล")
        }
    }
}

