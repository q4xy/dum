package com.example.model

data class IncidentPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val inputs: EnvironmentalInputs,
    val tacticalContext: String
) {
    companion object {
        val PRESETS = listOf(
            IncidentPreset(
                id = "tanker_grounding",
                title = "Supertanker Shoal Grounding",
                subtitle = "Medium Crude, coastal shelf tidal stream",
                badge = "Tidal Straits",
                inputs = EnvironmentalInputs(
                    currentSpeedKnots = 1.8f,
                    currentDirectionDeg = 55f,
                    windSpeedKnots = 18f,
                    windDirectionDeg = 240f,
                    windConvention = WindDirectionConvention.METEOROLOGICAL_FROM,
                    windLeewayFactor = 0.033f,
                    coriolisDeflectionDeg = 10f,
                    oilType = OilType.MEDIUM_CRUDE,
                    spillVolumeM3 = 1200f,
                    waterTemperatureCelsius = 16f,
                    simulationHours = 24f
                ),
                tacticalContext = "High volume release in moderate shelf current. Moderate wind pushing slick toward coastal fishing zones within 36 hours."
            ),
            IncidentPreset(
                id = "deepwater_blowout",
                title = "Deepwater Subsea Blowout",
                subtitle = "Light Crude, oceanic loop current & open sea trade winds",
                badge = "Deepwater",
                inputs = EnvironmentalInputs(
                    currentSpeedKnots = 2.4f,
                    currentDirectionDeg = 35f,
                    windSpeedKnots = 12f,
                    windDirectionDeg = 110f,
                    windConvention = WindDirectionConvention.METEOROLOGICAL_FROM,
                    windLeewayFactor = 0.031f,
                    coriolisDeflectionDeg = 8f,
                    oilType = OilType.LIGHT_CRUDE,
                    spillVolumeM3 = 2500f,
                    waterTemperatureCelsius = 24f,
                    simulationHours = 48f
                ),
                tacticalContext = "Dominant high-velocity oceanic current. Fast evaporation of light fractions with extensive sheen elongation."
            ),
            IncidentPreset(
                id = "gale_bunker_leak",
                title = "North Sea Bunker C Breach",
                subtitle = "Heavy Fuel Oil, gale force 7 wind & choppy chop",
                badge = "Rough Seas",
                inputs = EnvironmentalInputs(
                    currentSpeedKnots = 0.9f,
                    currentDirectionDeg = 140f,
                    windSpeedKnots = 32f,
                    windDirectionDeg = 315f,
                    windConvention = WindDirectionConvention.METEOROLOGICAL_FROM,
                    windLeewayFactor = 0.035f,
                    coriolisDeflectionDeg = 12f,
                    oilType = OilType.BUNKER_C,
                    spillVolumeM3 = 400f,
                    waterTemperatureCelsius = 10f,
                    simulationHours = 36f
                ),
                tacticalContext = "Wind-dominated drift. Heavy persistent fuel resists evaporation, creating thick tar emulsion mats difficult for mechanical skimming."
            ),
            IncidentPreset(
                id = "harbor_diesel_spill",
                title = "Harbor Fueling Line Rupture",
                subtitle = "Marine Diesel, sheltered harbor estuary",
                badge = "Sheltered",
                inputs = EnvironmentalInputs(
                    currentSpeedKnots = 0.5f,
                    currentDirectionDeg = 190f,
                    windSpeedKnots = 7f,
                    windDirectionDeg = 45f,
                    windConvention = WindDirectionConvention.METEOROLOGICAL_FROM,
                    windLeewayFactor = 0.030f,
                    coriolisDeflectionDeg = 5f,
                    oilType = OilType.DIESEL,
                    spillVolumeM3 = 120f,
                    waterTemperatureCelsius = 19f,
                    simulationHours = 12f
                ),
                tacticalContext = "Calm waters, rapid volatile flash-off. Over 60% evaporates within 12 hours. Rapid boom containment viable."
            )
        )
    }
}
