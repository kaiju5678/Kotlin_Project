package com.example.exercise1.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel, // ต้องรับ AuthViewModel เข้ามาด้วย
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    // ดึงสถานะปัจจุบันจาก ViewModel มาเพื่อเช็คว่าโหลดอยู่ หรือ มี Error ไหม
    val authState by authViewModel.authState.collectAsState()

    // เช็คสถานะเพื่อเปลี่ยนหน้าเมื่อสำเร็จ
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            onRegisterSuccess() // สั่งเปลี่ยนหน้า
            authViewModel.resetState() // คืนค่าสถานะป้องกันการเด้งซ้ำ
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("สมัครสมาชิก", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; localError = null },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; localError = null },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // --- การจัดการและแสดง Error ---
        // เช็คว่ามี Error จากการพิมพ์ผิด(localError) หรือ Error จาก Firebase(authState)
        val errorMessage = localError ?: if (authState is AuthViewModel.AuthState.Error) {
            (authState as AuthViewModel.AuthState.Error).message
        } else null

        if (errorMessage != null) {
            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
        }

        // --- ปุ่มสมัครสมาชิก ---
        Button(
            onClick = {
                if (password != confirmPassword) {
                    localError = "Password และ Confirm Password ไม่ตรงกัน"
                } else if (password.length < 6) {
                    localError = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"
                } else {
                    localError = null
                    // สั่งเรียกใช้ Firebase Register ตรงนี้!
                    authViewModel.register(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(6.dp),
            // ปิดปุ่มถ้ารหัสยังไม่กรอก หรือกำลังโหลดอยู่
            enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && authState !is AuthViewModel.AuthState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6D9E51),
                contentColor = Color.White)
        ) {
            // โชว์วงกลมหมุนๆ ระหว่างรอ Firebase
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("สมัครสมาชิก", color = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("เป็นสมาชิกแล้ว? เข้าสู่ระบบ", color = Color(0xFF6D9E51))
        }
    }
}