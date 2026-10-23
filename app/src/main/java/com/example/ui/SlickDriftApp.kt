package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UnitSystem
import com.example.ui.components.IncidentTelemetryHeader
import com.example.ui.components.InputControlPanel
import com.example.ui.components.MaritimeSitrepDialog
import com.example.ui.components.OceanRadarCanvas
import com.example.ui.components.ScenarioPresetSelector
import com.example.ui.components.TimelineScrubber
import com.example.ui.components.VectorMathBreakdownDialog
import com.example.ui.components.WeatheringSummaryCard
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.OceanSurface
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.OilDriftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlickDriftApp(
    viewModel: OilDriftViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSitrepDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OceanAbyss,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OceanCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = CyanNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SLICKDRIFT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(RadarGreen)
                                )
                            }
                            Text(
                                text = "Ocean Oil Spill Drift & Trajectory Simulator",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanNeon,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Units Dropdown Menu
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OceanCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("units_selector_button")
                        ) {
                            TextButton(
                                onClick = { showMenu = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = uiState.unitSystem.speedUnit.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanNeon
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(OceanCard)
                        ) {
                            UnitSystem.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = unit.label,
                                            color = if (unit == uiState.unitSystem) CyanNeon else TextPrimary,
                                            fontWeight = if (unit == uiState.unitSystem) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setUnitSystem(unit)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showSitrepDialog = true },
                        modifier = Modifier.testTag("appbar_sitrep_button")
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = "Situation Report", tint = CyanNeon)
                    }

                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("about_app_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "About Drift Physics", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OceanAbyss,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("main_scrollable_container"),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Professional Maritime Incident Telemetry & Coordinates Header
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IncidentTelemetryHeader(
                        inputs = uiState.inputs,
                        result = uiState.result,
                        selectedHour = uiState.selectedHour,
                        unitSystem = uiState.unitSystem,
                        onOpenSitrep = { showSitrepDialog = true }
                    )
                }
            }

            // 2. Live Marine Ocean & NOAA Station Feeds (Real-World Telemetry)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    com.example.ui.components.LiveMarineStationSelector(
                        result = uiState.result,
                        isSyncing = uiState.isSyncingLiveFeed,
                        statusMessage = uiState.liveFeedStatusMessage,
                        onSelectBasin = { basin -> viewModel.applyHighRiskBasin(basin) },
                        onFetchGpsLocation = {
                            viewModel.fetchLiveObservationForCurrentLocation(
                                lat = uiState.inputs.originLat,
                                lon = uiState.inputs.originLon
                            )
                        }
                    )
                }
            }

            // 3. Incident Scenario Quick Presets
            item {
                ScenarioPresetSelector(
                    activePresetId = uiState.activePresetId,
                    onSelectPreset = { preset -> viewModel.applyPreset(preset) }
                )
            }

            // 3. Interactive Nautical Radar Canvas
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OceanRadarCanvas(
                        result = uiState.result,
                        selectedHour = uiState.selectedHour,
                        unitSystem = uiState.unitSystem,
                        onHourChanged = { hour -> viewModel.setSelectedHour(hour) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    )
                }
            }

            // 4. Simulation Timeline Scrubber (0h to 72h)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TimelineScrubber(
                        selectedHour = uiState.selectedHour,
                        maxHours = uiState.inputs.simulationHours,
                        isPlaying = uiState.isPlaying,
                        onHourChanged = { hour -> viewModel.setSelectedHour(hour) },
                        onTogglePlayback = { viewModel.togglePlayback() }
                    )
                }
            }

            // 5. Tactical Drift Telemetry Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    WeatheringSummaryCard(
                        result = uiState.result,
                        selectedHour = uiState.selectedHour,
                        unitSystem = uiState.unitSystem,
                        onOpenBreakdown = { viewModel.setBreakdownDialogOpen(true) }
                    )
                }
            }

            // 5. Parameter Input & Environmental Control Panel (Vectors, Oil, Containment)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    InputControlPanel(
                        inputs = uiState.inputs,
                        result = uiState.result,
                        unitSystem = uiState.unitSystem,
                        activeTab = uiState.activeTab,
                        onTabSelected = { tab -> viewModel.setActiveTab(tab) },
                        onCurrentSpeedChanged = { speed -> viewModel.updateCurrentSpeed(speed) },
                        onCurrentDirectionChanged = { deg -> viewModel.updateCurrentDirection(deg) },
                        onWindSpeedChanged = { speed -> viewModel.updateWindSpeed(speed) },
                        onWindDirectionChanged = { deg -> viewModel.updateWindDirection(deg) },
                        onWindConventionChanged = { conv -> viewModel.updateWindConvention(conv) },
                        onOilTypeChanged = { oil -> viewModel.updateOilType(oil) },
                        onSpillVolumeChanged = { vol -> viewModel.updateSpillVolume(vol) },
                        onWaterTempChanged = { temp -> viewModel.updateWaterTemperature(temp) },
                        onSimulationHoursChanged = { hours -> viewModel.updateSimulationHours(hours) },
                        onWindLeewayChanged = { factor -> viewModel.updateWindLeewayFactor(factor) },
                        onCoriolisChanged = { deg -> viewModel.updateCoriolisDeflection(deg) }
                    )
                }
            }
        }

        // Vector Math Breakdown Dialog
        if (uiState.isBreakdownDialogOpen) {
            VectorMathBreakdownDialog(
                inputs = uiState.inputs,
                result = uiState.result,
                unitSystem = uiState.unitSystem,
                onDismiss = { viewModel.setBreakdownDialogOpen(false) }
            )
        }

        // About / Physics Model Info Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                containerColor = OceanCard,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About Drift Physics", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SlickDrift simulates ocean surface oil transport utilizing established empirical maritime equations:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "• Vector Leeway: Surface slicks drift with 100% of ocean currents plus 3.0%-3.5% of 10m surface wind speed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = "• Coriolis Deflection: Deflects wind-driven drift 5° to 15° to the right in the Northern Hemisphere.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = "• Mackay/Fay Spreading: Accounts for initial gravity-viscous expansion followed by surface tension spreading.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = "• Evaporation Kinetics: Calibrated across API gravity groups from rapid diesel flash-off to persistent Bunker C.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Understood", color = CyanNeon, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Maritime Situation Report (SITREP) Dialog
        if (showSitrepDialog) {
            MaritimeSitrepDialog(
                inputs = uiState.inputs,
                result = uiState.result,
                selectedHour = uiState.selectedHour,
                unitSystem = uiState.unitSystem,
                onDismiss = { showSitrepDialog = false }
            )
        }
    }
}
