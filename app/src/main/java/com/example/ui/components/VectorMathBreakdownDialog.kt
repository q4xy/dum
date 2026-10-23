package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DriftCalculationResult
import com.example.model.EnvironmentalInputs
import com.example.model.UnitSystem
import com.example.model.WindDirectionConvention
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun VectorMathBreakdownDialog(
    inputs: EnvironmentalInputs,
    result: DriftCalculationResult,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = OceanCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("vector_math_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DRIFT PHYSICS ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.2.sp,
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vector Decomposition & Kinetics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_breakdown_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Vector Sum Equation
                FormulaCard(
                    title = "1. Resultant Drift Vector Sum",
                    formula = "V_drift = V_current + V_wind_leeway",
                    explanation = "Surface slick transport is the vector addition of hydrodynamic water movement and atmospheric leeway."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Component Breakdowns
                VectorComponentRow(
                    label = "Ocean Current Vector",
                    speed = "${inputs.currentSpeedKnots} kt",
                    heading = "${String.format("%.1f°", inputs.currentDirectionDeg)} towards",
                    vectorX = result.currentVector.x,
                    vectorY = result.currentVector.y,
                    color = HydroBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                val windPushAngle = when (inputs.windConvention) {
                    WindDirectionConvention.METEOROLOGICAL_FROM -> (inputs.windDirectionDeg + 180f) % 360f
                    WindDirectionConvention.OCEANOGRAPHIC_TOWARDS -> inputs.windDirectionDeg
                }
                val leewayHeading = (windPushAngle + inputs.coriolisDeflectionDeg) % 360f

                VectorComponentRow(
                    label = "Wind Leeway Vector (${String.format("%.1f%%", inputs.windLeewayFactor * 100f)} + ${inputs.coriolisDeflectionDeg}° Coriolis)",
                    speed = "${String.format("%.2f", result.windLeewayVector.magnitude)} kt",
                    heading = "${String.format("%.1f°", leewayHeading)} push",
                    vectorX = result.windLeewayVector.x,
                    vectorY = result.windLeewayVector.y,
                    color = AeroCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                VectorComponentRow(
                    label = "Total Resultant Drift Vector",
                    speed = "${String.format("%.2f", result.driftSpeedKnots)} kt (${unitSystem.formatSpeed(result.driftSpeedKnots)})",
                    heading = "${String.format("%.1f°", result.driftHeadingDeg)} TRUE",
                    vectorX = result.totalDriftVector.x,
                    vectorY = result.totalDriftVector.y,
                    color = PetroleumAmber,
                    isTotal = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Spreading & Weathering Formulas
                FormulaCard(
                    title = "2. Mackay / Fay Spreading Model",
                    formula = "Area(t) = 10⁵ · (V₀)⁰·⁷⁵ · √t · C_spread",
                    explanation = "Initial gravity-viscous expansion transitions to surface tension spreading. Wind stretching elongates the slick along the drift axis into windrows."
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormulaCard(
                    title = "3. Logarithmic Evaporation Kinetic Model",
                    formula = "F_evap(t) = F_max · ln(1 + k·t) / ln(1 + 72·k)",
                    explanation = "Light volatile ends flash off first, accelerated by sea surface temperature (${inputs.waterTemperatureCelsius}°C) and wind stirring (${inputs.windSpeedKnots} kt)."
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormulaCard(
                    title = "4. Natural Wave Dispersion & Total Weathering",
                    formula = "Weathering(t) = Evap(t) + Disp(t) [Delvigne & Sweeney]",
                    explanation = "Breaking wave crests entrain micro-droplets into the water column. Total slick loss combines atmospheric evaporation and subsea dispersion."
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon)
                ) {
                    Text("Close Verification")
                }
            }
        }
    }
}

@Composable
private fun FormulaCard(
    title: String,
    formula: String,
    explanation: String
) {
    Surface(
        color = OceanAbyss,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = CyanNeon,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = OceanCard,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formula,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PetroleumAmber,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun VectorComponentRow(
    label: String,
    speed: String,
    heading: String,
    vectorX: Float,
    vectorY: Float,
    color: Color,
    isTotal: Boolean = false
) {
    Surface(
        color = OceanAbyss,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(if (isTotal) 1.5.dp else 1.dp, if (isTotal) color else OceanCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Text(
                    text = speed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bearing: $heading",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "[Vx = ${String.format("%+.2f", vectorX)}, Vy = ${String.format("%+.2f", vectorY)}]",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
