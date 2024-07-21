#!/bin/bash

# Set the path to the RuneScape client
RUNESCAPE_CLIENT_PATH="/home/dylan/Jagex/launcher/rs2client"

# Set the working directory to the OpenNXT directory
cd "$(dirname "$0")"

# Build the OpenNXT project using Gradle
./gradlew build

# Run the OpenNXT client using the RuneScape client
./gradlew runClient --args="$RUNESCAPE_CLIENT_PATH"

# End of script
