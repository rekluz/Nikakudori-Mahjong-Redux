/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.domain.model.GameState
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModel
import com.rekluzgames.nikakudorimahjong.presentation.ui.component.AlphabetTile
import com.rekluzgames.nikakudorimahjong.presentation.ui.component.MenuPillButton
import com.rekluzgames.nikakudorimahjong.presentation.ui.component.OverlayContainer

@Composable
fun AboutScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    OverlayContainer {
        Column(
            modifier = Modifier
                .width(620.dp)
                .background(Color(0xFF0D1A3A), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nikakudori Mahjong",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "v${uiState.version}",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            when (uiState.aboutStage) {
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f).padding(end = 16.dp)) {
                            Text(
                                text = "Nikakudori Mahjong is a traditional Japanese tile-matching puzzle game. Connect identical pairs using a path with no more than two 90-degree turns to clear the board.",
                                color = Color.White, fontSize = 16.sp, lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                text = "View project on GitHub",
                                color = Color(0xFF00BFFF),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://github.com/rekluz/Nikakudori-Mahjong-Redux/")
                                }
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row {
                                "REKLUZ".forEachIndexed { i, c ->
                                    AlphabetTile(c, !uiState.clearedAboutTiles.contains(i)) {
                                        viewModel.onAboutTileClick(i, 11)
                                    }
                                }
                            }
                            Row {
                                "GAMES".forEachIndexed { i, c ->
                                    AlphabetTile(c, !uiState.clearedAboutTiles.contains(i + 6)) {
                                        viewModel.onAboutTileClick(i + 6, 11)
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Box(modifier = Modifier.width(180.dp)) {
                                MenuPillButton(text = "DONE", color = Color(0xFF00BFFF)) {
                                    viewModel.changeState(GameState.PLAYING)
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Hello World!",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                                .border(3.dp, Color(0xFF00BFFF), CircleShape)
                                .padding(4.dp)
                                .clickable {
                                    viewModel.resetAbout()
                                    viewModel.changeState(GameState.PLAYING)
                                }
                        ) {
                            val id = context.resources.getIdentifier(
                                "my_photo", "drawable", context.packageName
                            )
                            if (id != 0) {
                                Image(
                                    painter = painterResource(id),
                                    contentDescription = "Developer Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            }
                        }

                        Text(
                            text = "Created by",
                            color = Color.Gray,
                            fontSize = 14.sp,

                        )
                        Text(
                            text = "Rico Luzi",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(Modifier.width(260.dp)) {
                            MenuPillButton("THANK YOU FOR PLAYING") {
                                viewModel.resetAbout()
                                viewModel.changeState(GameState.PLAYING)
                            }
                        }
                    }
                }
            }
        }
    }
}