package com.example.model

data class Vector2D(
    val x: Float, // Easting (nautical miles or knots)
    val y: Float  // Northing (nautical miles or knots)
) {
    val magnitude: Float
        get() = kotlin.math.sqrt(x * x + y * y)

    // Heading in degrees true (0° is North, 90° East, 180° South, 270° West)
    val headingDegrees: Float
        get() {
            var deg = Math.toDegrees(kotlin.math.atan2(x.toDouble(), y.toDouble())).toFloat()
            if (deg < 0f) deg += 360f
            return deg
        }

    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun times(scalar: Float): Vector2D = Vector2D(x * scalar, y * scalar)

    companion object {
        val ZERO = Vector2D(0f, 0f)

        fun fromSpeedAndHeading(speed: Float, headingDeg: Float): Vector2D {
            val rad = Math.toRadians(headingDeg.toDouble())
            val x = (speed * kotlin.math.sin(rad)).toFloat()
            val y = (speed * kotlin.math.cos(rad)).toFloat()
            return Vector2D(x, y)
        }
    }
}

data class TrajectoryPoint(
    val hour: Float,
    val displacementXNM: Float, // NM east/west of origin
    val displacementYNM: Float, // NM north/south of origin
    val distanceNM: Float,
    val slickMajorAxisNM: Float,
    val slickMinorAxisNM: Float,
    val slickAreaKm2: Float,
    val evaporatedPercent: Float,
    val dispersedPercent: Float = 0f,
    val totalWeatheredPercent: Float = evaporatedPercent,
    val currentLat: Double = 28.7367,
    val currentLon: Double = -88.3653
)

data class EnvironmentalInputs(
    val currentSpeedKnots: Float = 1.2f,
    val currentDirectionDeg: Float = 65f, // Direction current is flowing TOWARDS
    val windSpeedKnots: Float = 16.0f,
    val windDirectionDeg: Float = 220f, // Direction wind is blowing FROM
    val windConvention: WindDirectionConvention = WindDirectionConvention.METEOROLOGICAL_FROM,
    val windLeewayFactor: Float = 0.032f, // 3.2%
    val coriolisDeflectionDeg: Float = 10.0f, // +10° rightward leeway in Northern Hemisphere
    val oilType: OilType = OilType.MEDIUM_CRUDE,
    val spillVolumeM3: Float = 750f, // ~4,700 bbl
    val waterTemperatureCelsius: Float = 20f,
    val simulationHours: Float = 24f,
    val originLat: Double = 28.7367,
    val originLon: Double = -88.3653,
    val incidentCallsign: String = "SITREP-ALPHA-09"
) {
    fun calculateCoordinates(dispXNM: Float, dispYNM: Float): Pair<Double, Double> {
        val latDeg = originLat + (dispYNM / 60.0)
        val avgLatRad = Math.toRadians((originLat + latDeg) / 2.0)
        val lonDeg = originLon + (dispXNM / (60.0 * kotlin.math.cos(avgLatRad)))
        return Pair(latDeg, lonDeg)
    }

    companion object {
        fun formatNautical(lat: Double, lon: Double): String {
            val latHem = if (lat >= 0) "N" else "S"
            val absLat = kotlin.math.abs(lat)
            val latDeg = absLat.toInt()
            val latMin = (absLat - latDeg) * 60.0

            val lonHem = if (lon >= 0) "E" else "W"
            val absLon = kotlin.math.abs(lon)
            val lonDeg = absLon.toInt()
            val lonMin = (absLon - lonDeg) * 60.0

            return String.format("%02d°%04.1f'%s  %03d°%04.1f'%s", latDeg, latMin, latHem, lonDeg, lonMin, lonHem)
        }
    }
}

data class DriftCalculationResult(
    val currentVector: Vector2D,
    val windLeewayVector: Vector2D,
    val totalDriftVector: Vector2D,
    val driftSpeedKnots: Float,
    val driftHeadingDeg: Float,
    val totalDisplacementNM: Float,
    val trajectoryPoints: List<TrajectoryPoint>,
    val slickAreaKm2: Float,
    val slickMajorAxisNM: Float,
    val slickMinorAxisNM: Float,
    val evaporatedPercent: Float,
    val dispersedPercent: Float = 0f,
    val totalWeatheredPercent: Float = evaporatedPercent,
    val emulsifiedPercent: Float,
    val remainingVolumeM3: Float,
    val containmentBoomMeters: Float,
    val beaufortScale: Int,
    val seaStateDescription: String,
    val responseTierRecommendation: String,
    val originCoordinatesFormatted: String = "28°44.2'N  088°21.9'W",
    val currentHeadCoordinatesFormatted: String = "28°52.4'N  088°14.2'W",
    val projectedShorelineDistanceNM: Float = 22.0f,
    val estimatedTimeToImpactHours: Float? = null,
    val uncertaintyConePoints: List<UncertaintyConePoint> = emptyList(),
    val outcomeConfidenceScore: Int = 88,
    val confidenceRatingLabel: String = "HIGH",
    val liveObservation: LiveMarineObservation? = null
)

data class UncertaintyConePoint(
    val hour: Float,
    val dispXNM: Float,
    val dispYNM: Float,
    val crossTrackErrorNM: Float,
    val alongTrackErrorNM: Float,
    val prob50RadiusNM: Float,
    val prob75RadiusNM: Float,
    val prob90RadiusNM: Float
)

data class LiveMarineObservation(
    val stationId: String,
    val stationName: String,
    val latitude: Double,
    val longitude: Double,
    val oceanCurrentSpeedKnots: Float,
    val oceanCurrentDirectionDeg: Float,
    val windSpeedKnots: Float,
    val windDirectionDeg: Float,
    val waveHeightMeters: Float,
    val waterTempCelsius: Float,
    val timestampIso: String,
    val dataSourceName: String = "Open-Meteo Marine Grid",
    val confidenceRatingPercent: Int = 88,
    val confidenceRatingLabel: String = "HIGH"
)
