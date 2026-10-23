package com.example

import com.example.model.EnvironmentalInputs
import com.example.model.OilType
import com.example.model.WindDirectionConvention
import com.example.physics.DriftPhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPureCurrentDriftZeroWind() {
        val inputs = EnvironmentalInputs(
            currentSpeedKnots = 2.0f,
            currentDirectionDeg = 90f, // East
            windSpeedKnots = 0f,
            windDirectionDeg = 0f,
            simulationHours = 10f
        )
        val result = DriftPhysicsEngine.calculate(inputs)

        assertEquals(2.0f, result.driftSpeedKnots, 0.05f)
        assertEquals(90f, result.driftHeadingDeg, 0.5f)
        assertEquals(20.0f, result.totalDisplacementNM, 0.5f)
    }

    @Test
    fun testCombinedDriftCurrentAndWind() {
        // Current flowing North (000°) at 1.0 knot
        // Wind blowing FROM South (180°) -> pushing North (000°) at 30 knots
        // Leeway = 3.2% of 30kt = 0.96 kt. Coriolis = 0° for simple sum
        val inputs = EnvironmentalInputs(
            currentSpeedKnots = 1.0f,
            currentDirectionDeg = 0f,
            windSpeedKnots = 30f,
            windDirectionDeg = 180f,
            windConvention = WindDirectionConvention.METEOROLOGICAL_FROM,
            windLeewayFactor = 0.032f,
            coriolisDeflectionDeg = 0f,
            simulationHours = 5f
        )
        val result = DriftPhysicsEngine.calculate(inputs)

        val expectedSpeed = 1.0f + 0.96f
        assertEquals(expectedSpeed, result.driftSpeedKnots, 0.05f)
        assertEquals(0f, result.driftHeadingDeg, 0.5f)
        assertEquals(expectedSpeed * 5f, result.totalDisplacementNM, 0.2f)
    }

    @Test
    fun testOilWeatheringEvaporationAndSpreading() {
        val dieselInputs = EnvironmentalInputs(
            oilType = OilType.DIESEL,
            spillVolumeM3 = 500f,
            simulationHours = 24f,
            waterTemperatureCelsius = 20f
        )
        val bunkerInputs = EnvironmentalInputs(
            oilType = OilType.BUNKER_C,
            spillVolumeM3 = 500f,
            simulationHours = 24f,
            waterTemperatureCelsius = 20f
        )

        val dieselResult = DriftPhysicsEngine.calculate(dieselInputs)
        val bunkerResult = DriftPhysicsEngine.calculate(bunkerInputs)

        // Diesel must evaporate significantly more than Bunker C
        assertTrue("Diesel evaporation should exceed 30%", dieselResult.evaporatedPercent > 30f)
        assertTrue("Bunker C evaporation should be under 10%", bunkerResult.evaporatedPercent < 10f)

        // Slick area must expand from T=0 to T=24
        val initialPt = dieselResult.trajectoryPoints.first()
        val finalPt = dieselResult.trajectoryPoints.last()
        assertTrue("Slick area should increase over time", finalPt.slickAreaKm2 > initialPt.slickAreaKm2)
    }

    @Test
    fun testProjectedWeatheringEvaporationAndDispersion() {
        val inputs = EnvironmentalInputs(
            oilType = OilType.LIGHT_CRUDE,
            windSpeedKnots = 20f, // Moderate wind causes wave breaking & dispersion
            simulationHours = 24f
        )
        val result = DriftPhysicsEngine.calculate(inputs)

        val latest = result.trajectoryPoints.last()
        assertTrue("Evaporation should be positive", latest.evaporatedPercent > 0f)
        assertTrue("Dispersion in 20kt wind should be positive", latest.dispersedPercent > 0f)
        assertEquals(latest.evaporatedPercent + latest.dispersedPercent, latest.totalWeatheredPercent, 0.01f)
        assertTrue("Total weathered should exceed pure evaporation", latest.totalWeatheredPercent > latest.evaporatedPercent)
    }

    @Test
    fun testUncertaintyConeAndHighRiskBasins() {
        val inputs = EnvironmentalInputs(
            currentSpeedKnots = 1.5f,
            windSpeedKnots = 20f,
            simulationHours = 24f
        )
        val result = DriftPhysicsEngine.calculate(inputs)

        assertTrue("Uncertainty cone points should be generated for trajectory", result.uncertaintyConePoints.isNotEmpty())
        val conePt24 = result.uncertaintyConePoints.last()
        assertTrue("90% confidence radius should expand over time", conePt24.prob90RadiusNM > conePt24.prob50RadiusNM)
        assertTrue("Outcome confidence score should be between 50% and 100%", result.outcomeConfidenceScore in 50..100)

        // Verify High-Risk Basin catalog
        val basins = com.example.repository.MarineRepository.HIGH_RISK_BASINS
        assertTrue("High risk basins catalog must contain major shipping corridors", basins.size >= 4)
        val gulf = basins.first { it.id == "gulf_mexico" }
        assertEquals(28.7367, gulf.originLat, 0.001)
    }
}
