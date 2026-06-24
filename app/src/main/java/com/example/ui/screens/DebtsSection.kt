package com.example.ui.screens

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DebtDue
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DebtsSection(
    type: String, // "DEBT" or "DUE"
    debtsDues: List<DebtDue>,
    onSettleDebtDue: (DebtDue, logAsExpense: Boolean) -> Unit,
    onDeleteDebtDue: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("pending") } // "all", "pending", "settled"
    
    var itemToSettle by remember { mutableStateOf<DebtDue?>(null) }
    var itemToDelete by remember { mutableStateOf<DebtDue?>(null) }

    val filteredList = remember(searchQuery, statusFilter, debtsDues, type) {
        debtsDues.filter { item ->
            item.type == type &&
            (item.personName.contains(searchQuery, ignoreCase = true) ||
             item.description.contains(searchQuery, ignoreCase = true)) &&
            when (statusFilter) {
                "pending" -> !item.isCleared
                "settled" -> item.isCleared
                else -> true
            }
        }
    }

    val totalAmount = remember(filteredList) {
        filteredList.sumOf { it.amount }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Summary Banner Card
        val labelText = if (type == "DEBT") "Total Owed (Pending)" else "Total Collectible (Pending)"
        val activeTotal = remember(debtsDues, type) {
            debtsDues.filter { it.type == type && !it.isCleared }.sumOf { it.amount }
        }
        
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = labelText,
                    color = DarkCardTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "৳${String.format(Locale.US, "%,.2f", activeTotal)}",
                    color = DarkCardTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Search Outlined Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name or note...") },
            leadingIcon = { Icon(Icons.Rounded.Search, "Search", tint = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = CardSurface,
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "pending" to "Active / Pending",
                "settled" to "Cleared / Settled",
                "all" to "All Logs"
            )
            filters.forEach { (filterKey, filterLabel) ->
                val isSelected = statusFilter == filterKey
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) PrimaryAccent else CardSurface,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { statusFilter = filterKey }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filterLabel,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Debt / Due Items List
        val bottomPadding = 112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "No records found.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    DebtDueItem(
                        item = item,
                        onSettleClick = { itemToSettle = item },
                        onDeleteClick = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // Confirmation Settle Dialog
    if (itemToSettle != null) {
        val target = itemToSettle!!
        if (target.type == "DEBT") {
            // Debt payback requires expense prompt
            AlertDialog(
                onDismissRequest = { itemToSettle = null },
                title = { Text("Clear Debt", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("You are settling the debt of ৳${String.format(Locale.US, "%,.2f", target.amount)} paid to ${target.personName}. Would you like to log this repayment as an expense transaction as well?", color = TextPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onSettleDebtDue(target, true)
                            Toast.makeText(context, "Debt settled and logged as expense!", Toast.LENGTH_SHORT).show()
                            itemToSettle = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Settle & Log Expense", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            onSettleDebtDue(target, false)
                            Toast.makeText(context, "Debt settled without logging expense.", Toast.LENGTH_SHORT).show()
                            itemToSettle = null
                        }) {
                            Text("Settle Only", color = PrimaryAccent, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { itemToSettle = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                },
                containerColor = ThemeBackground,
                shape = RoundedCornerShape(24.dp)
            )
        } else {
            // Due collection is cleared directly (user requested no auto expense)
            AlertDialog(
                onDismissRequest = { itemToSettle = null },
                title = { Text("Clear Receivable", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to mark the receivable of ৳${String.format(Locale.US, "%,.2f", target.amount)} from ${target.personName} as collected / cleared?", color = TextPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onSettleDebtDue(target, false)
                            Toast.makeText(context, "Receivable marked as cleared!", Toast.LENGTH_SHORT).show()
                            itemToSettle = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mark Cleared", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToSettle = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = ThemeBackground,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }

    // Confirmation Delete Dialog
    if (itemToDelete != null) {
        val target = itemToDelete!!
        val isDebt = target.type == "DEBT"
        val typeLabel = if (isDebt) "debt" else "receivable"
        val titleLabel = if (isDebt) "Delete Debt" else "Delete Receivable"
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(titleLabel, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this $typeLabel record with ${target.personName} for ৳${String.format(Locale.US, "%,.2f", target.amount)}? This action cannot be undone.", color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDebtDue(target.id)
                        Toast.makeText(context, "Deleted record!", Toast.LENGTH_SHORT).show()
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = ThemeBackground,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun DebtDueItem(
    item: DebtDue,
    onSettleClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val dateStr = remember(item.date) { dateFormat.format(Date(item.date)) }
    val dueDateStr = remember(item.dueDate) { item.dueDate?.let { dateFormat.format(Date(it)) } }

    // Overdue logic
    val isOverdue = remember(item.dueDate, item.isCleared) {
        !item.isCleared && item.dueDate != null && item.dueDate < System.currentTimeMillis()
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored icon box based on Debt vs Due
                val isDebt = item.type == "DEBT"
                val tintColor = if (isDebt) Color(0xFFEA3B35) else Color(0xFF4CAF50) // Red vs Green
                val bgColor = if (isDebt) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                val icon = if (isDebt) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.type,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.personName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isDebt) "Borrowed" else "Lent"}: $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    if (dueDateStr != null) {
                        Text(
                            text = "Due: $dueDateStr",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isOverdue) Color(0xFFEA3B35) else TextSecondary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳${String.format(Locale.US, "%,.2f", item.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (item.isCleared) {
                        // Cleared badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Settled",
                                color = Color(0xFF2E7D32),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isOverdue) {
                        // Overdue badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Overdue",
                                color = Color(0xFFC62828),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Description and actions row (only for non-cleared, or expand note)
            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ThemeBackground.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action row if active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!item.isCleared) {
                    // Settle Button
                    OutlinedButton(
                        onClick = onSettleClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent),
                        border = BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Settle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
