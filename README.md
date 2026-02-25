# 🧠 Smart Habit Coach

> AI-powered habit tracking and coaching Android app — Jetpack Compose + Clean Architecture

---

## 📋 Overview

**Smart Habit Coach** is an Android app that helps users build and maintain daily habits. It leverages the OpenAI API to deliver personalized weekly coaching insights and proactively detects habits at risk of being abandoned. Built on Clean Architecture with a modern Android tech stack.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 📝 **Habit Management** | Create, edit, and delete habits |
| ✅ **Daily Check-in** | Mark habits as complete on a per-day basis |
| 📊 **Statistics Dashboard** | Streak tracking, weekly completion rate, today's progress |
| 🤖 **AI Weekly Report** | Personalized behavioral coaching powered by OpenAI GPT |
| ⚠️ **At-Risk Habit Detection** | Sliding-window algorithm to proactively flag habits in danger of being dropped |
| 🔔 **Weekly Notifications** | Automatic weekly AI report via WorkManager push notifications |
| 📱 **Adaptive Layout** | Single-pane on phones / Two-pane on tablets and foldables |

---

## 🏗️ Architecture

```
Clean Architecture (3-Layer)

┌─────────────────────────────────────┐
│         :app (Presentation)          │
│  MainActivity · HabitViewModel       │
│  Jetpack Compose UI · Navigation     │
│  WorkManager (WeeklyInsightWorker)   │
├─────────────────────────────────────┤
│         :domain (Business Logic)     │
│  Habit · HabitStatistics · UseCase   │
│  Repository Interfaces               │
├─────────────────────────────────────┤
│         :data (Infrastructure)       │
│  Room DB · Retrofit (OpenAI)         │
│  Repository Implementations          │
└─────────────────────────────────────┘
```

### Module Structure

```
SmartHabitCoach/
├── app/          # Presentation Layer (UI, ViewModel, DI, Worker)
├── domain/       # Business Logic (UseCases, Domain Models, Repository Interfaces)
└── data/         # Data Layer (Room, Retrofit, Repository Implementations)
```

---

## 🛠️ Tech Stack

### Android & Kotlin
| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.0.21 | Primary language |
| Android Gradle Plugin | 8.9.1 | Build tooling |
| compileSdk / targetSdk | 36 | Android platform |
| minSdk | 24 | Minimum supported version (Android 7.0+) |

### Jetpack Compose
| Library | Version |
|---------|---------|
| Compose BOM | 2026.02.00 |
| Material3 | via BOM |
| Navigation Compose | 2.9.7 |
| Material3 Adaptive (2-pane) | 1.1.0 |
| Activity Compose | 1.12.4 |

### Jetpack Components
| Library | Version | Purpose |
|---------|---------|---------|
| Room | 2.8.4 | Local database |
| Lifecycle / ViewModel | 2.10.0 | MVVM state management |
| WorkManager | 2.10.0 | Background task scheduling |
| Hilt | 2.51.1 | Dependency injection |
| Security Crypto | 1.0.0 | Encrypted API key storage |

### Networking (OpenAI)
| Library | Version |
|---------|---------|
| Retrofit | 2.11.0 |
| OkHttp | 4.12.0 |
| kotlinx-serialization | 1.7.3 |

### Coroutines
| Library | Version |
|---------|---------|
| kotlinx-coroutines | 1.9.0 |

---

## 📁 Project Structure

```
app/src/main/java/com/example/smarthabitcoach/
├── MainActivity.kt                 # App entry point
├── SmartHabitCoachApplication.kt   # Hilt Application class
├── di/
│   └── RepositoryModule.kt         # Hilt DI module
├── navigation/
│   ├── NavHost.kt                  # Navigation graph
│   └── Routes.kt                   # Route definitions
├── habits/
│   ├── HabitViewModel.kt           # Habit screen ViewModel
│   └── ui/
│       ├── HabitScreen.kt          # Main habit screen (Single / Two-pane)
│       ├── HabitListItem.kt        # Habit list item composable
│       ├── StatisticsCard.kt       # Statistics dashboard card
│       ├── CreateHabitDialog.kt    # Create habit dialog
│       ├── EditHabitDialog.kt      # Edit habit dialog
│       └── HabitUiState.kt         # UI state & event definitions
└── worker/
    └── WeeklyInsightWorker.kt      # Weekly AI report Worker

domain/src/main/kotlin/com/example/smarthabitcoach/domain/
├── model/
│   ├── Habit.kt                    # Habit domain model
│   ├── HabitStatistics.kt          # Statistics domain model
│   ├── HabitRiskAssessment.kt      # At-risk habit assessment model
│   └── WeeklyInsight.kt            # Weekly insight model
├── repository/                     # Repository interfaces
└── usecase/
    ├── GetHabitsUseCase.kt
    ├── CreateHabitUseCase.kt
    ├── UpdateHabitUseCase.kt
    ├── DeleteHabitUseCase.kt
    ├── CompleteHabitUseCase.kt
    ├── GetStatisticsUseCase.kt
    ├── GenerateWeeklyInsightUseCase.kt  # AI insight generation (with caching)
    └── DetectAtRiskHabitsUseCase.kt     # At-risk habit detection algorithm

data/src/main/java/com/example/smarthabitcoach/data/
├── local/
│   ├── HabitDatabase.kt            # Room Database
│   ├── HabitEntity.kt / HabitDao.kt
│   ├── WeeklyInsightEntity.kt / WeeklyInsightDao.kt
│   └── HabitTypeConverters.kt
├── ai/
│   ├── OpenAiService.kt            # Retrofit API interface
│   └── OpenAiModels.kt             # Request / Response models
├── mapper/                         # Entity ↔ Domain mappers
├── repository/
│   ├── HabitRepositoryImpl.kt
│   ├── AiRepositoryImpl.kt
│   └── WeeklyInsightCacheRepositoryImpl.kt
└── di/                             # Data layer Hilt modules
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Meerkat or later
- JDK 17
- Android SDK 36

### 1. Clone the repository

```bash
git clone https://github.com/your-username/SmartHabitCoach.git
cd SmartHabitCoach
```

### 2. Set up your OpenAI API key

Add your API key to the `local.properties` file in the project root:

```properties
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

> ⚠️ Make sure `local.properties` is listed in `.gitignore`. Never commit your API key to source control.

### 3. Build and run

```bash
./gradlew assembleDebug
```

Or click the **Run** button directly in Android Studio.

---

## 🤖 AI Features In Depth

### Weekly Insight Generation (`GenerateWeeklyInsightUseCase`)

- Calls the OpenAI Chat Completion API every Monday to generate a personalized weekly coaching report.
- **Cost optimization**: Results are cached in Room DB per week, preventing duplicate API calls.
- **Offline fallback**: If the API call fails, a local algorithm generates a meaningful fallback message.

### At-Risk Habit Detection (`DetectAtRiskHabitsUseCase`)

- **Sliding-window algorithm**: Looks back at the same day-of-week over the past 4 weeks.
- A habit is flagged **at risk** when `missRate ≥ 0.5` (missed 2 or more out of 4 occurrences of the same weekday).
- **Fully deterministic** — no LLM involved, runs instantly at zero API cost.

---

## 🔒 Security

- The OpenAI API key is injected at build time via `BuildConfig.OPENAI_API_KEY`.
- `androidx.security:security-crypto` with `EncryptedSharedPreferences` is available for secure runtime key storage.
- The key is never embedded in source code — it is read exclusively from `local.properties`.

---

## 📱 UI / UX Highlights

- **Material3 Dynamic Color** theming
- **Edge-to-Edge** display support
- **Adaptive layout**: Automatically switches to a Two-Pane layout on screens ≥ 600 dp wide (tablets and foldables)
- **Animations**: Smooth transitions using `AnimatedContent`, `AnimatedVisibility`, and `Animatable`
- **Accessibility**: `semantics` and `liveRegion` support throughout

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires a connected device or emulator)
./gradlew connectedAndroidTest
```

---

## 📄 License

```
MIT License

Copyright (c) 2026 SmartHabitCoach

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction.
```

