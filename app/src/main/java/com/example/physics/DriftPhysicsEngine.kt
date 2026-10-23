package com.example.physics

import com.example.model.DriftCalculationResult
import com.example.model.EnvironmentalInputs
import com.example.model.TrajectoryPoint
import com.example.model.Vector2D
import com.example.model.WindDirectionConvention
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object DriftPhysicsEngine {

    fun calculate(inputs: EnvironmentalInputs): DriftCalculationResult {
        // 1. Current vector (flowing towards currentDirectionDeg)
        val currentVector = Vector2D.fromSpeedAndHeading(
            speed = max(0f, inputs.currentSpeedKnots),
            headingDeg = inputs.currentDirectionDeg
        )

        // 2. Wind vector and leeway calculations
        val windPushDirection = when (inputs.windConvention) {
            WindDirectionConvention.METEOROLOGICAL_FROM -> (inputs.windDirectionDeg + 180f) % 360f
            WindDirectionConvention.OCEANOGRAPHIC_TOWARDS -> inputs.windDirectionDeg % 360f
        }

        // Apply Coriolis deflection (to the right in Northern Hemisphere)
        val windDriftHeading = (windPushDirection + inputs.coriolisDeflectionDeg + 360f) % 360f
        val leewaySpeed = max(0f, inputs.windSpeedKnots) * inputs.windLeewayFactor

        val windLeewayVector = Vector2D.fromSpeedAndHeading(
            speed = leewaySpeed,
            headingDeg = windDriftHeading
        )

        // 3. Resultant drift vector sum
        val totalDriftVector = currentVector + windLeewayVector
        val driftSpeedKnots = totalDriftVector.magnitude
        val driftHeadingDeg = totalDriftVector.headingDegrees
        val totalDisplacementNM = driftSpeedKnots * inputs.simulationHours

        // 4. Generate hourly trajectory points and weathering progression
        val trajectoryPoints = mutableListOf<TrajectoryPoint>()
        val stepHours = if (inputs.simulationHours <= 12f) 0.5f else 1.0f
        var h = 0f
        while (h <= inputs.simulationHours + 0.01f) {
            val dispX = totalDriftVector.x * h
            val dispY = totalDriftVector.y * h
            val dist = totalDriftVector.magnitude * h

            val (areaKm2, majorNM, minorNM) = calculateSlickSpreading(
                initialVolumeM3 = inputs.spillVolumeM3,
                hours = h,
                windSpeedKnots = inputs.windSpeedKnots,
                rateFactor = inputs.oilType.spreadingRateFactor
            )

            val evapPct = calculateEvaporation(
                oilType = inputs.oilType,
                hours = h,
                windSpeedKnots = inputs.windSpeedKnots,
                waterTempC = inputs.waterTemperatureCelsius
            )

            val dispPct = calculateDispersion(
                oilType = inputs.oilType,
                hours = h,
                windSpeedKnots = inputs.windSpeedKnots
            )
            val totalWeathered = min(100f, evapPct + dispPct)

            val (ptLat, ptLon) = inputs.calculateCoordinates(dispX, dispY)

            trajectoryPoints.add(
                TrajectoryPoint(
                    hour = h,
                    displacementXNM = dispX,
                    displacementYNM = dispY,
                    distanceNM = dist,
                    slickMajorAxisNM = majorNM,
                    slickMinorAxisNM = minorNM,
                    slickAreaKm2 = areaKm2,
                    evaporatedPercent = evapPct,
                    dispersedPercent = dispPct,
                    totalWeatheredPercent = totalWeathered,
                    currentLat = ptLat,
                    currentLon = ptLon
                )
            )
            h += stepHours
        }

        // Current status at simulation end
        val latest = trajectoryPoints.lastOrNull() ?: TrajectoryPoint(0f, 0f, 0f, 0f, 0.1f, 0.1f, 0.01f, 0f)
        val finalEvaporated = latest.evaporatedPercent
        val finalDispersed = latest.dispersedPercent
        val finalTotalWeathered = latest.totalWeatheredPercent
        val remainingVolumeM3 = max(0f, inputs.spillVolumeM3 * (1f - (finalTotalWeathered / 100f)))

        // Emulsification water uptake
        val emulsifiedPercent = calculateEmulsification(
            oilType = inputs.oilType,
            hours = inputs.simulationHours,
            windSpeedKnots = inputs.windSpeedKnots
        )

        // Recommended boom perimeter for containment (meters)
        val majorMeters = latest.slickMajorAxisNM * 1852f
        val minorMeters = latest.slickMinorAxisNM * 1852f
        val ellipsePerimeterMeters = PI.toFloat() * sqrt(2f * (majorMeters * majorMeters + minorMeters * minorMeters))
        val containmentBoomMeters = min(15000f, ellipsePerimeterMeters * 0.75f) // Focus on leading head

        val (beaufort, seaState) = getBeaufortScale(inputs.windSpeedKnots)
        val tier = determineResponseTier(inputs.spillVolumeM3, totalDisplacementNM)

        val originCoordStr = EnvironmentalInputs.formatNautical(inputs.originLat, inputs.originLon)
        val (finalLat, finalLon) = inputs.calculateCoordinates(latest.displacementXNM, latest.displacementYNM)
        val finalCoordStr = EnvironmentalInputs.formatNautical(finalLat, finalLon)

        val shorelineDistanceNM = 28.0f
        val timeToImpact = if (driftSpeedKnots > 0.08f) {
            shorelineDistanceNM / driftSpeedKnots
        } else null

        // Generate uncertainty cone envelope along trajectory
        val waveHeightM = (inputs.windSpeedKnots * 0.08f).coerceAtLeast(0.5f)
        val uncertaintyConePoints = trajectoryPoints.map { pt ->
            val h = pt.hour
            val crossTrackError = (0.08f * driftSpeedKnots * h) + (0.10f * waveHeightM * sqrt(h)) + 0.08f
            val alongTrackError = (0.05f * driftSpeedKnots * h) + (0.05f * sqrt(h)) + 0.05f
            com.example.model.UncertaintyConePoint(
                hour = h,
                dispXNM = pt.displacementXNM,
                dispYNM = pt.displacementYNM,
                crossTrackErrorNM = crossTrackError,
                alongTrackErrorNM = alongTrackError,
                prob50RadiusNM = crossTrackError * 1.177f,
                prob75RadiusNM = crossTrackError * 1.665f,
                prob90RadiusNM = crossTrackError * 2.146f
            )
        }

        // Calculate Outcome Confidence Rating Score (%)
        val baseScore = 94
        val wavePenalty = (waveHeightM * 2.5f).toInt().coerceAtMost(14)
        val timePenalty = (max(0f, inputs.simulationHours - 12f) * 0.4f).toInt().coerceAtMost(16)
        val confidenceScore = (baseScore - wavePenalty - timePenalty).coerceIn(55, 98)

        val confidenceLabel = when {
            confidenceScore >= 85 -> "HIGH ($confidenceScore% Ground-Truth Confidence)"
            confidenceScore >= 72 -> "MODERATE ($confidenceScore% Trajectory Certainty)"
            else -> "ELEVATED SPREAD ($confidenceScore% High Shear Zone)"
        }

        return DriftCalculationResult(
            currentVector = currentVector,
            windLeewayVector = windLeewayVector,
            totalDriftVector = totalDriftVector,
            driftSpeedKnots = driftSpeedKnots,
            driftHeadingDeg = driftHeadingDeg,
            totalDisplacementNM = totalDisplacementNM,
            trajectoryPoints = trajectoryPoints,
            slickAreaKm2 = latest.slickAreaKm2,
            slickMajorAxisNM = latest.slickMajorAxisNM,
            slickMinorAxisNM = latest.slickMinorAxisNM,
            evaporatedPercent = finalEvaporated,
            dispersedPercent = finalDispersed,
            totalWeatheredPercent = finalTotalWeathered,
            emulsifiedPercent = emulsifiedPercent,
            remainingVolumeM3 = remainingVolumeM3,
            containmentBoomMeters = containmentBoomMeters,
            beaufortScale = beaufort,
            seaStateDescription = seaState,
            responseTierRecommendation = tier,
            originCoordinatesFormatted = originCoordStr,
            currentHeadCoordinatesFormatted = finalCoordStr,
            projectedShorelineDistanceNM = shorelineDistanceNM,
            estimatedTimeToImpactHours = timeToImpact,
            uncertaintyConePoints = uncertaintyConePoints,
            outcomeConfidenceScore = confidenceScore,
            confidenceRatingLabel = confidenceLabel
        )
    }

    /**
     * Modified Mackay/Fay spreading model
     */
    fun calculateSlickSpreading(
        initialVolumeM3: Float,
        hours: Float,
        windSpeedKnots: Float,
        rateFactor: Float
    ): Triple<Float, Float, Float> {
        val t = max(0.1f, hours)
        // Mackay empirical formula for area in m^2
        val baseAreaM2 = 1.0e5f * (initialVolumeM3.toDouble().pow(0.75)).toFloat() * sqrt(t) * rateFactor
        val areaKm2 = baseAreaM2 / 1_000_000f

        // Wind elongates slick into windrows / ellipse (aspect ratio 1.5 to 4.0)
        val elongation = min(4.0f, 1.2f + 0.08f * windSpeedKnots)
        // Area = pi * a * b, with a = b * elongation => Area = pi * b^2 * elongation
        val minorMeters = sqrt(baseAreaM2 / (PI.toFloat() * elongation))
        val majorMeters = minorMeters * elongation

        val majorNM = majorMeters / 1852f
        val minorNM = minorMeters / 1852f

        return Triple(areaKm2, majorNM, minorNM)
    }

    /**
     * Empirical logarithmic evaporation model calibrated to oil distillation groups
     */
    fun calculateEvaporation(
        oilType: com.example.model.OilType,
        hours: Float,
        windSpeedKnots: Float,
        waterTempC: Float
    ): Float {
        if (hours <= 0f) return 0f
        val maxEvap = oilType.maxEvaporationPercent
        val tempEffect = max(0.5f, waterTempC / 20.0f)
        val windEffect = 1.0f + 0.025f * windSpeedKnots
        val kineticFactor = 0.18f * oilType.evaporationRateFactor * tempEffect * windEffect

        val fraction = ln(1.0 + kineticFactor * hours) / ln(1.0 + kineticFactor * 72.0)
        return min(maxEvap, (maxEvap * fraction).toFloat())
    }

    /**
     * Natural ocean wave dispersion (Delvigne & Sweeney breaking wave model approximation)
     */
    fun calculateDispersion(
        oilType: com.example.model.OilType,
        hours: Float,
        windSpeedKnots: Float
    ): Float {
        if (hours <= 0f || windSpeedKnots < 5f) return 0f
        val waveFactor = ((windSpeedKnots - 5f) / 25f).coerceIn(0f, 2.5f)
        val maxDispersion = when (oilType.group) {
            com.example.model.OilGroup.GROUP_1_VERY_LIGHT -> 28f
            com.example.model.OilGroup.GROUP_2_LIGHT -> 20f
            com.example.model.OilGroup.GROUP_3_MEDIUM -> 12f
            com.example.model.OilGroup.GROUP_4_HEAVY -> 4f
        }
        val rate = 0.06f * oilType.spreadingRateFactor
        val fraction = 1.0f - kotlin.math.exp(-rate * hours * waveFactor)
        return min(maxDispersion, (maxDispersion * fraction).toFloat())
    }

    /**
     * Water-in-oil emulsification (mousse formation)
     */
    private fun calculateEmulsification(
        oilType: com.example.model.OilType,
        hours: Float,
        windSpeedKnots: Float
    ): Float {
        if (oilType.group == com.example.model.OilGroup.GROUP_1_VERY_LIGHT ||
            oilType.group == com.example.model.OilGroup.GROUP_2_LIGHT) {
            return 0f // Light fuels don't form stable emulsions
        }
        if (hours <= 0f) return 0f
        val windStirring = min(2.5f, 0.5f + (windSpeedKnots / 15f))
        val potential = if (oilType.group == com.example.model.OilGroup.GROUP_4_HEAVY) 80f else 65f
        val progress = min(1.0f, (hours / 36.0f) * windStirring)
        return potential * progress
    }

    private fun getBeaufortScale(windKnots: Float): Pair<Int, String> {
        return when {
            windKnots < 1f -> Pair(0, "Calm (Mirror flat sea)")
            windKnots <= 3f -> Pair(1, "Light Air (Ripples, no crests)")
            windKnots <= 6f -> Pair(2, "Light Breeze (Small wavelets)")
            windKnots <= 10f -> Pair(3, "Gentle Breeze (Large wavelets, scattered whitecaps)")
            windKnots <= 16f -> Pair(4, "Moderate Breeze (Small waves, frequent whitecaps)")
            windKnots <= 21f -> Pair(5, "Fresh Breeze (Moderate waves, spray)")
            windKnots <= 27f -> Pair(6, "Strong Breeze (Large waves, white foam crests)")
            windKnots <= 33f -> Pair(7, "Near Gale (Sea heaps up, foam streaks)")
            windKnots <= 40f -> Pair(8, "Gale (Moderately high waves, breaking crests)")
            else -> Pair(9, "Severe Gale / Storm (High rolling waves)")
        }
    }

    private fun determineResponseTier(volumeM3: Float, displacementNM: Float): String {
        return when {
            volumeM3 < 50f && displacementNM < 5f -> "Tier 1: Local Port / Terminal Response"
            volumeM3 < 1000f && displacementNM < 25f -> "Tier 2: Regional Mutual Aid & Coastal Flotilla"
            else -> "Tier 3: National & International Major Incident Response"
        }
    }
}
