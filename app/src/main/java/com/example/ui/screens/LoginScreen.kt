package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.ui.theme.*

enum class LoginTab {
    SUBSCRIBER, VOUCHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isLoggingIn: Boolean,
    isCheckingVoucher: Boolean,
    onLoginClick: (String, String) -> Unit,
    onVoucherClick: (String) -> Unit,
    themeMode: Int = 0,
    onToggleTheme: () -> Unit = {},
    tenantSystemName: String = AppConfig.tenantSystemName
) {
    var activeTab by remember { mutableStateOf(LoginTab.SUBSCRIBER) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var voucherCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("login_screen")
    ) {
        // Floating Theme Toggle Button at top-right
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            val themeIcon = if (themeMode == 2) {
                Icons.Default.WbSunny
            } else {
                Icons.Default.NightsStay
            }
            Icon(
                imageVector = themeIcon,
                contentDescription = "تبديل المظهر",
                tint = GoldGold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "مرحباً بكم في شبكة",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = tenantSystemName.ifBlank { "شبكة MSR" },
                style = MaterialTheme.typography.titleLarge.copy(
                    color = GoldGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("network_title").padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Custom Segmented Tab Swapper (Arabic RTL aligned)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, GoldGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Subscriber Mode (Left/Right depending on preference, we keep Arabic RTL logical flow)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTab == LoginTab.SUBSCRIBER) GoldGold else Color.Transparent)
                        .clickable { activeTab = LoginTab.SUBSCRIBER }
                        .testTag("tab_subscriber"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (activeTab == LoginTab.SUBSCRIBER) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حساب مشترك",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LoginTab.SUBSCRIBER) Color.White else TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Tab 2: Voucher Mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTab == LoginTab.VOUCHER) GoldGold else Color.Transparent)
                        .clickable { activeTab = LoginTab.VOUCHER }
                        .testTag("tab_voucher"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = if (activeTab == LoginTab.VOUCHER) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فحص كارت",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LoginTab.VOUCHER) Color.White else TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Swappable Content Panels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                if (activeTab == LoginTab.SUBSCRIBER) {
                    // Subscriber Panel
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تسجيل دخول المشتركين",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Start
                        )

                        // Username input
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("اسم المستخدم") },
                            placeholder = { Text("أدخل اسم المستخدم الخاص بك") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GoldGold
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextBody,
                                unfocusedTextColor = TextBody,
                                focusedBorderColor = GoldGold,
                                unfocusedBorderColor = GoldGold.copy(alpha = 0.5f),
                                focusedLabelColor = GoldGold,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = GoldGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password input
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("كلمة المرور") },
                            placeholder = { Text("أدخل كلمة المرور الخاصة بك") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GoldGold
                                )
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = "عرض كلمة المرور", tint = GoldGold)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextBody,
                                unfocusedTextColor = TextBody,
                                focusedBorderColor = GoldGold,
                                unfocusedBorderColor = GoldGold.copy(alpha = 0.5f),
                                focusedLabelColor = GoldGold,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = GoldGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Primary login action
                        Button(
                            onClick = { onLoginClick(username, password) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldGold,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoggingIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_button")
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "تسجيل الدخول",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Voucher Panel
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "استعلام عن كارت شبكة الميكروتك",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Start
                        )

                        Text(
                            text = "أدخل كود الكارت لعرض الرصيد المتبقي وفترة الصلاحية دون الحاجة لتسجيل حساب كامل.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            textAlign = TextAlign.Start
                        )

                        // Voucher Code input
                        OutlinedTextField(
                            value = voucherCode,
                            onValueChange = { voucherCode = it },
                            label = { Text("كود الكارت") },
                            placeholder = { Text("مثال: ABCD-EFGH") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = GoldGold
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextBody,
                                unfocusedTextColor = TextBody,
                                focusedBorderColor = GoldGold,
                                unfocusedBorderColor = GoldGold.copy(alpha = 0.5f),
                                focusedLabelColor = GoldGold,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = GoldGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voucher_code_input")
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Voucher check action
                        Button(
                            onClick = { onVoucherClick(voucherCode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldGold,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isCheckingVoucher,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("check_voucher_button")
                        ) {
                            if (isCheckingVoucher) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "فحص الكارت",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
