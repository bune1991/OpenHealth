# Privacy Policy for OpenHealth

**Last updated: March 27, 2026**

## Overview

OpenHealth is an open-source health dashboard app that reads health data from Android Health Connect. Your privacy is our top priority.

## Data Collection

**OpenHealth does NOT collect, store, or transmit any personal data to external servers.**

All health data is:
- Read locally from Health Connect on your device
- Displayed only on your device
- Never sent to our servers (we don't have any)
- Never shared with third parties

## Health Connect Data

OpenHealth reads the following data types from Health Connect:
- Steps, distance, floors climbed
- Heart rate, resting heart rate, HRV
- Sleep sessions and stages
- Calories burned
- Body composition (weight, body fat, BMR, etc.)
- Blood oxygen (SpO2), respiratory rate
- Blood pressure, blood glucose, body temperature
- Skin temperature
- Exercise sessions
- Nutrition data

This data is read-only. OpenHealth never writes or modifies your Health Connect data.

## AI Health Insights (Optional)

If you choose to use the AI Insights feature:
- You provide your own API key (Bring Your Own Key)
- Your health data summary is sent directly to YOUR chosen AI provider (Claude, Gemini, ChatGPT, or custom)
- We never see, store, or have access to your API key or the data sent
- This feature is entirely optional and disabled by default

## Weather (Optional)

If you enable the Weather Health Advisory:
- Your manually entered coordinates are sent to Open-Meteo (open-source weather API)
- No GPS or location permissions are used
- Open-Meteo does not require authentication or track users

## Data Storage

All app settings (goals, preferences, API keys) are stored locally on your device in SharedPreferences. No cloud sync, no accounts, no registration.

## Third-Party Services

OpenHealth does not include any:
- Analytics or tracking SDKs
- Advertising SDKs
- Crash reporting services
- User authentication services

## Open Source

OpenHealth is fully open source under the GPL-3.0 license. You can review the entire source code at:
https://github.com/bune1991/OpenHealth

## Children's Privacy

OpenHealth is not directed at children under 13. We do not knowingly collect any information from children.

## Changes to This Policy

We may update this privacy policy from time to time. Changes will be reflected in the "Last updated" date above.

## Contact

For questions about this privacy policy, please open an issue on GitHub:
https://github.com/bune1991/OpenHealth/issues

---

**In summary: Your data stays on your device. We don't collect anything. Period.**
