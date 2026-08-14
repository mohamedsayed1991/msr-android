package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سجل العمليات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("transactions_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().testTag("transactions_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد أي عمليات مسجلة حالياً.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "مراجعة العمليات السابقة واستعادة كروت الشحن:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                    }

                    items(transactions) { txn ->
                        TransactionItemCard(
                            txn = txn,
                            onCopyClick = { text, label ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "تم نسخ $label بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

fun isValidVoucherCode(code: String?): Boolean {
    if (code == null) return false
    val clean = code.replace("-", "").trim()
    if (clean.isEmpty()) return false
    if (clean.startsWith("BUY", ignoreCase = true) ||
        clean.startsWith("GEN", ignoreCase = true) ||
        clean.startsWith("DEP", ignoreCase = true) ||
        clean.startsWith("TXN", ignoreCase = true)) {
        return false
    }
    if (clean.length > 16 || clean.length < 6) return false
    return clean.any { it.isDigit() }
}

fun isVoucherTransaction(description: String?): Boolean {
    if (description == null) return false
    val lower = description.lowercase()
    val hasVoucherKeyword = lower.contains("كارت") ||
            lower.contains("توليد") ||
            lower.contains("كروت") ||
            lower.contains("إنشاء") ||
            lower.contains("كود الكارت") ||
            lower.contains("voucher")
    if (!hasVoucherKeyword) return false
    if (lower.contains("مباشرة على الحساب") || lower.contains("شحن الرصيد") || lower.contains("إيداع") || lower.contains("deposit")) {
        if (!lower.contains("توليد كارت") && !lower.contains("إنشاء كارت") && !lower.contains("توليد كروت")) {
            return false
        }
    }
    if ((lower.contains("تجديد باقة") || lower.contains("تفعيل باقة") || lower.contains("اشتراك بالباقة")) &&
        !lower.contains("توليد") && !lower.contains("كارت") && !lower.contains("إنشاء")) {
        return false
    }
    return true
}

@Composable
fun TransactionItemCard(
    txn: Transaction,
    onCopyClick: (String, String) -> Unit
) {
    val rawExtracted = if (!txn.txnId.isNullOrEmpty() && txn.txnId != "null" && isValidVoucherCode(txn.txnId) && isVoucherTransaction(txn.description)) {
        txn.txnId
    } else {
        val fromDesc = if (isVoucherTransaction(txn.description)) extractVoucherCode(txn.description) else null
        if (isValidVoucherCode(fromDesc)) fromDesc else null
    }
    val extractedCode = rawExtracted?.replace("-", "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${txn.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(RadiusXL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTxnDate(txn.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val formattedAmount = com.example.config.AppConfig.formatPrice(txn.amount)
                val amountText = if (txn.type == "DEBIT") "- $formattedAmount ${com.example.config.AppConfig.currency}" else "+ $formattedAmount ${com.example.config.AppConfig.currency}"
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (txn.type == "DEBIT") ErrorRed else SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = txn.description ?: "عملية شبكة",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (extractedCode != null && isVoucherTransaction(txn.description)) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RadiusMD))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "كود كارت الشحن",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = extractedCode,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = { onCopyClick(extractedCode, "كود الكارت") },
                        modifier = Modifier
                            .clip(RoundedCornerShape(RadiusMD))
                            .background(MaterialTheme.colorScheme.primary)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun extractVoucherCode(description: String?): String? {
    if (description == null) return null
    if (description.contains("كود الكارت:")) {
        val part = description.substringAfter("كود الكارت:").trim()
        val codePart = part.substringBefore(")").trim()
        if (codePart.isNotEmpty() && isValidVoucherCode(codePart)) return codePart
    }
    val regex = """(?:\b\d{3,}(?:-\d{3,})+\b)|(?:\b\d{6,16}\b)""".toRegex()
    val found = regex.find(description)?.value
    return if (isValidVoucherCode(found)) found else null
}

fun formatTxnDate(dateStr: String?): String {
    if (dateStr == null) return ""
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val date = inputFormat.parse(dateStr) ?: return dateStr
        val outputFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateStr.replace("Z", "").replace("T", " ")
    }
}
