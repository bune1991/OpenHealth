package com.openhealth.openhealth.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
// OpenHealth v2.0 — "Electric Nocturne" Design System
// ═══════════════════════════════════════════════════════════

// Surface Hierarchy (nested tonal layers)
val SurfaceLowest = Color(0xFF000000)        // Base canvas — pure OLED black
val SurfaceLow = Color(0xFF131313)           // Large structural sections
val SurfaceMid = Color(0xFF191919)           // Floating cards
val SurfaceHigh = Color(0xFF1F1F1F)          // Active/interactive cards
val SurfaceHighest = Color(0xFF262626)       // Overlays, focused inputs
val SurfaceBright = Color(0xFF333333)        // Hover/pressed states

// Primary — Electric Indigo
val ElectricIndigo = Color(0xFFB89FFF)       // Primary accent
val ElectricIndigoDim = Color(0xFF9B7AE6)    // Dimmed primary for subtle use
val IndigoContainer = Color(0xFF7F3AFC)      // Primary container / gradient end
val OnIndigo = Color(0xFF2A0066)             // Text on primary

// Secondary — Vibrant Magenta
val VibrantMagenta = Color(0xFFFF51FA)       // Secondary accent — "spark" moments
val MagentaContainer = Color(0xFFA900A9)     // Secondary container
val OnMagenta = Color(0xFF3D0039)            // Text on secondary

// Tertiary — Soft Lavender
val SoftLavender = Color(0xFFAE89FF)         // Tertiary/supportive accent

// Text Colors (no pure white for body — reduce eye strain)
val TextOnSurface = Color(0xFFFFFFFF)        // High emphasis (headlines, hero numbers)
val TextOnSurfaceVariant = Color(0xFFABABAB) // Medium emphasis (body, descriptions)
val TextSubtle = Color(0xFF737373)           // Low emphasis (labels, timestamps)

// Metric Accent Colors
val CardSteps = Color(0xFFB89FFF)            // Indigo — primary metric
val CardHeartRate = Color(0xFFFF6B8A)        // Pink-red
val CardSleep = Color(0xFFAE89FF)            // Lavender
val CardCalories = Color(0xFFFF9F43)         // Warm orange
val CardExercise = Color(0xFF5BF5A0)         // Mint green
val CardDistance = Color(0xFF64D2FF)          // Sky blue
val CardFloors = Color(0xFF5BF5A0)           // Mint green
val CardVo2Max = Color(0xFFFF9F43)           // Warm orange

// Body Composition
val CardBodyFat = Color(0xFFFF6B8A)          // Pink-red
val CardWeight = Color(0xFFAE89FF)           // Lavender
val CardBMR = Color(0xFFFF9F43)              // Warm orange
val CardBodyWater = Color(0xFF64D2FF)        // Sky blue
val CardBoneMass = Color(0xFF737373)         // Subtle grey
val CardLeanBodyMass = Color(0xFF5BF5A0)     // Mint green

// Vitals
val CardBloodGlucose = Color(0xFFFF6B8A)     // Pink-red
val CardBloodPressure = Color(0xFFAE89FF)    // Lavender
val CardBodyTemperature = Color(0xFFFF9F43)  // Warm orange
val CardHRV = Color(0xFFB89FFF)              // Electric indigo
val CardSpO2 = Color(0xFF64D2FF)             // Sky blue
val CardRespiratoryRate = Color(0xFF5BF5A0)  // Mint green
val CardSkinTemperature = Color(0xFFFF9F43)  // Warm orange
val CardNutrition = Color(0xFF5BF5A0)        // Mint green

// Readiness Score Colors
val ReadinessExcellent = Color(0xFF5BF5A0)   // Mint green
val ReadinessGood = Color(0xFF5BF5A0)        // Mint green
val ReadinessFair = Color(0xFFFF9F43)        // Warm orange
val ReadinessPoor = Color(0xFFFF5252)        // Bright red

// Status Colors
val SuccessGreen = Color(0xFF5BF5A0)
val WarningOrange = Color(0xFFFF9F43)
val ErrorRed = Color(0xFFFF5252)

// Chart Colors
val ChartLineSteps = ElectricIndigo
val ChartLineHeartRate = CardHeartRate
val ChartLineSleep = SoftLavender
val ChartLineCalories = CardCalories
val ChartGridLine = Color(0xFF262626)
val ChartFillSteps = Color(0x20B89FFF)
val ChartFillHeartRate = Color(0x20FF6B8A)
val ChartFillSleep = Color(0x20AE89FF)
val ChartFillCalories = Color(0x20FF9F43)

// Ghost Border (accessibility fallback)
val GhostBorder = Color(0x26484848)          // outline_variant at 15% opacity

// Legacy aliases (keep existing code working during migration)
val BackgroundBlack = SurfaceLowest
val SurfaceDark = SurfaceMid
val SurfaceVariant = SurfaceHigh
val CardBackground = SurfaceMid
val PrimaryBlue = ElectricIndigo
val AccentCyan = ElectricIndigo
val AccentTeal = ElectricIndigo
val TextPrimary = TextOnSurface
val TextSecondary = TextOnSurfaceVariant
val TextTertiary = TextSubtle
val FabColor = SurfaceHigh

// Light Theme (kept for toggle support, refined later)
val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE8E8E8)
val LightCardBackground = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF1A1A1A)
val LightTextSecondary = Color(0xFF666666)
val LightTextTertiary = Color(0xFF999999)
val LightFabColor = Color(0xFFE0E0E0)

// Legacy card aliases
val StepsCard = SurfaceMid
val HeartRateCard = SurfaceMid
val SleepCard = SurfaceMid
val ExerciseCard = SurfaceMid
val Vo2MaxCard = SurfaceMid
val StepsAccent = CardSteps
val HeartRateAccent = CardHeartRate
val SleepAccent = CardSleep
val ExerciseAccent = CardExercise
val Vo2MaxAccent = CardVo2Max
val CaloriesAccent = CardCalories
val DistanceAccent = CardDistance
val GradientStart = SurfaceLowest
val GradientEnd = SurfaceMid
