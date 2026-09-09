#!/bin/bash

# ============================================
# Run script for Health Data Management App
# ============================================

set -e

PORT=${PORT:-8083}

# Load environment variables from .env if present
if [ -f ".env" ]; then
    echo "✓ Loading environment variables from .env"
    set -a
    source .env
    set +a
fi

echo "=========================================="
echo "  Health Data Management - Spring Boot"
echo "=========================================="

# Check if port is already in use and free it
EXISTING_PID=$(lsof -ti :$PORT || true)
if [ -n "$EXISTING_PID" ]; then
    echo "⚠️ Port $PORT is already in use by PID(s): $EXISTING_PID"
    echo "▶ Stopping existing process..."
    kill -9 $EXISTING_PID 2>/dev/null || true
    sleep 1
    echo "✓ Port $PORT cleared."
fi

# Use Maven wrapper if available, otherwise fall back to system maven
echo ""
echo "▶ Building and starting the application on port $PORT..."
echo ""

if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    bash ./mvnw spring-boot:run
else
    mvn spring-boot:run
fi
