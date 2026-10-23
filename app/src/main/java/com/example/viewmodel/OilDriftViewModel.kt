package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DriftCalculationResult
import com.example.model.EnvironmentalInputs
import com.example.model.IncidentPreset
import com.example.model.OilType
import com.example.model.UnitSystem
import com.example.model.WindDirectionConvention
import com.example.physics.DriftPhysicsEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OilDriftUiState(
    val inputs: EnvironmentalInputs = EnvironmentalInputs(),
    val result: DriftCalculationResult = DriftPhysicsEngine.calculate(EnvironmentalInputs()),
    val unitSystem: UnitSystem = UnitSystem.NAUTICAL,
    val selectedHour: Float = 24f,
    val isPlaying: Boolean = false,
    val activePresetId: String? = null,
    val isBreakdownDialogOpen: Boolean = false,
    val activeTab: Int = 0, // 0: Vectors, 1: Spill Details, 2: Containment Tactics
    val isSyncingLiveFeed: Boolean = false,
    val liveFeedStatusMessage: String? = null,
    val showLiveBasinSelector: Boolean = false
)

class OilDriftViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OilDriftUiState())
    val uiState: StateFlow<OilDriftUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Initial state calculation
        recalculate()
    }

    private fun recalculate() {
        val currentInputs = _uiState.value.inputs
        val newResult = DriftPhysicsEngine.calculate(currentInputs)
        _uiState.update { state ->
            val boundedHour = state.selectedHour.coerceIn(0f, currentInputs.simulationHours)
            state.copy(
                result = newResult,
                selectedHour = boundedHour
            )
        }
    }

    fun updateCurrentSpeed(speedKnots: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(currentSpeedKnots = speedKnots), activePresetId = null) }
        recalculate()
    }

    fun updateCurrentDirection(deg: Float) {
        val normalized = (deg % 360f + 360f) % 360f
        _uiState.update { it.copy(inputs = it.inputs.copy(currentDirectionDeg = normalized), activePresetId = null) }
        recalculate()
    }

    fun updateWindSpeed(speedKnots: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(windSpeedKnots = speedKnots), activePresetId = null) }
        recalculate()
    }

    fun updateWindDirection(deg: Float) {
        val normalized = (deg % 360f + 360f) % 360f
        _uiState.update { it.copy(inputs = it.inputs.copy(windDirectionDeg = normalized), activePresetId = null) }
        recalculate()
    }

    fun updateWindConvention(convention: WindDirectionConvention) {
        _uiState.update { it.copy(inputs = it.inputs.copy(windConvention = convention), activePresetId = null) }
        recalculate()
    }

    fun updateOilType(oilType: OilType) {
        _uiState.update { it.copy(inputs = it.inputs.copy(oilType = oilType), activePresetId = null) }
        recalculate()
    }

    fun updateSpillVolume(volumeM3: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(spillVolumeM3 = volumeM3), activePresetId = null) }
        recalculate()
    }

    fun updateWaterTemperature(tempC: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(waterTemperatureCelsius = tempC), activePresetId = null) }
        recalculate()
    }

    fun updateSimulationHours(hours: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(simulationHours = hours), activePresetId = null) }
        recalculate()
    }

    fun updateCoriolisDeflection(deg: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(coriolisDeflectionDeg = deg), activePresetId = null) }
        recalculate()
    }

    fun updateWindLeewayFactor(factor: Float) {
        _uiState.update { it.copy(inputs = it.inputs.copy(windLeewayFactor = factor), activePresetId = null) }
        recalculate()
    }

    fun applyPreset(preset: IncidentPreset) {
        _uiState.update {
            it.copy(
                inputs = preset.inputs,
                activePresetId = preset.id,
                selectedHour = preset.inputs.simulationHours
            )
        }
        recalculate()
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        _uiState.update { it.copy(unitSystem = unitSystem) }
    }

    fun setSelectedHour(hour: Float) {
        _uiState.update { it.copy(selectedHour = hour.coerceIn(0f, it.inputs.simulationHours)) }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun setBreakdownDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isBreakdownDialogOpen = open) }
    }

    fun setShowLiveBasinSelector(show: Boolean) {
        _uiState.update { it.copy(showLiveBasinSelector = show) }
    }

    fun applyHighRiskBasin(basin: com.example.repository.HighRiskBasin) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncingLiveFeed = true,
                    liveFeedStatusMessage = "Syncing ${basin.name} Telemetry...",
                    showLiveBasinSelector = false
                )
            }

            val obs = com.example.repository.MarineRepository.fetchLiveObservation(
                lat = basin.originLat,
                lon = basin.originLon,
                stationName = basin.name
            )

            _uiState.update { state ->
                val newInputs = state.inputs.copy(
                    originLat = basin.originLat,
                    originLon = basin.originLon,
                    currentSpeedKnots = obs.oceanCurrentSpeedKnots,
                    currentDirectionDeg = obs.oceanCurrentDirectionDeg,
                    windSpeedKnots = obs.windSpeedKnots,
                    windDirectionDeg = obs.windDirectionDeg,
                    waterTemperatureCelsius = obs.waterTempCelsius,
                    incidentCallsign = basin.noaaStationCode
                )
                val newResult = DriftPhysicsEngine.calculate(newInputs).copy(
                    liveObservation = obs
                )
                state.copy(
                    inputs = newInputs,
                    result = newResult,
                    isSyncingLiveFeed = false,
                    liveFeedStatusMessage = "Live Feed Connected: ${obs.stationName}"
                )
            }
        }
    }

    fun fetchLiveObservationForCurrentLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncingLiveFeed = true,
                    liveFeedStatusMessage = "Fetching GPS Satellite Telemetry ($lat, $lon)..."
                )
            }

            val obs = com.example.repository.MarineRepository.fetchLiveObservation(
                lat = lat,
                lon = lon,
                stationName = "GPS Offshore Fix"
            )

            _uiState.update { state ->
                val newInputs = state.inputs.copy(
                    originLat = lat,
                    originLon = lon,
                    currentSpeedKnots = obs.oceanCurrentSpeedKnots,
                    currentDirectionDeg = obs.oceanCurrentDirectionDeg,
                    windSpeedKnots = obs.windSpeedKnots,
                    windDirectionDeg = obs.windDirectionDeg,
                    incidentCallsign = "GPS-NOAA-SYNC"
                )
                val newResult = DriftPhysicsEngine.calculate(newInputs).copy(
                    liveObservation = obs
                )
                state.copy(
                    inputs = newInputs,
                    result = newResult,
                    isSyncingLiveFeed = false,
                    liveFeedStatusMessage = "GPS Telemetry Synced (${obs.confidenceRatingLabel} Confidence)"
                )
            }
        }
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            playbackJob?.cancel()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            _uiState.update { it.copy(isPlaying = true) }
            playbackJob = viewModelScope.launch {
                val maxHours = _uiState.value.inputs.simulationHours
                if (_uiState.value.selectedHour >= maxHours) {
                    _uiState.update { it.copy(selectedHour = 0f) }
                }
                while (_uiState.value.isPlaying && _uiState.value.selectedHour < maxHours) {
                    delay(120)
                    _uiState.update { state ->
                        val next = (state.selectedHour + 1.0f).coerceAtMost(maxHours)
                        state.copy(
                            selectedHour = next,
                            isPlaying = next < maxHours
                        )
                    }
                }
                _uiState.update { it.copy(isPlaying = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
