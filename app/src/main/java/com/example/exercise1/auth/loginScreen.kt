package com.example.exercise1.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exercise1.R


@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    // ดึง Context สำหรับใช้แสดง Toast
    val context = LocalContext.current

    // ดึงสถานะปัจจุบันมาเฝ้าดู
    val authState by authViewModel.authState.collectAsState()

    // ทำงานเมื่อ authState มีการเปลี่ยนแปลง
    LaunchedEffect(key1 = authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                // ล็อกอินสำเร็จ: รีเซ็ตสถานะก่อน แล้วค่อยเปลี่ยนหน้า
                authViewModel.resetState()
                onLoginSuccess()
            }
            is AuthViewModel.AuthState.ResetPasswordSent -> {
                // ส่งอีเมลสำเร็จ ให้โชว์ Toast
                android.widget.Toast.makeText(
                    context,
                    "ส่งลิงก์รีเซ็ตรหัสผ่านไปยังอีเมลแล้ว!",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                authViewModel.resetState()
            }
            else -> {} // สถานะอื่นปล่อยผ่าน
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetEmail = ""
            },
            title = { Text("ลืมรหัสผ่าน") },
            text = {
                Column {
                    Text("กรอก Email ที่ใช้สมัครสมาชิก\nระบบจะส่งลิงก์รีเซ็ตรหัสผ่านให้")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    // แก้ไขใส่ Logic รีเซ็ตรหัสผ่าน
                    onClick = {
                        authViewModel.resetPassword(resetEmail)
                        showForgotDialog = false
                    },
                    enabled = resetEmail.isNotBlank(),
                ) { Text("ส่ง Email", color = Color(0xFF6D9E51)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail = ""
                }) { Text("ยกเลิก", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFE3DBBB)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sign in", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Visibility"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                resetEmail = email // นำอีเมลที่กรอกไว้ไปใส่ในช่อง reset อัตโนมัติ
                showForgotDialog = true
            }) {
                Text("ลืมรหัสผ่าน?", color = Color(0xFF6D9E51))
            }
        }
        Spacer(Modifier.height(12.dp))

        // --- แสดงข้อความ Error แจ้งเตือน ถ้าล็อกอินผิด ---
        if (authState is AuthViewModel.AuthState.Error) {
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = Color.Red,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            // แก้ไขใส่ Logic ล็อกอิน
            onClick = { authViewModel.loginWithEmail(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(6.dp),
            // ปิดปุ่มถ้ารหัสยังไม่กรอก หรือกำลังโหลดอยู่
            enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthViewModel.AuthState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6D9E51),
                contentColor = Color.White
            )
        ) {
            // โชว์วงกลมโหลด หรือ Text
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("เข้าสู่ระบบ", color = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("ยังไม่เป็นสมาชิก? สมัครสมาชิก", color = Color(0xFF6D9E51))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            // 1. เรียกใช้งาน authViewModel.loginWithGoogle และส่ง context เข้าไป
            onClick = { authViewModel.loginWithGoogle(context) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            // 2. เช็คว่าถ้ากำลังโหลดอยู่ ให้โชว์วงกลมหมุนๆ แทนข้อความ
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign in with Google", color = Color.Black)
            }
        }
    }
}