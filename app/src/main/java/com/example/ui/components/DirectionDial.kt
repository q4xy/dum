package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DirectionDial(
    label: String,
    currentDegrees: Float,
    accentColor: Color,
    onDegreesChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String = ""
) {
    val cardinalName = getCardinalLabel(currentDegrees)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(OceanCard)
            .border(1.dp, OceanCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (subLabel.isNotEmpty()) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                color = OceanAbyss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder)
            ) {
                Text(
                    text = "${String.format("%03.0f°", currentDegrees)} $cardinalName",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Compass Disc
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(OceanAbyss)
                .border(2.dp, OceanCardBorder, CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = calculateAngle(offset, center)
                        onDegreesChanged(angle)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = calculateAngle(change.position, center)
                        onDegreesChanged(angle)
                    }
                }
                .testTag("direction_dial_${label.lowercase().replace(" ", "_")}"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Dial Background Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F253C), OceanAbyss),
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )

                // Tick marks
                for (deg in 0 until 360 step 30) {
                    val rad = Math.toRadians(deg.toDouble())
                    val isCardinal = deg % 90 == 0
                    val tickLen = if (isCardinal) 10.dp.toPx() else 6.dp.toPx()

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
                }

                // Center pivot ring
                drawCircle(color = OceanCardBorder, radius = 18.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
                drawCircle(color = OceanAbyss, radius = 16.dp.toPx(), center = center)

                // Needle Arrow pointing in currentDegrees
                drawCompassNeedle(
                    center = center,
                    length = radius * 0.75f,
                    headingDeg = currentDegrees,
                    accentColor = accentColor
                )

                // Center cap
                drawCircle(color = accentColor, radius = 4.dp.toPx(), center = center)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Cardinal Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f).forEach { (name, deg) ->
                AssistChip(
                    onClick = { onDegreesChanged(deg) },
                    label = { Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (kotlin.math.abs(currentDegrees - deg) < 5f) accentColor.copy(alpha = 0.25f) else OceanAbyss,
                        labelColor = if (kotlin.math.abs(currentDegrees - deg) < 5f) accentColor else TextPrimary
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = if (kotlin.math.abs(currentDegrees - deg) < 5f) accentColor else OceanCardBorder
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawCompassNeedle(
    center: Offset,
    length: Float,
    headingDeg: Float,
    accentColor: Color
) {
    val rad = Math.toRadians(headingDeg.toDouble())
    val tipX = center.x + (length * sin(rad)).toFloat()
    val tipY = center.y - (length * cos(rad)).toFloat()

    val tailRad = rad + PI
    val tailLen = length * 0.35f
    val tailX = center.x + (tailLen * sin(tailRad)).toFloat()
    val tailY = center.y - (tailLen * cos(tailRad)).toFloat()

    val wingLen = 10.dp.toPx()
    val wingRad1 = rad + (PI * 0.5)
    val wingRad2 = rad - (PI * 0.5)

    val wing1X = center.x + (wingLen * sin(wingRad1)).toFloat()
    val wing1Y = center.y - (wingLen * cos(wingRad1)).toFloat()
    val wing2X = center.x + (wingLen * sin(wingRad2)).toFloat()
    val wing2Y = center.y - (wingLen * cos(wingRad2)).toFloat()

    // Forward pointer triangle
    val headPath = Path().apply {
        moveTo(tipX, tipY)
        lineTo(wing1X, wing1Y)
        lineTo(center.x, center.y)
        close()
    }
    drawPath(headPath, color = accentColor, style = Fill)

    val headPath2 = Path().apply {
        moveTo(tipX, tipY)
        lineTo(wing2X, wing2Y)
        lineTo(center.x, center.y)
        close()
    }
    drawPath(headPath2, color = accentColor.copy(alpha = 0.75f), style = Fill)

    // Tail counter-weight
    val tailPath = Path().apply {
        moveTo(tailX, tailY)
        lineTo(wing1X, wing1Y)
        lineTo(center.x, center.y)
        close()
    }
    drawPath(tailPath, color = Color(0xFF384B5D), style = Fill)

    val tailPath2 = Path().apply {
        moveTo(tailX, tailY)
        lineTo(wing2X, wing2Y)
        lineTo(center.x, center.y)
        close()
    }
    drawPath(tailPath2, color = Color(0xFF283645), style = Fill)
}

private fun calculateAngle(offset: Offset, center: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    // atan2(dx, -dy) maps North (0, -1) to 0, East (1, 0) to 90
    var deg = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
    if (deg < 0f) deg += 360f
    return deg
}

private fun getCardinalLabel(deg: Float): String {
    val normalized = (deg % 360f + 360f) % 360f
    return when {
        normalized >= 337.5f || normalized < 22.5f -> "N"
        normalized < 67.5f -> "NE"
        normalized < 112.5f -> "E"
        normalized < 157.5f -> "SE"
        normalized < 202.5f -> "S"
        normalized < 247.5f -> "SW"
        normalized < 292.5f -> "W"
        else -> "NW"
    }
}
