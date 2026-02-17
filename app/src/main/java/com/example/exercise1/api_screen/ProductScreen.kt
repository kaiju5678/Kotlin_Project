package com.example.exercise1.api_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.exercise1.cart.CartViewModel
import com.example.exercise1.cart.CartViewModelFactory

@Composable
fun ProductScreen(
    productID: Int,
    viewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(ProductRepository())
    )) {
    val state = viewModel.product.observeAsState()
    LaunchedEffect(productID) {
        viewModel.loadProduct(productID)
    }
    when(val result = state.value){
        is Resource.Loading -> { CircularProgressIndicator() }
        is Resource.Success -> result.data?.let {
            ProductItem(it)
        }
        is Resource.Error -> { Text(text = result.message ?: "Error")}
        null -> Unit
    }
}

@Composable
fun AllProductsScreen(
    viewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(ProductRepository())
    )
) {
    val state = viewModel.allProduct.observeAsState()
    LaunchedEffect(Unit) {
        viewModel.loadAllProduct()
    }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        when(val result = state.value){
            is Resource.Loading -> { CircularProgressIndicator()}
            is Resource.Success -> {
                LazyColumn {  items(result.data ?: emptyList()){
                    product -> ProductItem(product)
                } }
            }
            is Resource.Error -> { Text(text = result.message ?: "Error")}
            null -> Unit
        }
    }
}


@Composable
fun ProductItem(product: Products){
    Card(modifier = Modifier.fillMaxWidth()
        .padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = product.image,
                contentDescription = product.title,
                modifier = Modifier.fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit)
            Text(product.title, fontWeight = FontWeight.Bold)
            Text("Price: $${product.price}")
            Text("Category: ${product.category}")
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Rating: ${product.rating.rate} (${product.rating.count})")
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Cart"
                    )
                }
            }


        }
    }
}