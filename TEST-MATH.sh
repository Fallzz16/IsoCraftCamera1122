#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p build/math
javac -source 8 -target 8 -d build/math \
 src/main/java/dev/isocraft/camera/IsometricMath.java \
 src/main/java/dev/isocraft/camera/LeafCircle.java \
 src/main/java/dev/isocraft/camera/ScreenRayMath.java \
 src/main/java/dev/isocraft/camera/CameraBehavior.java \
 tests/IsometricMathTest.java tests/LeafCircleTest.java tests/ScreenRayMathTest.java tests/CameraBehaviorTest.java
java -cp build/math IsometricMathTest
java -cp build/math LeafCircleTest
java -cp build/math ScreenRayMathTest
java -cp build/math CameraBehaviorTest
