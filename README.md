# OpenHealth

A beautiful, modern health dashboard app for Android that reads health data from Health Connect. Built with Jetpack Compose and Material 3.

![OpenHealth Dashboard](screenshots/dashboard.png)

## Features

### Health Metrics
- **Steps** - Daily step count with progress toward goal
- **Distance** - Total distance traveled today
- **Floors Climbed** - Number of floors climbed with goal tracking
- **Heart Rate** - Current and resting heart rate
- **Resting Heart Rate** - Latest resting heart rate measurement
- **Sleep** - Last night's sleep duration
- **Exercise** - Today's exercise sessions with duration
- **VO2 Max** - Latest VO2 Max measurement
- **Calories** - Total and active calories burned
- **Weight** - Latest weight measurement
- **Body Fat** - Latest body fat percentage

### Dashboard
- Beautiful dark theme with modern Material 3 design
- Organized by categories: Activity, Calories, Heart, Body, Sleep, Exercise
- Click any metric card to view detailed 30-day history
- Real-time data refresh with pull-to-refresh and FAB
- Progress indicators for goals (Steps, Floors)

### Detail Screens
- Today's value prominently displayed
- Monthly average statistics
- Best day tracking
- 30-day history list with daily values
- Color-coded by metric type

## Architecture

### Tech Stack
- **Jetpack Compose** - Modern declarative UI
- **Material 3** - Latest Material Design components
- **Health Connect** - Android health data platform
- **Kotlin Coroutines** - Asynchronous operations
- **StateFlow** - Reactive state management
- **MVVM Pattern** - Clean architecture

### Project Structure
```
app/src/main/java/com/openhealth/openhealth/
├── MainActivity.kt              # Main entry point with navigation
├── model/
│   └── HealthData.kt            # Data models for all health metrics
├── screens/
│   ├── DashboardScreen.kt       # Main dashboard with metric cards
│   └── MetricDetailScreen.kt    # Detail view with 30-day history
├── ui/theme/
│   ├── Color.kt                 # Color palette
│   ├── Theme.kt                 # Material 3 theme
│   └── Type.kt                  # Typography
├── utils/
│   └── HealthConnectManager.kt  # Health Connect integration
└── viewmodel/
    └── HealthViewModel.kt       # Business logic and state management
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34 (Android 14)
- Health Connect app installed on device

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/openhealth.git
cd openhealth
```

2. Open in Android Studio

3. Sync project with Gradle files

4. Run on a device or emulator with Health Connect installed

### Health Connect Setup

1. Install Health Connect from Play Store
2. Open Health Connect app and grant permissions
3. Connect your health data sources (Google Fit, Samsung Health, etc.)
4. Launch OpenHealth and grant permissions when prompted

## Permissions

OpenHealth requires the following Health Connect permissions:
- `android.permission.health.READ_STEPS`
- `android.permission.health.READ_HEART_RATE`
- `android.permission.health.READ_SLEEP`
- `android.permission.health.READ_EXERCISE`
- `android.permission.health.READ_VO2_MAX`
- `android.permission.health.READ_DISTANCE`
- `android.permission.health.READ_TOTAL_CALORIES_BURNED`
- `android.permission.health.READ_ACTIVE_CALORIES_BURNED`
- `android.permission.health.READ_FLOORS_CLIMBED`
- `android.permission.health.READ_BODY_FAT`
- `android.permission.health.READ_WEIGHT`
- `android.permission.health.READ_RESTING_HEART_RATE`

## Design

### Color Palette
- **Background**: Deep navy (#0D1B2A)
- **Surface**: Dark blue-gray (#1B263B)
- **Primary**: Bright cyan (#4FC3F7)
- **Accent Colors**:
  - Steps: Blue (#4FC3F7)
  - Heart Rate: Red (#FF6B6B)
  - Sleep: Purple (#9C27B0)
  - Exercise: Green (#4CAF50)
  - VO2 Max: Orange (#FF9800)
  - Calories: Deep Orange (#FF5722)
  - Distance: Cyan (#00BCD4)
  - Floors: Light Green (#8BC34A)
  - Body Fat: Pink (#E91E63)
  - Weight: Indigo (#3F51B5)

### Typography
- Uses Material 3 typography scale
- Large display values for metrics
- Clear hierarchy with weights and sizes

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by Garmin Connect's clean, modern design
- Built with Android Health Connect APIs
- Material 3 design system

## Support

For issues and feature requests, please use the GitHub issue tracker.

---

**OpenHealth** - Your health data, beautifully displayed.
