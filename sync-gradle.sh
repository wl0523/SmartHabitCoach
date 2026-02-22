#!/bin/bash

# Gradle Project Sync Script
# This script syncs the Android Gradle project with IDE

set -e

echo "🔄 Syncing Gradle Project..."

# Stop Gradle daemon
./gradlew --stop 2>/dev/null || true

# Clean build cache
rm -rf .gradle build app/build

# Refresh dependencies
./gradlew --refresh-dependencies

# Initial build
./gradlew clean assembleDebug -x test

echo "✅ Gradle project sync completed!"
echo "📱 Debug APK built successfully"
echo ""
echo "Next steps:"
echo "1. Open project in Android Studio"
echo "2. Click 'Sync Now' if prompted"
echo "3. Build > Make Project"

