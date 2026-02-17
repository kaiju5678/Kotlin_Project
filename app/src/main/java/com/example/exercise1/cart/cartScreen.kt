package com.example.exercise1.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.exercise1.api_screen.ProductRepository
import com.example.exercise1.api_screen.ProductViewModel
import com.example.exercise1.api_screen.ProductViewModelFactory
import com.example.exercise1.api_screen.Resource
import androidx.compose.material.icons.filled.ArrowBack
// import coil.compose.AsyncImage // เปิดใช้บรรทัดนี้ถ้าลง library coil แล้ว

// ==========================================
// ส่วนที่ 1: หน้าเลือกซื้อสินค้า (มาแทน AllProductsScreen อันเก่า)
// ==========================================
@Composable
fun NewShoppingScreen(onCartClick: () -> Unit) {
    val context = LocalContext.current

    // 1. ประกาศ ViewModel
    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(ProductRepository())
    )
    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(context)
    )

    // สั่งโหลดข้อมูล
    LaunchedEffect(Unit) {
        productViewModel.loadAllProduct()
    }

    val state = productViewModel.allProduct.observeAsState(initial = Resource.Loading())

    // 2. ใช้ Box แทน Scaffold
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // ใส่สีพื้นหลังให้ดูดีขึ้น
    ) {
        // --- Layer 1: เนื้อหา (Content) ---
        when (val resource = state.value) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6B4A2D))
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${resource.message}", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val products = resource.data ?: emptyList()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp), // เว้นขอบซ้ายขวา
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp), // *สำคัญ* เว้นขอบล่างเยอะๆ กันปุ่ม FAB บังสินค้าชิ้นสุดท้าย
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        Card(
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // รูปภาพสินค้า
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(model = product.image, contentDescription = null)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // รายละเอียดสินค้า
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                    Text("${product.price} $", color = Color(0xFF6B4A2D))

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            cartViewModel.addToCart(
                                                id = product.id,
                                                title = product.title,
                                                price = product.price,
                                                category = product.category,
                                                image = product.image
                                            )
                                        },
                                        modifier = Modifier.height(35.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4A2D))
                                    ) {
                                        Text("Add to Cart", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Layer 2: ปุ่มตะกร้า (FAB) ลอยอยู่ข้างบน ---
        FloatingActionButton(
            onClick = onCartClick,
            containerColor = Color(0xFF6B4A2D),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd) // จัดให้อยู่มุมขวาล่าง
                .padding(16.dp) // เว้นระยะจากขอบจอ
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Go to Cart")
        }
    }
}

// ==========================================
// ส่วนที่ 2: หน้าตะกร้าสินค้า (แสดงรายการที่กดเลือกมา)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
// ในไฟล์ cartScreen.kt

@Composable
fun MyCartPage(navController: NavController) {
    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(context)
    )
    // ดึงข้อมูลสินค้าในตะกร้า
    val cartItems by cartViewModel.cartItems.collectAsState(initial = emptyList())
    // คำนวณราคารวม
    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    // ใช้ Column แทน Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)) // ใส่สีพื้นหลังให้อ่อนๆ เพื่อความสวยงาม
            .padding(16.dp)
    ) {
        // ---------------------------------------------------------
        // ส่วนหัว (Header): ปุ่มย้อนกลับ + หัวข้อ + ปุ่มลบ
        // ---------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ปุ่มย้อนกลับ (Back Button)
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // ต้อง import androidx.compose.material.icons.filled.ArrowBack
                    contentDescription = "Back",
                    tint = Color(0xFF6B4A2D)
                )
            }

            Text(
                text = "ตะกร้าของฉัน",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B4A2D),
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ปุ่มลบทั้งหมด (แสดงเฉพาะตอนมีของ)
            if (cartItems.isNotEmpty()) {
                IconButton(onClick = { cartViewModel.clearAll() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------------------------------------------------
        // ส่วนรายการสินค้า (Content): ใช้ weight(1f) เพื่อดันส่วนล่างลงไป
        // ---------------------------------------------------------
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ยังไม่มีสินค้าในตะกร้า", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // แสดงชื่อและราคา
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${item.price} $", fontSize = 14.sp, color = Color(0xFF6B4A2D))
                                Text("รวม: ${(item.price * item.quantity)} $", fontSize = 12.sp, color = Color.Gray)
                            }

                            // ปุ่ม เพิ่ม-ลด จำนวน
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { cartViewModel.updateQuantity(item, item.quantity - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null)
                                }

                                Text(
                                    text = "${item.quantity}",
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { cartViewModel.updateQuantity(item, item.quantity + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------------------------------------------------
        // ส่วนท้าย (Footer): แสดงยอดรวมและปุ่มสั่งซื้อ (อยู่ด้านล่างสุดของ Column)
        // ---------------------------------------------------------
        if (cartItems.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ยอดรวมทั้งสิ้น", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "%.2f $".format(totalPrice),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B4A2D)
                        )
                    }

                    Button(
                        onClick = { /* สั่งซื้อ */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4A2D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ชำระเงิน", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}