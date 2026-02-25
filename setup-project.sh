#!/bin/bash

# Android Studio Gradle Project Setup
# Run this to fully link the Gradle project with Android Studio

echo "════════════════════════════════════════════════════════"
echo "  Smart Habit Coach - Gradle Project Setup"
echo "════════════════════════════════════════════════════════"
echo ""

# Check prerequisites
echo "✓ Checking prerequisites..."
if ! command -v java &> /dev/null; then
    echo "✗ Java not found. Please install JDK 17+"
    exit 1
fi
if [ ! -f "./gradlew" ]; then
    echo "✗ Gradle wrapper not found"
    exit 1
fi
echo "✓ All prerequisites met"
echo ""

# Clean up
echo "🧹 Cleaning project..."
./gradlew --stop 2>/dev/null || true
rm -rf .gradle build app/build .idea/caches
echo "✓ Clean complete"
echo ""

# Sync dependencies
echo "📦 Syncing dependencies..."
./gradlew --refresh-dependencies > /dev/null 2>&1
echo "✓ Dependencies synced"
echo ""

# Build
echo "🏗️  Building project..."
./gradlew clean assembleDebug -x test
BUILD_RESULT=$?
echo ""

if [ $BUILD_RESULT -eq 0 ]; then
    echo "════════════════════════════════════════════════════════"
    echo "  ✅ BUILD SUCCESSFUL"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "📱 Debug APK built:"
    ls -lh app/build/outputs/apk/debug/app-debug.apk 2>/dev/null || echo "   APK path: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📋 Next steps:"
    echo "   1. Open project in Android Studio"
    echo "   2. File → Sync Now (if needed)"
    echo "   3. Run → Run 'app' to launch on emulator"
    echo ""
else
    echo "════════════════════════════════════════════════════════"
    echo "  ✗ BUILD FAILED"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "🔧 Troubleshooting:"
    echo "   1. Check Java version: java -version"
    echo "   2. Clear gradle cache: rm -rf ~/.gradle"
    echo "   3. Rebuild: ./gradlew clean build"
    echo ""
    exit 1
fi

echo ""

