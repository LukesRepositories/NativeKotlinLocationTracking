#!/bin/bash

PACKAGE_NAME="com.example.nativekotlinlocationtracking"  # Replace with your package name
ACTIVITY_NAME=".MainActivity"        # Replace with your main activity
NUM_TESTS=5                          # Number of test runs

echo "Testing cold boot startup times for $PACKAGE_NAME"
echo "=========================================="

total_time=0

for i in $(seq 1 $NUM_TESTS); do
    echo "Test run $i of $NUM_TESTS..."
    
    # Force stop the app to ensure cold boot
    adb shell am force-stop $PACKAGE_NAME
    
    # Clear app cache
    adb shell pm clear $PACKAGE_NAME
    
    # Wait a moment for system to settle
    sleep 2
    
    # Start app and capture startup time
    result=$(adb shell am start -W -n $PACKAGE_NAME/$ACTIVITY_NAME 2>&1)
    
    # Extract TotalTime (time until first frame drawn)
    startup_time=$(echo "$result" | grep "TotalTime" | awk '{print $2}')
    
    echo "  Startup time: ${startup_time}ms"
    total_time=$((total_time + startup_time))
    
    sleep 3
done

average=$((total_time / NUM_TESTS))
echo "=========================================="
echo "Average cold boot time: ${average}ms"