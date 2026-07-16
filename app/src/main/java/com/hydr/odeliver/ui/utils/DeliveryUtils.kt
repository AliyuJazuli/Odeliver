package com.hydr.odeliver.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydr.odeliver.DeliveryStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val len = originalText.length
        val formatted = StringBuilder()
        for (i in 0 until len) {
            formatted.append(originalText[i])
            if ((len - i - 1) % 3 == 0 && i != len - 1) {
                formatted.append(",")
            }
        }
        val out = "₦$formatted"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset < 0) return 0
                val safeOffset = offset.coerceAtMost(len)
                var commasBefore = 0
                for (i in 0 until safeOffset) {
                    if ((len - i - 1) % 3 == 0 && i != len - 1) {
                        commasBefore++
                    }
                }
                return safeOffset + commasBefore + 1 // +1 for ₦
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 1) return 0
                var originalOffset = 0
                var currentTransformed = 1 // Start after ₦
                for (i in 0 until len) {
                    currentTransformed++ // digit
                    originalOffset++
                    if (currentTransformed >= offset) return originalOffset
                    if ((len - i - 1) % 3 == 0 && i != len - 1) {
                        currentTransformed++ // comma
                        if (currentTransformed >= offset) return originalOffset
                    }
                }
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

fun DeliveryStatus.toDisplayColor(): Color {
    return when (this) {
        DeliveryStatus.PENDING -> Color(0xFFFFA500) // Orange
        DeliveryStatus.ON_THE_WAY -> Color(0xFF2196F3) // Blue
        DeliveryStatus.DELIVERED -> Color(0xFF4CAF50) // Green
        DeliveryStatus.CANCELLED -> Color(0xFFF44336) // Red
    }
}

fun DeliveryStatus.toDisplayText(): String {
    return this.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

fun Double.formatCurrency(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 2
    return "₦" + formatter.format(this)
}

fun String.formatDisplayDate(): String {
    if (this.length != 8) return this
    return try {
        val parser = SimpleDateFormat("ddMMyyyy", Locale.US)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = parser.parse(this)
        formatter.format(date!!)
    } catch (e: Exception) {
        this
    }
}

fun String.formatDisplayTime(): String {
    if (this.length != 4) return this
    return try {
        val parser = SimpleDateFormat("HHmm", Locale.US)
        val formatter = SimpleDateFormat("h:mm a", Locale.US)
        val time = parser.parse(this)
        formatter.format(time!!)
    } catch (e: Exception) {
        this
    }
}

@Composable
fun MoneySegmentedInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxDigits: Int = 12
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { char -> char.isDigit() }
            if (digitsOnly.length <= maxDigits) {
                onValueChange(digitsOnly)
            }
        },
        modifier = modifier.width(180.dp),
        placeholder = { 
            Text(
                "₦0", 
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            ) 
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        visualTransformation = CurrencyVisualTransformation(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.primary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun SegmentedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int,
    mask: String,
    placeholder: String = ""
) {
    // Hidden BasicTextField that covers the entire Row
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { char -> char.isDigit() }
            if (digitsOnly.length <= length) {
                onValueChange(digitsOnly)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                // The actual input is invisible but receives touches
                Box(modifier = Modifier.alpha(0f)) {
                    innerTextField()
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    var charIndex = 0
                    mask.forEach { maskChar ->
                        if (maskChar == '#') {
                            // Use the actual value if present, otherwise use the placeholder digit
                            val isDigitPresent = charIndex < value.length
                            val char = if (isDigitPresent) {
                                value[charIndex].toString()
                            } else if (charIndex < placeholder.length) {
                                placeholder[charIndex].toString()
                            } else {
                                ""
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(width = 32.dp, height = 48.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (charIndex == value.length) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (charIndex == value.length)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    color = if (isDigitPresent)
                                        MaterialTheme.colorScheme.onSurface 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            charIndex++
                        } else {
                            Text(
                                text = maskChar.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

class MaskVisualTransformation(private val mask: String, private val maskChar: Char) : androidx.compose.ui.text.input.VisualTransformation {
    private val digitIndices = mask.indices.filter { mask[it] == maskChar }

    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = text.text.take(digitIndices.size)
        var out = ""
        var maskIndex = 0
        var trimmedIndex = 0

        while (maskIndex < mask.length && trimmedIndex < trimmed.length) {
            if (mask[maskIndex] == maskChar) {
                out += trimmed[trimmedIndex]
                trimmedIndex++
            } else {
                out += mask[maskIndex]
            }
            maskIndex++
        }

        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(out),
            object : androidx.compose.ui.text.input.OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return 0
                    var transformedOffset = 0
                    var originalCount = 0
                    for (i in mask.indices) {
                        if (mask[i] == maskChar) {
                            originalCount++
                        }
                        transformedOffset++
                        if (originalCount == offset) break
                    }
                    return transformedOffset
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 0) return 0
                    var originalOffset = 0
                    for (i in 0 until offset.coerceAtMost(out.length)) {
                        if (mask[i] == maskChar) {
                            originalOffset++
                        }
                    }
                    return originalOffset
                }
            }
        )
    }
}
