#!/bin/bash

# Android Studio Gradle Project Link Fix
# This script fixes "Code insight unavailable (related gradle project not linked)" error

echo "════════════════════════════════════════════════════════"
echo "  Fixing Android Studio Gradle Link"
echo "════════════════════════════════════════════════════════"
echo ""

# Step 1: Clean IDE cache
echo "🧹 Step 1: Cleaning IDE cache..."
rm -rf .gradle
rm -rf .idea/caches
rm -rf .idea/gradle.xml
rm -rf build
rm -rf app/build
echo "✓ Cache cleaned"
echo ""

# Step 2: Recreate IDE configuration
echo "🔧 Step 2: Creating IDE configuration..."
mkdir -p .idea

cat > .idea/gradle.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="GradleProjectSettings">
    <option name="testRunner" value="GRADLE" />
    <option name="distributionType" value="DEFAULT_WRAPPED" />
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="modules">
      <set>
        <option value="$PROJECT_DIR$" />
        <option value="$PROJECT_DIR$/app" />
      </set>
    </option>
  </component>
</project>
EOF

cat > .idea/misc.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ProjectRootManager" version="2" languageLevel="JDK_17" default="true" project-jdk-name="17" project-jdk-type="JavaSDK">
    <output url="file://$PROJECT_DIR$/out" />
  </component>
</project>
EOF

echo "✓ IDE configuration created"
echo ""

# Step 3: Sync Gradle
echo "📦 Step 3: Syncing Gradle..."
./gradlew --stop 2>/dev/null || true
./gradlew clean build -x test

if [ $? -eq 0 ]; then
    echo "✓ Gradle sync successful"
else
    echo "✗ Gradle sync failed"
    exit 1
fi
echo ""

# Step 4: Instructions
echo "════════════════════════════════════════════════════════"
echo "  ✅ GRADLE LINK FIX COMPLETE"
echo "════════════════════════════════════════════════════════"
echo ""
echo "📋 Next steps:"
echo ""
echo "1️⃣  Close Android Studio completely"
echo ""
echo "2️⃣  Reopen the project in Android Studio"
echo ""
echo "3️⃣  Wait for Gradle sync to complete"
echo ""
echo "4️⃣  The error should be gone!"
echo ""
echo "If the error persists:"
echo "  • File → Invalidate Caches → Invalidate and Restart"
echo "  • Or: File → Sync Project with Gradle Files"
echo ""

