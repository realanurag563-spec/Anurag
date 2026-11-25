package com.example.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun parseRichBoldText(input: String): AnnotatedString {
    return buildAnnotatedString {
        val cleaned = input.replace("\r\n", "\n")
        var idx = 0
        while (idx < cleaned.length) {
            val startBold = cleaned.indexOf("**", idx)
            if (startBold == -1) {
                append(cleaned.substring(idx))
                break
            }
            if (startBold > idx) {
                append(cleaned.substring(idx, startBold))
            }
            val endBold = cleaned.indexOf("**", startBold + 2)
            if (endBold == -1) {
                append(cleaned.substring(startBold))
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(cleaned.substring(startBold + 2, endBold))
            }
            idx = endBold + 2
        }
    }
}
