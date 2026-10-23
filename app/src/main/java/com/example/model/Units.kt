package com.example.model

enum class UnitSystem(val label: String, val speedUnit: String, val distanceUnit: String, val areaUnit: String, val volumeUnit: String) {
    NAUTICAL("Nautical (kt / NM)", "knots", "NM", "km²", "m³"),
    METRIC("Metric (m/s / km)", "m/s", "km", "km²", "m³"),
    IMPERIAL("Imperial (mph / mi)", "mph", "mi", "sq mi", "bbl");

    fun formatSpeed(knots: Float): String {
        return when (this) {
            NAUTICAL -> String.format("%.1f kt", knots)
            METRIC -> String.format("%.1f m/s", knots * 0.514444f)
            IMPERIAL -> String.format("%.1f mph", knots * 1.15078f)
        }
    }

    fun formatDistance(nauticalMiles: Float): String {
        return when (this) {
            NAUTICAL -> String.format("%.1f NM", nauticalMiles)
            METRIC -> String.format("%.1f km", nauticalMiles * 1.852f)
            IMPERIAL -> String.format("%.1f mi", nauticalMiles * 1.15078f)
        }
    }

    fun formatArea(areaKm2: Float): String {
        return when (this) {
            NAUTICAL, METRIC -> String.format("%.2f km²", areaKm2)
            IMPERIAL -> String.format("%.2f sq mi", areaKm2 * 0.386102f)
        }
    }

    fun formatVolume(m3: Float): String {
        return when (this) {
            NAUTICAL, METRIC -> String.format("%.0f m³", m3)
            IMPERIAL -> String.format("%.0f bbl", m3 * 6.28981f)
        }
    }
}

enum class WindDirectionConvention(val label: String, val helperText: String) {
    METEOROLOGICAL_FROM("Blowing From (Met)", "Standard weather report: direction the wind is coming from"),
    OCEANOGRAPHIC_TOWARDS("Blowing Towards (Ocean)", "Navigation vector: direction the wind is pushing towards")
}
