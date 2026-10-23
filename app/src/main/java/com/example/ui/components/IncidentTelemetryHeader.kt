package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriftCalculationResult
import com.example.model.EnvironmentalInputs
import com.example.model.UnitSystem
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun IncidentTelemetryHeader(
    inputs: EnvironmentalInputs,
    result: DriftCalculationResult,
    selectedHour: Float,
    unitSystem: UnitSystem,
    onOpenSitrep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPt = result.trajectoryPoints.minByOrNull { kotlin.math.abs(it.hour - selectedHour) }
        ?: result.trajectoryPoints.last()

    val currentCoords = inputs.calculateCoordinates(currentPt.displacementXNM, currentPt.displacementYNM)
    val headCoordStr = EnvironmentalInputs.formatNautical(currentCoords.first, currentCoords.second)
    val originCoordStr = EnvironmentalInputs.formatNautical(inputs.originLat, inputs.originLon)

    Surface(
        color = OceanCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("incident_telemetry_header")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Status Badges & SITREP Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(RadarGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INCIDENT COMMAND • ACTIVE DRIFT OPS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Surface(
                    color = OceanAbyss,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenSitrep() }
                        .testTag("open_sitrep_dialog_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = "SITREP",
                            tint = CyanNeon,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OFFICIAL SITREP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nautical Coordinates Readout
            Surface(
                color = OceanAbyss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, OceanCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Origin Ground Zero
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GROUND ZERO (RELEASE)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = originCoordStr,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(OceanCardBorder))

                    // Drifting Slick Head Position
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PROJECTED SLICK HEAD (T+${String.format("%.0f", selectedHour)}h)",
                            style = MaterialTheme.typography.labelSmall,
                            color = PetroleumAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = headCoordStr,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = PetroleumAmber,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Shoreline Collision Threat Advisory
            if (result.estimatedTimeToImpactHours != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Threat",
                        tint = PetroleumAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Coastal Threat Advisory: Distance ~${unitSystem.formatDistance(result.projectedShorelineDistanceNM)} • Shoreline ETA ~${String.format("%.1f", result.estimatedTimeToImpactHours)} hrs at current velocity",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
