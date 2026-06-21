package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.GlassBox
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: MainViewModel, onOnboardingComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("6000") }
    var enableBiometrics by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // Blurred Glassmorphic Background (zinc & red accents)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(85.dp)
        ) {
            drawCircle(Color(0xFF020203).copy(alpha = 0.08f), radius = size.width * 0.7f, center = Offset(size.width * 0.2f, size.height * 0.1f))
            drawCircle(Color(0xFFEA3B35).copy(alpha = 0.06f), radius = size.width * 0.8f, center = Offset(size.width * 0.8f, size.height * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                )
                Text(
                    text = "Expense Tracker",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                )
                Text(
                    text = "Let's personalize your dashboard",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondary
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Glassmorphic Input Setup Box
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(24.dp),
                isDark = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    // Name Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("What is your name?") },
                        leadingIcon = { Icon(Icons.Rounded.Face, contentDescription = "Name", tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = CardSurface,
                            focusedLabelColor = PrimaryAccent,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryAccent
                        )
                    )

                    // Budget Input
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Monthly Budget Limit (৳)") },
                        leadingIcon = { Icon(Icons.Rounded.Wallet, contentDescription = "Budget", tint = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_budget"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = CardSurface,
                            focusedLabelColor = PrimaryAccent,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryAccent
                        )
                    )

                    // Biometric Lock Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fingerprint,
                                contentDescription = "Biometrics",
                                tint = PrimaryAccent
                            )
                            Column {
                                  Text(
                                      text = "Enable Biometric Lock",
                                      style = MaterialTheme.typography.bodyLarge.copy(
                                          fontWeight = FontWeight.SemiBold,
                                          color = TextPrimary
                                      )
                                  )
                                Text(
                                    text = "Protect your app access",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = enableBiometrics,
                            onCheckedChange = { enableBiometrics = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryAccent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = CardSurface
                            ),
                            modifier = Modifier.testTag("onboarding_biometrics_toggle")
                        )
                    }
                }
            }

            // Start Button
            Button(
                onClick = {
                    val finalName = name.ifEmpty { "User" }
                    val finalBudget = budget.toDoubleOrNull() ?: 6000.0
                    viewModel.completeOnboarding(finalName, finalBudget, enableBiometrics)
                    onOnboardingComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_start_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccent,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = "Check")
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
