package com.example.model

enum class OilGroup {
    GROUP_1_VERY_LIGHT,
    GROUP_2_LIGHT,
    GROUP_3_MEDIUM,
    GROUP_4_HEAVY
}

data class OilType(
    val id: String,
    val displayName: String,
    val group: OilGroup,
    val apiGravity: Float,
    val densityKgM3: Float,
    val maxEvaporationPercent: Float,
    val evaporationRateFactor: Float,
    val spreadingRateFactor: Float,
    val description: String
) {
    companion object {
        val DIESEL = OilType(
            id = "diesel",
            displayName = "Marine Diesel / Gas Oil",
            group = OilGroup.GROUP_2_LIGHT,
            apiGravity = 38.0f,
            densityKgM3 = 835f,
            maxEvaporationPercent = 75f,
            evaporationRateFactor = 1.4f,
            spreadingRateFactor = 1.3f,
            description = "High volatility, low persistence. Spreads rapidly into thin rainbow/silver sheens."
        )

        val LIGHT_CRUDE = OilType(
            id = "light_crude",
            displayName = "Light Crude (Brent / WTI)",
            group = OilGroup.GROUP_2_LIGHT,
            apiGravity = 35.0f,
            densityKgM3 = 850f,
            maxEvaporationPercent = 45f,
            evaporationRateFactor = 1.0f,
            spreadingRateFactor = 1.1f,
            description = "Moderate volatility. Loses 30-40% in first 24h. Forms unstable water emulsions."
        )

        val MEDIUM_CRUDE = OilType(
            id = "medium_crude",
            displayName = "Medium Crude (Arabian Light / Kuwait)",
            group = OilGroup.GROUP_3_MEDIUM,
            apiGravity = 28.5f,
            densityKgM3 = 885f,
            maxEvaporationPercent = 30f,
            evaporationRateFactor = 0.75f,
            spreadingRateFactor = 0.95f,
            description = "Typical tanker cargo. Forms viscous 'chocolate mousse' water-in-oil emulsion."
        )

        val HEAVY_CRUDE = OilType(
            id = "heavy_crude",
            displayName = "Heavy Crude (Maya / Western Canadian)",
            group = OilGroup.GROUP_4_HEAVY,
            apiGravity = 21.0f,
            densityKgM3 = 930f,
            maxEvaporationPercent = 18f,
            evaporationRateFactor = 0.45f,
            spreadingRateFactor = 0.7f,
            description = "Highly persistent, dense, slow spreading. Emulsifies into thick tarry patties."
        )

        val BUNKER_C = OilType(
            id = "bunker_c",
            displayName = "Heavy Fuel Oil (IFO 380 / Bunker C)",
            group = OilGroup.GROUP_4_HEAVY,
            apiGravity = 12.0f,
            densityKgM3 = 985f,
            maxEvaporationPercent = 8f,
            evaporationRateFactor = 0.2f,
            spreadingRateFactor = 0.5f,
            description = "Heavy marine bunker fuel. Near-zero evaporation. Very high environmental persistence."
        )

        val ALL = listOf(DIESEL, LIGHT_CRUDE, MEDIUM_CRUDE, HEAVY_CRUDE, BUNKER_C)
    }
}
