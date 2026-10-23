package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriftCalculationResult
import com.example.model.EnvironmentalInputs
import com.example.model.OilType
import com.example.model.UnitSystem
import com.example.model.WindDirectionConvention
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.PetroleumOrange
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun InputControlPanel(
    inputs: EnvironmentalInputs,
    result: DriftCalculationResult,
    unitSystem: UnitSystem,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    onCurrentSpeedChanged: (Float) -> Unit,
    onCurrentDirectionChanged: (Float) -> Unit,
    onWindSpeedChanged: (Float) -> Unit,
    onWindDirectionChanged: (Float) -> Unit,
    onWindConventionChanged: (WindDirectionConvention) -> Unit,
    onOilTypeChanged: (OilType) -> Unit,
    onSpillVolumeChanged: (Float) -> Unit,
    onWaterTempChanged: (Float) -> Unit,
    onSimulationHoursChanged: (Float) -> Unit,
    onWindLeewayChanged: (Float) -> Unit,
    onCoriolisChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OceanCard)
            .border(1.dp, OceanCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("input_control_panel")
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = OceanAbyss,
            contentColor = CyanNeon,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = CyanNeon
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text("VECTORS", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CompassCalibration, contentDescription = "Vectors", modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_vectors")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text("OIL DYNAMICS", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Opacity, contentDescription = "Oil", modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_oil")
            )
            Tab(
                selected = activeTab == 2,
                onClick = { onTabSelected(2) },
                text = { Text("CONTAINMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Shield, contentDescription = "Containment", modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_containment")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTab) {
            0 -> VectorControlsTab(
                inputs = inputs,
                unitSystem = unitSystem,
                onCurrentSpeedChanged = onCurrentSpeedChanged,
                onCurrentDirectionChanged = onCurrentDirectionChanged,
                onWindSpeedChanged = onWindSpeedChanged,
                onWindDirectionChanged = onWindDirectionChanged,
                onWindConventionChanged = onWindConventionChanged,
                onWindLeewayChanged = onWindLeewayChanged,
                onCoriolisChanged = onCoriolisChanged
            )
            1 -> OilDynamicsTab(
                inputs = inputs,
                unitSystem = unitSystem,
                onOilTypeChanged = onOilTypeChanged,
                onSpillVolumeChanged = onSpillVolumeChanged,
                onWaterTempChanged = onWaterTempChanged,
                onSimulationHoursChanged = onSimulationHoursChanged
            )
            2 -> ContainmentTacticsTab(
                inputs = inputs,
                result = result,
                unitSystem = unitSystem
            )
        }
    }
}

@Composable
private fun VectorControlsTab(
    inputs: EnvironmentalInputs,
    unitSystem: UnitSystem,
    onCurrentSpeedChanged: (Float) -> Unit,
    onCurrentDirectionChanged: (Float) -> Unit,
    onWindSpeedChanged: (Float) -> Unit,
    onWindDirectionChanged: (Float) -> Unit,
    onWindConventionChanged: (WindDirectionConvention) -> Unit,
    onWindLeewayChanged: (Float) -> Unit,
    onCoriolisChanged: (Float) -> Unit
) {
    var showAdvanced by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- SECTION 1: OCEAN CURRENT ---
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Water, contentDescription = null, tint = HydroBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OCEAN SURFACE CURRENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HydroBlue
                        )
                    }
                    Text(
                        text = unitSystem.formatSpeed(inputs.currentSpeedKnots),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HydroBlue
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = inputs.currentSpeedKnots,
                    onValueChange = onCurrentSpeedChanged,
                    valueRange = 0f..5.0f,
                    colors = SliderDefaults.colors(thumbColor = HydroBlue, activeTrackColor = HydroBlue),
                    modifier = Modifier.testTag("current_speed_slider")
                )

                Spacer(modifier = Modifier.height(4.dp))

                DirectionDial(
                    label = "Current Flow Direction",
                    subLabel = "Heading water is moving towards",
                    currentDegrees = inputs.currentDirectionDeg,
                    accentColor = HydroBlue,
                    onDegreesChanged = onCurrentDirectionChanged
                )
            }
        }

        // --- SECTION 2: SURFACE WIND ---
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WindPower, contentDescription = null, tint = AeroCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SURFACE WIND",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AeroCyan
                        )
                    }
                    Text(
                        text = unitSystem.formatSpeed(inputs.windSpeedKnots),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AeroCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = inputs.windSpeedKnots,
                    onValueChange = onWindSpeedChanged,
                    valueRange = 0f..50.0f,
                    colors = SliderDefaults.colors(thumbColor = AeroCyan, activeTrackColor = AeroCyan),
                    modifier = Modifier.testTag("wind_speed_slider")
                )

                // Wind Direction Convention Selector
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wind Convention:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = inputs.windConvention == WindDirectionConvention.METEOROLOGICAL_FROM,
                            onClick = { onWindConventionChanged(WindDirectionConvention.METEOROLOGICAL_FROM) },
                            label = { Text("From (Met)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AeroCyan.copy(alpha = 0.25f),
                                selectedLabelColor = AeroCyan
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                        FilterChip(
                            selected = inputs.windConvention == WindDirectionConvention.OCEANOGRAPHIC_TOWARDS,
                            onClick = { onWindConventionChanged(WindDirectionConvention.OCEANOGRAPHIC_TOWARDS) },
                            label = { Text("Towards (Ocean)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AeroCyan.copy(alpha = 0.25f),
                                selectedLabelColor = AeroCyan
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                DirectionDial(
                    label = "Wind Direction",
                    subLabel = inputs.windConvention.helperText,
                    currentDegrees = inputs.windDirectionDeg,
                    accentColor = AeroCyan,
                    onDegreesChanged = onWindDirectionChanged
                )
            }
        }

        // Advanced Parameters Toggle
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdvanced = !showAdvanced }
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Physics Tuning: Leeway Factor & Coriolis",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanNeon,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (showAdvanced) "▲ Collapse" else "▼ Expand",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        if (showAdvanced) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = OceanAbyss,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Wind Leeway Factor (Typically 3.0% - 3.5%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            "${String.format("%.1f", inputs.windLeewayFactor * 100f)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                    Slider(
                        value = inputs.windLeewayFactor,
                        onValueChange = onWindLeewayChanged,
                        valueRange = 0.015f..0.055f,
                        colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Coriolis Leeway Deflection (Right in North)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            "${String.format("%+.1f°", inputs.coriolisDeflectionDeg)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                    Slider(
                        value = inputs.coriolisDeflectionDeg,
                        onValueChange = onCoriolisChanged,
                        valueRange = -20f..20f,
                        colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
                    )
                }
            }
        }
    }
}

@Composable
private fun OilDynamicsTab(
    inputs: EnvironmentalInputs,
    unitSystem: UnitSystem,
    onOilTypeChanged: (OilType) -> Unit,
    onSpillVolumeChanged: (Float) -> Unit,
    onWaterTempChanged: (Float) -> Unit,
    onSimulationHoursChanged: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT HYDROCARBON TYPE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PetroleumAmber
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Oil Type Selector List
        OilType.ALL.forEach { oil ->
            val isSelected = inputs.oilType.id == oil.id
            Surface(
                color = if (isSelected) OceanAbyss else OceanCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) PetroleumAmber else OceanCardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOilTypeChanged(oil) }
                    .testTag("oil_type_${oil.id}")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = oil.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PetroleumAmber else TextPrimary
                        )
                        Text(
                            text = "${oil.apiGravity}° API",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CyanNeon else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = oil.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spill Volume Slider
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Initial Spill Volume", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        unitSystem.formatVolume(inputs.spillVolumeM3),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PetroleumAmber
                    )
                }
                Slider(
                    value = inputs.spillVolumeM3,
                    onValueChange = onSpillVolumeChanged,
                    valueRange = 50f..5000f,
                    colors = SliderDefaults.colors(thumbColor = PetroleumAmber, activeTrackColor = PetroleumAmber),
                    modifier = Modifier.testTag("spill_volume_slider")
                )
            }
        }

        // Water Temperature Slider
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sea Surface Water Temperature", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "${String.format("%.0f", inputs.waterTemperatureCelsius)}°C",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }
                Slider(
                    value = inputs.waterTemperatureCelsius,
                    onValueChange = onWaterTempChanged,
                    valueRange = 2f..35f,
                    colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon),
                    modifier = Modifier.testTag("water_temp_slider")
                )
            }
        }

        // Simulation Horizon Slider
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trajectory Forecast Horizon", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "${String.format("%.0f", inputs.simulationHours)} Hours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RadarGreen
                    )
                }
                Slider(
                    value = inputs.simulationHours,
                    onValueChange = onSimulationHoursChanged,
                    valueRange = 6f..72f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = RadarGreen, activeTrackColor = RadarGreen),
                    modifier = Modifier.testTag("simulation_hours_slider")
                )
            }
        }
    }
}

@Composable
private fun ContainmentTacticsTab(
    inputs: EnvironmentalInputs,
    result: DriftCalculationResult,
    unitSystem: UnitSystem
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "MARITIME SPILL RESPONSE DIRECTIVES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = CyanNeon
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Containment Boom Requirement Card
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("RECOMMENDED BOOM DEPLOYMENT", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%,.0f", result.containmentBoomMeters)} meters (${String.format("%,.0f", result.containmentBoomMeters * 3.28084f)} ft)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PetroleumAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calculated for J-configuration towing or offshore diversion boom to intercept the leading edge of the thick oil core.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Mechanical Skimming Feasibility
        val skimmerFeasible = inputs.windSpeedKnots <= 20f && inputs.currentSpeedKnots <= 2.5f
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MECHANICAL SKIMMING", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Surface(
                        color = if (skimmerFeasible) RadarGreen.copy(alpha = 0.2f) else PetroleumOrange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (skimmerFeasible) "FEASIBLE" else "LIMITED / MARGINAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (skimmerFeasible) RadarGreen else PetroleumOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (skimmerFeasible)
                        "Sea state allows high-recovery oleophilic disc / brush skimmers. Current and wind speeds are within typical containment limits (<0.7 kt tow speed)."
                    else
                        "High waves or fast current (>0.7 kt relative) will cause boom entrainment failure. Consider chemical dispersants or defensive shoreline deflection.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Dispersant Suitability
        val dispersantOk = inputs.oilType.group == com.example.model.OilGroup.GROUP_2_LIGHT ||
                inputs.oilType.group == com.example.model.OilGroup.GROUP_3_MEDIUM
        Surface(
            color = OceanAbyss,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AERIAL / VESSEL DISPERSANT", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Surface(
                        color = if (dispersantOk) CyanNeon.copy(alpha = 0.2f) else TextMuted.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (dispersantOk) "VIABLE CANDIDATE" else "NOT RECOMMENDED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (dispersantOk) CyanNeon else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (dispersantOk)
                        "Crude viscosity is suitable for Corexit/Dasic type dispersants before emulsification exceeds 50% water content."
                    else
                        "Very heavy fuels (Bunker C) or highly refined products (Diesel) are not effective candidates for chemical dispersants.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
