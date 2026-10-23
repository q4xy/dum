package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriftCalculationResult
import com.example.model.UnitSystem
import com.example.model.Vector2D
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanNeonGlow
import com.example.ui.theme.DistressRed
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.PetroleumDark
import com.example.ui.theme.PetroleumOrange
import com.example.ui.theme.PetroleumSheen
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun OceanRadarCanvas(
    result: DriftCalculationResult,
    selectedHour: Float,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
    onHourChanged: ((Float) -> Unit)? = null
) {
    // Zoom and pan state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Radar phosphor sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Wave swell oscillation animation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Pulsing core effect
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Dynamic range determination: fit the max displacement comfortably inside radar
    val maxDispNM = max(5.0f, result.totalDisplacementNM * 1.35f)
    val ringStepNM = when {
        maxDispNM <= 6f -> 1f
        maxDispNM <= 15f -> 2.5f
        maxDispNM <= 35f -> 5f
        maxDispNM <= 70f -> 10f
        else -> 20f
    }
    val outerRangeNM = kotlin.math.ceil(maxDispNM / ringStepNM) * ringStepNM

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(OceanAbyss)
            .border(1.dp, OceanCardBorder, RoundedCornerShape(20.dp))
            .testTag("ocean_radar_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 4.0f)
                        panOffset += pan
                    }
                }
                .testTag("ocean_radar_canvas")
        ) {
            val center = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)
            val radarRadius = (min(size.width, size.height) / 2f) * 0.88f * zoomScale
            val pixelsPerNM = radarRadius / outerRangeNM

            // 1. Draw radar background gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0B1E32), OceanAbyss),
                    center = center,
                    radius = radarRadius * 1.15f
                ),
                radius = radarRadius,
                center = center
            )

            // 2. Draw ambient animated wave swells
            drawWaveSwells(
                center = center,
                radius = radarRadius,
                wavePhase = wavePhase,
                currentHeading = result.currentVector.headingDegrees
            )

            // 3. Draw concentric nautical range rings with distance labels
            val ringTagPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#48CAE4")
                textSize = 9.sp.toPx()
                textAlign = android.graphics.Paint.Align.LEFT
                isAntiAlias = true
            }

            val ringCount = (outerRangeNM / ringStepNM).toInt()
            for (i in 1..ringCount) {
                val ringDistNM = i * ringStepNM
                val ringPx = ringDistNM * pixelsPerNM
                if (ringPx <= radarRadius * 1.05f) {
                    drawCircle(
                        color = Color(0xFF1B3B54),
                        radius = ringPx,
                        center = center,
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )
                    // Nautical Range label directly on ring
                    drawContext.canvas.nativeCanvas.drawText(
                        unitSystem.formatDistance(ringDistNM),
                        center.x + ringPx + 3.dp.toPx(),
                        center.y - 3.dp.toPx(),
                        ringTagPaint
                    )
                }
            }

            // 4. Draw cardinal crosshairs
            drawLine(
                color = Color(0xFF1B3B54),
                start = Offset(center.x - radarRadius, center.y),
                end = Offset(center.x + radarRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1B3B54),
                start = Offset(center.x, center.y - radarRadius),
                end = Offset(center.x, center.y + radarRadius),
                strokeWidth = 1.dp.toPx()
            )

            // 5. Draw rotating radar sweep phosphor
            rotate(sweepAngle, pivot = center) {
                val sweepBrush = Brush.sweepGradient(
                    0.0f to Color.Transparent,
                    0.85f to Color.Transparent,
                    0.99f to Color(0x3300E5FF),
                    1.0f to CyanNeon,
                    center = center
                )
                drawCircle(
                    brush = sweepBrush,
                    radius = radarRadius,
                    center = center
                )
            }

            // 6. Draw Trajectory Cone of Uncertainty Envelope & Probability Ellipses Layer
            drawTrajectoryConfidenceLayer(
                uncertaintyPoints = result.uncertaintyConePoints,
                driftHeadingDeg = result.driftHeadingDeg,
                center = center,
                pixelsPerNM = pixelsPerNM
            )

            // 7. Draw Full Trajectory Path (history & forecast up to simulation end)
            drawTrajectoryPath(
                points = result.trajectoryPoints,
                center = center,
                pixelsPerNM = pixelsPerNM,
                selectedHour = selectedHour
            )

            // 7. Draw Current Vector (Blue arrow from origin)
            drawNavVector(
                origin = center,
                vector = result.currentVector,
                pixelsPerUnit = pixelsPerNM * 4.0f, // Scaled for visibility
                color = HydroBlue,
                label = "CURRENT: ${unitSystem.formatSpeed(result.currentVector.magnitude)}"
            )

            // 8. Draw Wind Leeway Vector (Cyan dashed arrow)
            drawNavVector(
                origin = center,
                vector = result.windLeewayVector,
                pixelsPerUnit = pixelsPerNM * 8.0f,
                color = AeroCyan,
                label = "LEEWAY: ${unitSystem.formatSpeed(result.windLeewayVector.magnitude)}",
                isDashed = true
            )

            // 9. Draw Resultant Combined Drift Vector (Golden glowing arrow)
            drawNavVector(
                origin = center,
                vector = result.totalDriftVector,
                pixelsPerUnit = pixelsPerNM * 3.5f,
                color = PetroleumAmber,
                label = "DRIFT: ${unitSystem.formatSpeed(result.driftSpeedKnots)} @ ${String.format("%03.0f°", result.driftHeadingDeg)}",
                isBold = true
            )

            // 10. Draw Oil Slick at Selected Hour (Spreading ellipse)
            val currentDispX = result.totalDriftVector.x * selectedHour
            val currentDispY = result.totalDriftVector.y * selectedHour
            val slickCenter = Offset(
                center.x + currentDispX * pixelsPerNM,
                center.y - currentDispY * pixelsPerNM // Invert Y for North
            )

            // Look up slick dimensions for selectedHour
            val interpPt = result.trajectoryPoints.minByOrNull { kotlin.math.abs(it.hour - selectedHour) }
            val majorPx = max(12f, (interpPt?.slickMajorAxisNM ?: 0.5f) * pixelsPerNM * 1.5f)
            val minorPx = max(8f, (interpPt?.slickMinorAxisNM ?: 0.3f) * pixelsPerNM * 1.5f)
            val driftAngle = result.driftHeadingDeg

            drawOilSlick(
                center = slickCenter,
                majorPx = majorPx,
                minorPx = minorPx,
                headingDeg = driftAngle,
                pulseAlpha = pulseAlpha
            )

            // 11. Draw Spill Origin Point (0,0)
            drawSpillOrigin(center = center)

            // 12. Compass Ring Ticks and Text
            drawCompassTicks(
                center = center,
                radius = radarRadius,
                outerRangeNM = outerRangeNM,
                unitSystem = unitSystem
            )
        }

        // Radar Overlay Controls (Top left: Range, Top Right: Zoom/Reset, Bottom: Real-Time Weathering Chart & Telemetry)
        RadarHudOverlay(
            result = result,
            selectedHour = selectedHour,
            unitSystem = unitSystem,
            outerRangeNM = outerRangeNM,
            zoomScale = zoomScale,
            onZoomIn = { zoomScale = (zoomScale * 1.25f).coerceAtMost(4f) },
            onZoomOut = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.5f) },
            onReset = {
                zoomScale = 1.0f
                panOffset = Offset.Zero
            },
            onHourChanged = onHourChanged
        )
    }
}

private fun DrawScope.drawWaveSwells(
    center: Offset,
    radius: Float,
    wavePhase: Float,
    currentHeading: Float
) {
    val waveRad = Math.toRadians(currentHeading.toDouble()).toFloat()
    val numWaves = 6
    for (i in 0 until numWaves) {
        val distRatio = 0.2f + (i * 0.14f)
        val distPx = radius * distRatio
        val phaseOffset = wavePhase + i * 0.8f
        val perpX = -sin(waveRad)
        val perpY = cos(waveRad)

        val waveCenter = Offset(
            center.x + cos(waveRad) * distPx,
            center.y + sin(waveRad) * distPx
        )

        val wavePath = Path()
        val waveLen = radius * 0.25f
        for (step in -10..10) {
            val t = step / 10f
            val px = waveCenter.x + perpX * (t * waveLen) + cos(waveRad) * (sin(t * 3f + phaseOffset) * 4f)
            val py = waveCenter.y + perpY * (t * waveLen) + sin(waveRad) * (sin(t * 3f + phaseOffset) * 4f)
            if (step == -10) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
        }

        drawPath(
            path = wavePath,
            color = Color(0x1800E5FF),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawTrajectoryPath(
    points: List<com.example.model.TrajectoryPoint>,
    center: Offset,
    pixelsPerNM: Float,
    selectedHour: Float
) {
    if (points.size < 2) return

    val historyPath = Path()
    val forecastPath = Path()
    var hitSelected = false

    points.forEachIndexed { index, pt ->
        val px = center.x + pt.displacementXNM * pixelsPerNM
        val py = center.y - pt.displacementYNM * pixelsPerNM // Y is inverted

        if (index == 0) {
            historyPath.moveTo(px, py)
            forecastPath.moveTo(px, py)
        } else {
            if (pt.hour <= selectedHour) {
                historyPath.lineTo(px, py)
                forecastPath.moveTo(px, py)
            } else {
                if (!hitSelected) {
                    hitSelected = true
                    forecastPath.moveTo(
                        center.x + points.last { it.hour <= selectedHour }.displacementXNM * pixelsPerNM,
                        center.y - points.last { it.hour <= selectedHour }.displacementYNM * pixelsPerNM
                    )
                }
                forecastPath.lineTo(px, py)
            }
        }

        // Draw hour milestone breadcrumbs (e.g., 6h, 12h, 24h, 48h, 72h)
        if (pt.hour > 0f && (pt.hour.toInt() % 6 == 0 || pt.hour == points.last().hour)) {
            val isPassed = pt.hour <= selectedHour
            drawCircle(
                color = if (isPassed) PetroleumAmber else Color(0xFF6B7280),
                radius = 3.5.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }

    // Traveled path (solid amber glow)
    drawPath(
        path = historyPath,
        color = PetroleumAmber,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Forecasted future path (dashed faint line)
    drawPath(
        path = forecastPath,
        color = Color(0x77FF9E00),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    )
}

private fun DrawScope.drawTrajectoryConfidenceLayer(
    uncertaintyPoints: List<com.example.model.UncertaintyConePoint>,
    driftHeadingDeg: Float,
    center: Offset,
    pixelsPerNM: Float
) {
    if (uncertaintyPoints.isEmpty()) return

    val upperPoints = mutableListOf<Offset>()
    val lowerPoints = mutableListOf<Offset>()

    uncertaintyPoints.forEach { pt ->
        val px = center.x + pt.dispXNM * pixelsPerNM
        val py = center.y - pt.dispYNM * pixelsPerNM
        val r90 = pt.prob90RadiusNM * pixelsPerNM

        val mag = sqrt(pt.dispXNM * pt.dispXNM + pt.dispYNM * pt.dispYNM).coerceAtLeast(0.001f)
        val perpX = (-pt.dispYNM / mag) * r90
        val perpY = (pt.dispXNM / mag) * r90

        upperPoints.add(Offset(px + perpX, py - perpY))
        lowerPoints.add(Offset(px - perpX, py + perpY))
    }

    // 1. Shaded Polyline Cone Envelope
    if (upperPoints.size >= 2) {
        val conePath = Path().apply {
            moveTo(upperPoints.first().x, upperPoints.first().y)
            for (i in 1 until upperPoints.size) {
                lineTo(upperPoints[i].x, upperPoints[i].y)
            }
            for (i in lowerPoints.indices.reversed()) {
                lineTo(lowerPoints[i].x, lowerPoints[i].y)
            }
            close()
        }

        drawPath(
            path = conePath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0x3300E5FF), Color(0x1100E5FF), Color(0x0200E5FF)),
                center = center,
                radius = ((uncertaintyPoints.lastOrNull()?.prob90RadiusNM ?: 5f) * pixelsPerNM * 1.5f).coerceAtLeast(10f)
            )
        )

        val upperBoundaryPath = Path().apply {
            moveTo(upperPoints.first().x, upperPoints.first().y)
            for (i in 1 until upperPoints.size) lineTo(upperPoints[i].x, upperPoints[i].y)
        }
        val lowerBoundaryPath = Path().apply {
            moveTo(lowerPoints.first().x, lowerPoints.first().y)
            for (i in 1 until lowerPoints.size) lineTo(lowerPoints[i].x, lowerPoints[i].y)
        }

        drawPath(
            path = upperBoundaryPath,
            color = Color(0x7700E5FF),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
        drawPath(
            path = lowerBoundaryPath,
            color = Color(0x7700E5FF),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
    }

    // 2. Rotated Probability Ellipses (50%, 75%, 90% Confidence Contours)
    uncertaintyPoints.filter { it.hour > 0f && (it.hour.toInt() % 12 == 0 || it == uncertaintyPoints.last()) }.forEach { pt ->
        val px = center.x + pt.dispXNM * pixelsPerNM
        val py = center.y - pt.dispYNM * pixelsPerNM
        val pivot = Offset(px, py)

        val rx90 = pt.alongTrackErrorNM * 2.146f * pixelsPerNM
        val ry90 = pt.crossTrackErrorNM * 2.146f * pixelsPerNM

        val rx75 = pt.alongTrackErrorNM * 1.665f * pixelsPerNM
        val ry75 = pt.crossTrackErrorNM * 1.665f * pixelsPerNM

        val rx50 = pt.alongTrackErrorNM * 1.177f * pixelsPerNM
        val ry50 = pt.crossTrackErrorNM * 1.177f * pixelsPerNM

        rotate(driftHeadingDeg, pivot = pivot) {
            // 90% Outer Envelope (Cyan)
            drawOval(
                color = Color(0x2200E5FF),
                topLeft = Offset(px - rx90, py - ry90),
                size = Size(rx90 * 2f, ry90 * 2f)
            )
            drawOval(
                color = Color(0x8800E5FF),
                topLeft = Offset(px - rx90, py - ry90),
                size = Size(rx90 * 2f, ry90 * 2f),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
            )

            // 75% Mid Contour (Gold)
            drawOval(
                color = Color(0x22FFD166),
                topLeft = Offset(px - rx75, py - ry75),
                size = Size(rx75 * 2f, ry75 * 2f)
            )
            drawOval(
                color = Color(0xAAFFD166),
                topLeft = Offset(px - rx75, py - ry75),
                size = Size(rx75 * 2f, ry75 * 2f),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
            )

            // 50% Core Contour (Amber)
            drawOval(
                color = Color(0x33FFB703),
                topLeft = Offset(px - rx50, py - ry50),
                size = Size(rx50 * 2f, ry50 * 2f)
            )
            drawOval(
                color = PetroleumAmber,
                topLeft = Offset(px - rx50, py - ry50),
                size = Size(rx50 * 2f, ry50 * 2f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawNavVector(
    origin: Offset,
    vector: Vector2D,
    pixelsPerUnit: Float,
    color: Color,
    label: String,
    isDashed: Boolean = false,
    isBold: Boolean = false
) {
    if (vector.magnitude < 0.05f) return

    // Heading in nautical degrees: 0° is up (North, -Y), 90° is right (+X)
    val rad = Math.toRadians(vector.headingDegrees.toDouble())
    val lengthPx = min(180f, max(35f, vector.magnitude * pixelsPerUnit))
    val endX = origin.x + (lengthPx * sin(rad)).toFloat()
    val endY = origin.y - (lengthPx * cos(rad)).toFloat()

    val strokeWidth = if (isBold) 3.5.dp.toPx() else 2.dp.toPx()
    val effect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f) else null

    // Draw main vector line
    drawLine(
        color = color,
        start = origin,
        end = Offset(endX, endY),
        strokeWidth = strokeWidth,
        pathEffect = effect,
        cap = StrokeCap.Round
    )

    // Draw arrowhead
    val arrowSize = if (isBold) 14f else 10f
    val arrowAngle = Math.toRadians(25.0)
    val backAngle1 = rad + PI - arrowAngle
    val backAngle2 = rad + PI + arrowAngle

    val arrowP1 = Offset(
        endX + (arrowSize * sin(backAngle1)).toFloat(),
        endY - (arrowSize * cos(backAngle1)).toFloat()
    )
    val arrowP2 = Offset(
        endX + (arrowSize * sin(backAngle2)).toFloat(),
        endY - (arrowSize * cos(backAngle2)).toFloat()
    )

    val arrowHead = Path().apply {
        moveTo(endX, endY)
        lineTo(arrowP1.x, arrowP1.y)
        lineTo(arrowP2.x, arrowP2.y)
        close()
    }
    drawPath(arrowHead, color = color, style = Fill)
}

private fun DrawScope.drawOilSlick(
    center: Offset,
    majorPx: Float,
    minorPx: Float,
    headingDeg: Float,
    pulseAlpha: Float
) {
    rotate(headingDeg, pivot = center) {
        // Outer iridescent sheen (amber translucent halo)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    PetroleumOrange.copy(alpha = 0.50f * pulseAlpha),
                    PetroleumAmber.copy(alpha = 0.25f),
                    PetroleumSheen.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = center,
                radius = majorPx
            ),
            topLeft = Offset(center.x - majorPx, center.y - minorPx),
            size = Size(majorPx * 2f, minorPx * 2f)
        )

        // Inner thick oil core (tarry petroleum amber/dark)
        val coreMajor = majorPx * 0.45f
        val coreMinor = minorPx * 0.45f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    PetroleumDark,
                    Color(0xFF3E1B00),
                    PetroleumOrange.copy(alpha = 0.85f)
                ),
                center = center,
                radius = coreMajor
            ),
            topLeft = Offset(center.x - coreMajor, center.y - coreMinor),
            size = Size(coreMajor * 2f, coreMinor * 2f)
        )

        // Pulsing leading edge perimeter
        drawOval(
            color = PetroleumAmber.copy(alpha = pulseAlpha * 0.8f),
            topLeft = Offset(center.x - majorPx * 0.95f, center.y - minorPx * 0.95f),
            size = Size(majorPx * 1.9f, minorPx * 1.9f),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
        )
    }

    // Elegant glowing center focal beacon
    drawCircle(color = CyanNeonGlow, radius = 9.dp.toPx(), center = center)
    drawCircle(color = CyanNeon, radius = 3.5.dp.toPx(), center = center)
}

private fun DrawScope.drawSpillOrigin(center: Offset) {
    // Red crosshair ring for Incident Ground Zero
    drawCircle(color = DistressRed, radius = 6.dp.toPx(), center = center, style = Stroke(width = 1.5.dp.toPx()))
    drawCircle(color = DistressRed, radius = 2.5.dp.toPx(), center = center)
    drawLine(
        color = DistressRed,
        start = Offset(center.x - 10.dp.toPx(), center.y),
        end = Offset(center.x + 10.dp.toPx(), center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = DistressRed,
        start = Offset(center.x, center.y - 10.dp.toPx()),
        end = Offset(center.x, center.y + 10.dp.toPx()),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawCompassTicks(
    center: Offset,
    radius: Float,
    outerRangeNM: Float,
    unitSystem: UnitSystem
) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#8EA8BE")
        textSize = 10.sp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    val cardinalPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#00E5FF")
        textSize = 12.sp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }

    val cardinals = mapOf(
        0 to "N",
        45 to "NE",
        90 to "E",
        135 to "SE",
        180 to "S",
        225 to "SW",
        270 to "W",
        315 to "NW"
    )

    for (deg in 0 until 360 step 15) {
        val rad = Math.toRadians(deg.toDouble())
        val isMajor = deg % 45 == 0
        val isCardinal = deg % 90 == 0
        val tickLen = if (isCardinal) 12.dp.toPx() else if (isMajor) 8.dp.toPx() else 4.dp.toPx()

        val startX = center.x + ((radius - tickLen) * sin(rad)).toFloat()
        val startY = center.y - ((radius - tickLen) * cos(rad)).toFloat()
        val endX = center.x + (radius * sin(rad)).toFloat()
        val endY = center.y - (radius * cos(rad)).toFloat()

        drawLine(
            color = if (isCardinal) CyanNeon else Color(0xFF23415C),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
        )

        // Draw cardinal labels
        if (isMajor) {
            val textRadius = radius - 20.dp.toPx()
            val textX = center.x + (textRadius * sin(rad)).toFloat()
            val textY = center.y - (textRadius * cos(rad)).toFloat() + 4.dp.toPx()
            val label = cardinals[deg] ?: String.format("%03d°", deg)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                textX,
                textY,
                if (isCardinal) cardinalPaint else paint
            )
        }
    }
}

@Composable
private fun RadarHudOverlay(
    result: DriftCalculationResult,
    selectedHour: Float,
    unitSystem: UnitSystem,
    outerRangeNM: Float,
    zoomScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    onHourChanged: ((Float) -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        // Top Left: Range & Status
        Surface(
            color = OceanCard.copy(alpha = 0.85f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(RadarGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RADAR RANGE: ${unitSystem.formatDistance(outerRangeNM / zoomScale)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "SWEEP: ACTIVE • T+${String.format("%.1f", selectedHour)}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanNeon
                )
                Text(
                    text = "CONFIDENCE: ${result.outcomeConfidenceScore}% (${if (result.outcomeConfidenceScore >= 85) "HIGH" else "MODERATE"})",
                    style = MaterialTheme.typography.labelSmall,
                    color = PetroleumAmber,
                    fontSize = 9.sp
                )
            }
        }

        // Top Right: Zoom controls
        Surface(
            color = OceanCard.copy(alpha = 0.85f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Row(modifier = Modifier.padding(2.dp)) {
                IconButton(onClick = onZoomIn, modifier = Modifier.size(36.dp).testTag("zoom_in_button")) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextPrimary)
                }
                IconButton(onClick = onZoomOut, modifier = Modifier.size(36.dp).testTag("zoom_out_button")) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextPrimary)
                }
                IconButton(onClick = onReset, modifier = Modifier.size(36.dp).testTag("radar_recenter_button")) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = CyanNeon)
                }
            }
        }

        // Bottom Section: Elegant Telemetry & Vector Legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bottom Left: Current Telemetry Pill
            Surface(
                color = OceanCard.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder)
            ) {
                val dispAtHour = result.totalDriftVector.magnitude * selectedHour
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DISPLACEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = unitSystem.formatDistance(dispAtHour),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = PetroleumAmber,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "BEARING",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = String.format("%03.1f°", result.driftHeadingDeg),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = CyanNeon,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Bottom Right: Vector Legend Pill
            Surface(
                color = OceanCard.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp, 3.dp).background(HydroBlue, RoundedCornerShape(1.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current", style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp, 3.dp).background(AeroCyan, RoundedCornerShape(1.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wind", style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp, 3.dp).background(PetroleumAmber, RoundedCornerShape(1.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Drift", style = MaterialTheme.typography.labelSmall, color = PetroleumAmber, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
