package com.example.dummytts.ui.theme // Passe Paketnamen an!

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Standard Material 3 Typography Einstellungen
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Hier könnten weitere Textstile definiert werden:
    titleLarge = TextStyle( ... ),
    labelSmall = TextStyle( ... )
    */
)