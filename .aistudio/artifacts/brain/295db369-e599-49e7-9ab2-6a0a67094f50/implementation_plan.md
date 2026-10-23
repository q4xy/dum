# Real-World Marine Feeds & Outcome Confidence Plan

SlickDrift is upgrading from synthetic simulation to a live oceanographic tactical intelligence system. By integrating live Open-Meteo Marine and NOAA ocean buoy telemetry, GPS coordinate fetching, and a rigorous hydrodynamic Cone of Uncertainty, the platform empowers maritime responders to forecast oil spill trajectories with calculated confidence intervals.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural parameters were selected during interactive clarification:
> - **Live Ocean & Weather Data**: Open-Meteo Marine API (ocean currents, wave height, swell period) and Open-Meteo Weather API (10m wind speed, gust, atmospheric pressure) with instant GPS location lookup and live NOAA buoy feed integration.
> - **Prediction Certainty & Outcome Confidence**: Tactical Cone of Uncertainty with confidence ellipses (50%, 75%, 90% probability zones) expanding over drift time, alongside an overall Outcome Confidence Rating index (0–100%) computed from data freshness, sea state turbulence, and velocity variance.
> - **Operational Maritime Basins**: Instant one-tap operational station feeds for high-risk offshore extraction and shipping corridors: Gulf of Mexico (Mississippi Canyon), North Sea (Brent Field), Persian Gulf (Strait of Hormuz), and Southeast Asia (Malacca Strait).

- **Confirmed Decision 1**: Real-world REST networking will be implemented with Retrofit/OkHttp and Kotlin Coroutines, providing asynchronous live data fetching with automatic fallback to high-resolution regional telemetry if offline.
- **Confirmed Decision 2**: Radar canvas will dynamically render the probabilistic Cone of Uncertainty as an expanding translucent envelope around the trajectory vector with 50%/75%/90% dispersion contours.

---

## 1. Overview & Core Concept

### What It Delivers
Real-world spill response requires operators to know not just *where* the slick might go, but *how confident* the prediction is when compared to actual sea truth. SlickDrift will provide:
1. **Live Marine Data Integration**: Fetch real-time surface ocean current velocity, current direction, 10m wind vector, and significant wave height directly from satellite and oceanographic reanalysis models (Open-Meteo Marine) and coastal NOAA marine stations.
2. **One-Tap High-Risk Maritime Presets**: Pre-mapped coordinates for notorious offshore zones (Gulf of Mexico, North Sea, Persian Gulf, Malacca Strait) with live environmental data at the touch of a button.
3. **GPS Coordinate Auto-Fetch**: Ability to tap a GPS target button to fetch live marine current and wind vectors for the user's current offshore location or any input coordinates.
4. **Trajectory Cone of Uncertainty**: Nautical radar visualization showing the spreading probability cone (expanding standard error based on wind shear and current variance) with time-stamped probability ellipses.
5. **Outcome Confidence Rating (OCR)**: A composite confidence index (e.g. `87% HIGH CONFIDENCE - Ground Truth Aligned`) calculating model certainty from live sensor freshness, wind stability index, and tidal shear.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Live Feed Acquisition Flow**:
   - The user taps the **Live Marine Data** pill in the top action bar or inside the control panel.
   - A bottom drawer or modal offers:
     - **GPS Auto-Detect**: Acquires current device latitude/longitude and calls Open-Meteo Marine.
     - **High-Risk Basin Feeds**: One-tap quick selector for *Gulf of Mexico (Station 42001)*, *North Sea (Ekofisk/Brent)*, *Strait of Hormuz*, and *Singapore/Malacca*.
     - **Custom Marine Coordinate Query**: Enter any offshore lat/lon to query live ocean currents and wind.
   - A pulsing sync indicator (`SYNCING SATELLITE & BUOY FEEDS...`) displays telemetry status, then populates live current speed, current direction, wind speed, wind direction, and wave height.

2. **Outcome Confidence & Radar Display Flow**:
   - On the nautical radar, the trajectory path now renders with an elegant, glowing **Cone of Uncertainty**:
     - Inner core: Most probable mean trajectory line (Amber).
     - 50% Confidence Envelope: Warm golden translucent corridor.
     - 90% Maximum Dispersion Boundary: Feathered cyan/amber dashed boundary with major/minor error axes.
   - An **Outcome Confidence Badge** floats on the radar (e.g., `CONFIDENCE: 91% (HIGH) • LIVE NOAA/METEO FEED`).

3. **Telemetry Diagnostics & Ground-Truth Comparison**:
   - The Tactical Drift Telemetry card displays:
     - Outcome Certainty Score & Uncertainty Radius (e.g., `±1.8 NM at T+24h`).
     - Data Source tag (`LIVE SATELLITE / OPEN-METEO BUOY GRID` vs `MANUAL OVERRIDE`).
     - Real-time timestamp of the fetched marine observation.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Open-Meteo Marine API vs Commercial Weather APIs**
  - *Chosen Approach*: Open-Meteo Marine + Open-Meteo Weather APIs.
  - *Why*: Open-access, zero API key barrier, comprehensive coverage of ocean currents (`ocean_current_velocity`, `ocean_current_direction`) and waves (`wave_height`), combined with global wind reanalysis.
  - *Fallback*: Embedded real-world historical telemetry benchmarks for the 4 primary high-risk basins so the application operates seamlessly even in airplane mode or disconnected vessels.

- **Decision 2: Cone of Uncertainty Mathematical Formulation**
  - *Chosen Approach*: Empirical Gaussian drift expansion where cross-track error $\sigma_{cross}(t) = 0.08 \cdot V_{drift} \cdot t + 0.1 \cdot H_{wave} \cdot \sqrt{t}$ and along-track error $\sigma_{along}(t) = 0.05 \cdot V_{drift} \cdot t$.
  - *Why*: Accurately mirrors NOAA GNOME (General NOAA Operational Modeling Environment) and Coast Guard SAROPS search-and-rescue probability distributions.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      SlickDrift UI                     │
│  ┌──────────────────┐  ┌─────────────────────────────┐ │
│  │ OceanRadarCanvas │  │ LiveMarineStationSelector   │ │
│  │ (Cone of Uncert.)│  │ (Gulf, North Sea, GPS)      │ │
│  └────────▲─────────┘  └──────────────▲──────────────┘ │
│           │                           │                │
│  ┌────────┴───────────────────────────┴──────────────┐ │
│  │                  OilDriftViewModel                │ │
│  │  - fetchLiveMarineData(lat, lon)                  │ │
│  │  - calculateConfidenceScore(windVar, waveHeight)  │ │
│  └────────────────────────▲──────────────────────────┘ │
│                           │                            │
│  ┌────────────────────────┴──────────────────────────┐ │
│  │             MarineTelemetryRepository             │ │
│  │  - OpenMeteoMarineService (Retrofit)              │ │
│  │  - HighRiskBasinDataSource (Real-World Presets)   │ │
│  └───────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

### Key Entities & Models
- `LiveMarineObservation`: Timestamp, source, latitude, longitude, oceanCurrentSpeedKnots, oceanCurrentDirectionDeg, windSpeedKnots, windDirectionDeg, waveHeightMeters, waterTemperatureC, confidenceIndex.
- `UncertaintyConePoint`: Hour, meanLat, meanLon, semiMajorNM, semiMinorNM, headingDeg, probability50RadiusNM, probability90RadiusNM.
- `MarineStation`: Id, name, basinName, coordinates, noaaStationId, description.

---

## 5. Verification Plan

### Automated Checks
- Unit tests in `ExampleUnitTest.kt` verifying:
  - Open-Meteo JSON response parsing into `LiveMarineObservation`.
  - Cone of Uncertainty mathematical boundary expansion over time.
  - Outcome confidence scoring algorithm.
- Robolectric tests in `ExampleRobolectricTest.kt` verifying:
  - ViewModel live feed state transitions (Loading -> Success -> Trajectory updated).
  - High-risk basin selection updates both environmental inputs and radar telemetry.
- Build check: `compile_applet` must succeed with 0 errors.
