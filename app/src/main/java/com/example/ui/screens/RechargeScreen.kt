package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    walletPhone: String,
    currentAltPhone: String,
    isUpdating: Boolean,
    onBack: () -> Unit,
    onSaveAltPhone: (String) -> Unit
) {
    var altPhoneInput by remember { mutableStateOf(currentAltPhone) }
    val clipboardManager = LocalClipboardManager.current
    val adminPhone = walletPhone.ifEmpty { AppConfig.walletPhone }

    LaunchedEffect(currentAltPhone) {
        altPhoneInput = currentAltPhone
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "شحن الرصيد",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldGold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("recharge_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize().testTag("recharge_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Info Box
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldGold.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شحن رصيد المحفظة عبر تحويل كاش",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldGold
                            ),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = null,
                            tint = GoldGold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "لإضافة رصيد، قم بتحويل المبلغ المطلوب إلى الرقم التالي وسيصلك الرصيد مباشرة:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextBody,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Copyable Admin Wallet Phone Number Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgDark)
                            .border(1.dp, GoldGold, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (adminPhone.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(adminPhone))
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldGold.copy(alpha = 0.15f))
                                .testTag("copy_admin_phone_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ رقم المحفظة",
                                tint = GoldGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = adminPhone.ifEmpty { "جاري التحميل..." },
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = GoldGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.testTag("admin_phone_text")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 2: Alternative Phone Field
            Text(
                text = "هل ستحول من رقم آخر؟",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GoldGold,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "إذا كنت ستستخدم رقم كاش يختلف عن رقمك المسجل لدينا، يجب كتابته هنا والضغط على تأكيد قبل إرسال المبلغ.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alternative Phone input
            OutlinedTextField(
                value = altPhoneInput,
                onValueChange = { altPhoneInput = it },
                label = { Text("رقم الكاش الذي ستحول منه") },
                placeholder = { Text("رقم الكاش الذي ستحول منه") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
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
                    .testTag("alt_phone_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Alternative phone trigger
            Button(
                onClick = { onSaveAltPhone(altPhoneInput) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldGold,
                    contentColor = BgDark
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_alt_phone_button")
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = BgDark, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "تأكيد الرقم قبل التحويل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BgDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BgDark
                        )
                    }
                }
            }
        }
    }
}
