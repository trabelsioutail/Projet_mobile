# EduNova Mobile - Application Android Kotlin

## Description
Application mobile Android native développée en Kotlin avec architecture MVVM stricte, basée sur le projet web EduNova. Cette application offre toutes les fonctionnalités de la plateforme éducative avec un mode hybride (hors ligne + API).

## Architecture MVVM
- **Model**: Entités Room + API Models
- **View**: Activities/Fragments avec Data Binding
- **ViewModel**: Gestion d'état avec LiveData/Flow
- **Repository**: Source unique de vérité (API + Room)

## Fonctionnalités
- ✅ Authentification complète (JWT + Google OAuth)
- ✅ Gestion de session (Room + SharedPreferences)
- ✅ CRUD complet pour toutes les entités
- ✅ Mode hors ligne avec synchronisation
- ✅ Messagerie en temps réel
- ✅ Système de quiz interactif
- ✅ Gestion des cours et contenus
- ✅ Notifications push
- ✅ Système de badges (gamification)

## Technologies
- Kotlin 1.9+
- Android SDK 34
- Architecture Components (ViewModel, LiveData, Room)
- Retrofit 2 + OkHttp
- Hilt (Dependency Injection)
- Coroutines + Flow
- Material Design 3
- Navigation Component
- Data Binding
- WorkManager (sync hors ligne)

## Structure du Projet
```
app/
├── data/
│   ├── local/          # Room Database
│   ├── remote/         # Retrofit API
│   └── repository/     # Repositories
├── domain/
│   ├── model/          # Entités métier
│   └── usecase/        # Cas d'usage
├── presentation/
│   ├── ui/             # Activities/Fragments
│   ├── viewmodel/      # ViewModels
│   └── adapter/        # RecyclerView Adapters
└── di/                 # Hilt Modules
```

## Installation
1. Cloner le projet
2. Ouvrir dans Android Studio
3. Configurer l'API URL dans `local.properties`
4. Build et run

## API Backend
Compatible avec l'API EduNova existante (Node.js + MySQL)