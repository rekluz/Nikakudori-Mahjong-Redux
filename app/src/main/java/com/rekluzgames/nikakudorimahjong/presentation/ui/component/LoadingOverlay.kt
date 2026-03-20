/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.presentation.ui.theme.MidnightBlue

@Composable
fun LoadingOverlay() {
    val context = LocalContext.current
    val iconId = context.resources.getIdentifier(
        "nikakudorimahjong", "drawable", context.packageName
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue),
        contentAlignment = Alignment.Center
    ) {
        // Background watermark image
        if (iconId != 0) {
            Image(
                painter = painterResource(iconId),
                contentDescription = null,
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center),
                alpha = 0.40f
            )
        }

        // Loading content on top
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF00BFFF),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "GENERATING SOLVABLE BOARD",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "NIKAKUDORI MAHJONG",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}