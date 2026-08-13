package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
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
                            fontWeight = FontWeight.Bold,
                            color = GoldGold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("transactions_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize().testTag("transactions_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
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
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد أي عمليات مسجلة حالياً.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "مراجعة العمليات السابقة واستعادة كروت الشحن المتولدة:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextBody, fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = TextAlign.Start
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
    
    // First, check if it contains explicit voucher terms
    val hasVoucherKeyword = lower.contains("كارت") || 
                            lower.contains("توليد") || 
                            lower.contains("كروت") || 
                            lower.contains("إنشاء") || 
                            lower.contains("كود الكارت") ||
                            lower.contains("voucher")
                            
    if (!hasVoucherKeyword) return false
    
    // Exclude direct renewals or activations that aren't voucher generations
    if (lower.contains("مباشرة على الحساب") || lower.contains("شحن الرصيد") || lower.contains("إيداع") || lower.contains("deposit")) {
        // Unless it explicitly contains "توليد كارت" or "توليد كروت" or "إنشاء كارت"
        if (!lower.contains("توليد كارت") && !lower.contains("إنشاء كارت") && !lower.contains("توليد كروت")) {
            return false
        }
    }
    
    // If it's a direct package renewal/activation without card generation, ignore
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
            .border(1.dp, GoldGold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .testTag("transaction_item_${txn.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = formatTxnDate(txn.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                // Amount & Type
                val formattedAmount = com.example.config.AppConfig.formatPrice(txn.amount)
                val amountText = if (txn.type == "DEBIT") "- $formattedAmount ${com.example.config.AppConfig.currency}" else "+ $formattedAmount ${com.example.config.AppConfig.currency}"
                val amountColor = if (txn.type == "DEBIT") GoldGold else GoldGold
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = amountColor,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = GoldGold.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = txn.description ?: "عملية شبكة",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextBody,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // Extracted Voucher Code Display and Copy Actions
            if (extractedCode != null && isVoucherTransaction(txn.description)) {
                Spacer(modifier = Modifier.height(14.dp))
                
                // Beautiful voucher card container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldGold.copy(alpha = 0.08f))
                        .border(1.dp, GoldGold.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "كود كارت الشحن",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = extractedCode,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GoldGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )
                            )
                        }
                        
                        IconButton(
                            onClick = { onCopyClick(extractedCode, "كود الكارت") },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldGold)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ كود الكارت",
                                tint = BgDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun extractVoucherCode(description: String?): String? {
    if (description == null) return null
    // Try to find code after "كود الكارت:"
    if (description.contains("كود الكارت:")) {
        val part = description.substringAfter("كود الكارت:").trim()
        val codePart = part.substringBefore(")").trim()
        if (codePart.isNotEmpty() && isValidVoucherCode(codePart)) return codePart
    }
    // Fallback to regex: matches sequence with dashes or pure numeric sequence (6 to 16 characters)
    val regex = """(?:\b\d{3,}(?:-\d{3,})+\b)|(?:\b\d{6,16}\b)""".toRegex()
    val found = regex.find(description)?.value
    return if (isValidVoucherCode(found)) found else null
}

fun formatTxnDate(dateStr: String?): String {
    if (dateStr == null) return ""
    return try {
        // Simple conversion or display as is
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val date = inputFormat.parse(dateStr) ?: return dateStr
        val outputFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateStr.replace("Z", "").replace("T", " ")
    }
}
