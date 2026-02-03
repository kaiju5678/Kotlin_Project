package com.example.exercise1

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.room.Index

@Composable
fun Historypage(viewModel: OrderViewModel){
    val order by viewModel.drinks.collectAsState(initial = emptyList())
    LazyColumn {
        items(order){ orders ->
            Card(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
                Text(text = "Size : ${orders.size}",  fontWeight = FontWeight.Bold)
                Text(text = "Quantity : ${orders.num}")
                orders.note?.let {
                    Text(text = "Note :$it")
                }
            }
        }
    }
}