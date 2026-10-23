package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DriftCalculationResult
import com.example.model.EnvironmentalInputs
import com.example.model.UnitSystem
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MaritimeSitrepDialog(
    inputs: EnvironmentalInputs,
    result: DriftCalculationResult,
    selectedHour: Float,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val sitrepText = generateSitrepString(inputs, result, selectedHour, unitSystem)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = OceanCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("maritime_sitrep_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "INCIDENT COMMAND SITREP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Standard Maritime Spill Trajectory Briefing",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanNeon,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SITREP Monospace Box
                Surface(
                    color = OceanAbyss,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sitrepText,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFC0D5E5),
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Maritime Spill SITREP", sitrepText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "SITREP copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = OceanAbyss),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_sitrep_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy SITREP", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

private fun generateSitrepString(
    inputs: EnvironmentalInputs,
    result: DriftCalculationResult,
    selectedHour: Float,
    unitSystem: UnitSystem
): String {
    val currentPt = result.trajectoryPoints.minByOrNull { kotlin.math.abs(it.hour - selectedHour) }
        ?: result.trajectoryPoints.last()

    val currentCoords = inputs.calculateCoordinates(currentPt.displacementXNM, currentPt.displacementYNM)
    val headCoordStr = EnvironmentalInputs.formatNautical(currentCoords.first, currentCoords.second)
    val originCoordStr = EnvironmentalInputs.formatNautical(inputs.originLat, inputs.originLon)

    return buildString {
        appendLine("==================================================")
        appendLine("  MARITIME CASUALTY OIL SPILL SITUATION REPORT   ")
        appendLine("  INCIDENT ID: ${inputs.incidentCallsign}            ")
        appendLine("==================================================")
        appendLine("1. GENERAL INCIDENT TELEMETRY:")
        appendLine("   - Origin / Release Point : $originCoordStr")
        appendLine("   - Hydrocarbon Type       : ${inputs.oilType.displayName}")
        appendLine("   - Initial Volume Released: ${unitSystem.formatVolume(inputs.spillVolumeM3)}")
        appendLine("   - Sea Water Temperature  : ${inputs.waterTemperatureCelsius}°C")
        appendLine("   - Response Level         : ${result.responseTierRecommendation}")
        appendLine()
        appendLine("2. METOCEAN FORCES:")
        appendLine("   - Surface Current        : ${unitSystem.formatSpeed(inputs.currentSpeedKnots)} @ ${String.format("%03.0f°", inputs.currentDirectionDeg)} (Heading towards)")
        appendLine("   - 10m Atmospheric Wind   : ${unitSystem.formatSpeed(inputs.windSpeedKnots)} @ ${String.format("%03.0f°", inputs.windDirectionDeg)} (${inputs.windConvention.label})")
        appendLine("   - Wind Leeway Factor     : ${String.format("%.1f%%", inputs.windLeewayFactor * 100f)} (Coriolis: ${String.format("%+.1f°", inputs.coriolisDeflectionDeg)})")
        appendLine("   - Sea State              : Beaufort Force ${result.beaufortScale} (${result.seaStateDescription})")
        appendLine()
        appendLine("3. TRAJECTORY & DISPLACEMENT (T+${String.format("%.1f", selectedHour)}h):")
        appendLine("   - Current Slick Head Pos : $headCoordStr")
        appendLine("   - Resultant Drift Vector : ${unitSystem.formatSpeed(result.driftSpeedKnots)} @ ${String.format("%03.1f°", result.driftHeadingDeg)} True")
        appendLine("   - Net Displacement       : ${unitSystem.formatDistance(currentPt.distanceNM)}")
        if (result.estimatedTimeToImpactHours != null) {
            appendLine("   - Shoreline Threat ETA   : ~${String.format("%.1f", result.estimatedTimeToImpactHours)} hours (at 28 NM baseline)")
        }
        appendLine()
        appendLine("4. WEATHERING & SLICK MORPHOLOGY:")
        appendLine("   - Active Slick Surface   : ${unitSystem.formatArea(currentPt.slickAreaKm2)} (Major Axis: ${unitSystem.formatDistance(currentPt.slickMajorAxisNM)})")
        appendLine("   - Cumulative Weathering  : ${String.format("%.1f%%", currentPt.totalWeatheredPercent)} Total Loss")
        appendLine("     * Evaporation Loss     : ${String.format("%.1f%%", currentPt.evaporatedPercent)}")
        appendLine("     * Wave Dispersion Loss : ${String.format("%.1f%%", currentPt.dispersedPercent)}")
        appendLine("   - Active Slick Volume    : ${unitSystem.formatVolume(result.remainingVolumeM3)}")
        appendLine("   - Water Emulsification   : ${String.format("%.0f%%", result.emulsifiedPercent)} water-in-oil")
        appendLine()
        appendLine("5. TACTICAL CONTAINMENT DIRECTIVES:")
        appendLine("   - Required Boom Length   : ${String.format("%,.0f", result.containmentBoomMeters)} meters (${String.format("%,.0f", result.containmentBoomMeters * 3.28084f)} ft)")
        appendLine("   - Skimming Feasibility   : ${if (inputs.windSpeedKnots <= 20f && inputs.currentSpeedKnots <= 2.5f) "FEASIBLE" else "MARGINAL / RETRACTED"}")
        appendLine("==================================================")
        appendLine("  TRANSMITTED VIA SLICKDRIFT MARITIME TELEMETRY   ")
        appendLine("==================================================")
    }
}
