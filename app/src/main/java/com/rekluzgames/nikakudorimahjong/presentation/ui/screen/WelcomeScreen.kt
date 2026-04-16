/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.R
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ─── Petal data ───────────────────────────────────────────────────────────────
private data class Petal(
    var x: Float,          // normalised 0..1 across screen width
    var y: Float,          // normalised 0..1 across screen height
    var vx: Float,         // horizontal velocity (positive = rightward / inward)
    var vy: Float,         // vertical velocity (downward drift)
    var rotation: Float,
    var rotSpeed: Float,
    var alpha: Float,
    var fade: Float,
    var size: Float,
    var scaleX: Float,
    var wobble: Float,
    var wobbleSpeed: Float,
    var color: Color,
    var depth: Float       // 0 = far (small, faint) .. 1 = close (large, opaque)
)

private val petalColors = listOf(
    Color(0xFFFFB7C5), Color(0xFFFF8FAB), Color(0xFFFFC8D6),
    Color(0xFFFFE4EC), Color(0xFFFF9AB5), Color(0xFFFFD6E0),
    Color(0xFFFFCCD8)
)

// The window occupies roughly the left 55% of the image horizontally,
// and vertically sits between ~15% and ~75% of the screen height.
// Petals spawn along the right/inner edge of that window opening so they
// appear to be crossing the threshold from outside into the room.
private const val WINDOW_RIGHT  = 0.42f   // x: right edge of window opening
private const val WINDOW_TOP    = 0.32f   // y: top of window opening
private const val WINDOW_BOTTOM = 0.72f   // y: bottom of window opening

private fun randomWindowPetal(): Petal {
    val depth = Random.nextFloat()                        // parallax depth
    val sizeBase = Random.nextFloat() * 6f + 4f

    return Petal(
        // Spawn along the inner edge of the window, spread across its height
        x           = WINDOW_RIGHT - Random.nextFloat() * 0.04f,
        y           = WINDOW_TOP + Random.nextFloat() * (WINDOW_BOTTOM - WINDOW_TOP),

        // Petals spread in both directions once through the window.
        // Bias slightly rightward (0.6 chance) since that's the open room,
        // but some petals curl back left as air currents would in a real room.
        vx          = if (Random.nextFloat() < 0.6f) {
            // Drifting right into the room
            (0.0006f + depth * 0.0010f) + Random.nextFloat() * 0.0005f
        } else {
            // Curling back left along the wall
            -((0.0003f + depth * 0.0006f) + Random.nextFloat() * 0.0003f)
        },

        // Gentle downward drift — petals in a room settle slowly
        vy          = Random.nextFloat() * 0.0012f + 0.0004f,

        rotation    = Random.nextFloat() * 360f,
        rotSpeed    = (Random.nextFloat() * 3f) - 1.5f,

        // Depth-based opacity: far petals are more transparent
        alpha       = 0.3f + depth * 0.65f,
        fade        = Random.nextFloat() * 0.0008f + 0.0004f,

        // Depth-based size: far petals are smaller
        size        = sizeBase * (0.5f + depth * 0.7f),
        scaleX      = Random.nextFloat() * 0.6f + 0.4f,

        wobble      = Random.nextFloat() * (2f * PI.toFloat()),
        // Closer petals wobble more (air currents stronger near viewer)
        wobbleSpeed = Random.nextFloat() * 0.025f + 0.008f + depth * 0.01f,

        color       = petalColors.random(),
        depth       = depth
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(onStartGame: () -> Unit) {

    var imageVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var tapVisible   by remember { mutableStateOf(false) }
    var petals       by remember { mutableStateOf<List<Petal>>(emptyList()) }

    // Image fade
    val imageAlpha by animateFloatAsState(
        targetValue   = if (imageVisible) 1f else 0f,
        animationSpec = tween(1800),
        label         = "imageAlpha"
    )

    // Title slide + fade
    val titleAlpha by animateFloatAsState(
        targetValue   = if (titleVisible) 1f else 0f,
        animationSpec = tween(1400),
        label         = "titleAlpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue   = if (titleVisible) 0f else -28f,
        animationSpec = tween(1400, easing = EaseOutCubic),
        label         = "titleY"
    )

    var tapBlink by remember { mutableStateOf(true) }

    LaunchedEffect(tapVisible) {
        if (!tapVisible) return@LaunchedEffect
        while (true) {
            tapBlink = true
            delay(750)
            tapBlink = false
            delay(750)
        }
    }

    // ── Sequence ────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        imageVisible = true
        delay(500)
        titleVisible = true
        delay(2200)
        tapVisible = true
    }

    // ── Petal spawner ───────────────────────────────────────────────────────
    // Spawn from the window edge. Cap at 80 petals — a room interior would
    // have fewer petals than an outdoor scene.
    LaunchedEffect(imageVisible) {
        if (!imageVisible) return@LaunchedEffect
        while (true) {
            delay(120)
            if (petals.size < 80) {
                petals = petals + randomWindowPetal()
            }
        }
    }

    // ── Petal physics ───────────────────────────────────────────────────────
    // Petals drift rightward and downward with a gentle wobble.
    // They fade out when they leave the visible area.
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            petals = petals
                .map { p ->
                    // Wobble modulates vertical position (gentle sine sway)
                    val newWobble = p.wobble + p.wobbleSpeed
                    p.copy(
                        wobble   = newWobble,
                        x        = p.x + p.vx,
                        // Wobble on Y gives a floating, tumbling feel
                        y        = p.y + p.vy + sin(newWobble) * 0.0008f,
                        rotation = p.rotation + p.rotSpeed,
                        alpha    = (p.alpha - p.fade).coerceAtLeast(0f)
                    )
                }
                // Remove petals that have drifted off-screen or fully faded.
                // Allow x up to 1.1 so they can drift fully across the room.
                .filter { it.alpha > 0.01f && it.y < 1.05f && it.x < 1.1f && it.x > -0.1f }
        }
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { if (tapVisible) onStartGame() },
        contentAlignment = Alignment.Center
    ) {

        // Background image
        Image(
            painter            = painterResource(id = R.drawable.welcome_day),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = imageAlpha }
        )

        // Gradient overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f   to Color(0xB80A0300),
                            0.5f to Color.Transparent
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.62f to Color.Transparent,
                            1f    to Color(0xC50A0300)
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            0f to Color.Transparent,
                            1f to Color(0x660A0300),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.85f
                        )
                    )
                }
        )

        // Falling petals — drawn depth-sorted (far first) so closer petals
        // render on top, reinforcing the parallax illusion.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    // Sort ascending by depth so deepest (far) draw first
                    val sorted = petals.sortedBy { it.depth }
                    for (p in sorted) {
                        val cx = p.x * size.width
                        val cy = p.y * size.height
                        val w  = p.size * 2f * p.scaleX
                        val h  = p.size

                        // Rotate each petal around its own centre
                        val rad     = Math.toRadians(p.rotation.toDouble()).toFloat()
                        val halfW   = w / 2f
                        val halfH   = h / 2f

                        // Draw as a rotated oval using save/restore on the canvas
                        with(drawContext.canvas.nativeCanvas) {
                            save()
                            translate(cx, cy)
                            rotate(Math.toDegrees(rad.toDouble()).toFloat())
                            drawOval(
                                android.graphics.RectF(-halfW, -halfH, halfW, halfH),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(
                                        (p.alpha * 255).toInt(),
                                        (p.color.red * 255).toInt(),
                                        (p.color.green * 255).toInt(),
                                        (p.color.blue * 255).toInt()
                                    )
                                    isAntiAlias = true
                                }
                            )
                            restore()
                        }
                    }
                }
        )

        // Title block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    alpha        = titleAlpha
                    translationY = titleOffsetY
                }
                .padding(horizontal = 24.dp)
        ) {
            val customStylizedFont = FontFamily(Font(R.font.zen_antique_soft))

            val mainTitleStyle = TextStyle(
                fontFamily    = customStylizedFont,
                fontSize      = 52.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color(0xFFE8C87A),
                letterSpacing = 3.sp,
                textAlign     = TextAlign.Center,
                shadow        = Shadow(
                    color      = Color.Black.copy(alpha = 0.6f),
                    offset     = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )

            val outlineTextStyle = TextStyle(
                fontFamily    = customStylizedFont,
                fontSize      = 52.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.Black,
                letterSpacing = 3.sp,
                textAlign     = TextAlign.Center
            )

            val outlineThickness = 2.dp
            val shadow3DOffset   = 4.dp

            // ── NIKAKUDORI ──
            TextWithThickOutline(
                text             = stringResource(id = R.string.title_nikakudori),
                style            = mainTitleStyle,
                outlineStyle     = outlineTextStyle,
                outlineThickness = outlineThickness,
                shadow3DOffset   = shadow3DOffset
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── MAHJONG ──
            TextWithThickOutline(
                text             = stringResource(id = R.string.title_mahjong),
                style            = mainTitleStyle,
                outlineStyle     = outlineTextStyle,
                outlineThickness = outlineThickness,
                shadow3DOffset   = shadow3DOffset
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Japanese text — larger and outlined for tablet readability
            Box(contentAlignment = Alignment.Center) {
                // Black outline passes
                for ((ox, oy) in listOf(
                    2f to 0f, -2f to 0f, 0f to 2f, 0f to -2f,
                    2f to 2f, -2f to 2f, 2f to -2f, -2f to -2f
                )) {
                    Text(
                        text          = "二 角 取 り ・ 麻 雀",
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color.Black,
                        letterSpacing = 3.sp,
                        textAlign     = TextAlign.Center,
                        modifier      = Modifier.offset(ox.dp, oy.dp)
                    )
                }
                // Gold text on top
                Text(
                    text          = "二 角 取 り ・ 麻 雀",
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color(0xFFD4A84A),
                    letterSpacing = 3.sp,
                    textAlign     = TextAlign.Center,
                    style         = TextStyle(
                        shadow = Shadow(
                            color      = Color.Black.copy(alpha = 0.9f),
                            offset     = Offset(1f, 1f),
                            blurRadius = 4f
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (tapVisible) {
                Text(
                    text          = stringResource(id = R.string.tap_to_play),
                    fontSize      = 17.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = if (tapBlink) Color(0xFFE8C87A) else Color(0xFFE8C87A).copy(alpha = 0.6f),
                    letterSpacing = 4.sp,
                    textAlign     = TextAlign.Center,
                    style         = TextStyle(
                        shadow = Shadow(
                            color      = Color.Black.copy(alpha = if (tapBlink) 1f else 0.6f),
                            offset     = Offset(1f, 1f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }

    }
}

@Composable
private fun TextWithThickOutline(
    text: String,
    style: TextStyle,
    outlineStyle: TextStyle,
    outlineThickness: Dp,
    shadow3DOffset: Dp,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        // 1. Stack outlines for thickness
        Text(text = text, style = outlineStyle, modifier = Modifier.offset(outlineThickness, outlineThickness))
        Text(text = text, style = outlineStyle, modifier = Modifier.offset(-outlineThickness, outlineThickness))
        Text(text = text, style = outlineStyle, modifier = Modifier.offset(outlineThickness, -outlineThickness))
        Text(text = text, style = outlineStyle, modifier = Modifier.offset(-outlineThickness, -outlineThickness))

        // 2. 3D shadow layer
        Text(
            text = text,
            style = outlineStyle.copy(
                shadow = Shadow(
                    color      = Color.Black.copy(alpha = 0.5f),
                    offset     = Offset(shadow3DOffset.value * 1.5f, shadow3DOffset.value * 1.5f),
                    blurRadius = shadow3DOffset.value * 2f
                )
            ),
            modifier = Modifier.offset(shadow3DOffset, shadow3DOffset)
        )

        // 3. Top layer (Gold)
        Text(text = text, style = style)
    }
}