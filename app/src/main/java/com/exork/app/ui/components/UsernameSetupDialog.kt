package com.exork.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exork.app.ui.theme.MonarchSlate
import com.exork.app.ui.theme.ElectricCyan
import com.exork.app.viewmodel.UsernameValidation

@Composable
fun UsernameSetupDialog(
    onConfirm: (String) -> Unit,
    validationState: UsernameValidation,
    onNameChange: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { }, // Force setup
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MonarchSlate,
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "IDENTITY ESTABLISHMENT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "ENTER YOUR HUNTER NAME",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        if (it.length <= 15) {
                            name = it
                            onNameChange(it)
                        }
                    },
                    label = { Text("HUNTER NAME") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Live Validation Text
                val validationText = when(validationState) {
                    UsernameValidation.AVAILABLE -> "Name available!"
                    UsernameValidation.TAKEN -> "Name already taken!"
                    UsernameValidation.VALIDATING -> "Checking system records..."
                    UsernameValidation.ERROR -> "System error. Try again."
                    else -> ""
                }
                val validationColor = when(validationState) {
                    UsernameValidation.AVAILABLE -> Color.Green
                    UsernameValidation.TAKEN -> Color.Red
                    else -> Color.Gray
                }
                
                Text(
                    text = validationText,
                    color = validationColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onConfirm(name) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = validationState == UsernameValidation.AVAILABLE && name.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black)
                ) {
                    Text("CONFIRM IDENTITY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
