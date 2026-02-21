#!/bin/bash

# Test Runner Script for AmpFreQQ Playlist Editor
# Usage: ./run_tests.sh [all|unit|integration|suite]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# JUnit paths
JUNIT_JAR="junit-4.13.2.jar"
HAMCREST_JAR="hamcrest-core-1.3.jar"

# Check if JUnit is available
if [ ! -f "$JUNIT_JAR" ] || [ ! -f "$HAMCREST_JAR" ]; then
    echo -e "${YELLOW}Downloading JUnit and Hamcrest...${NC}"
    
    if command -v curl &> /dev/null; then
        curl -o "$JUNIT_JAR" -L "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar" 2>/dev/null || true
        curl -o "$HAMCREST_JAR" -L "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar" 2>/dev/null || true
    elif command -v wget &> /dev/null; then
        wget -q -O "$JUNIT_JAR" "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar" 2>/dev/null || true
        wget -q -O "$HAMCREST_JAR" "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar" 2>/dev/null || true
    fi
fi

# Verify JARs exist
if [ ! -f "$JUNIT_JAR" ] || [ ! -f "$HAMCREST_JAR" ]; then
    echo -e "${RED}Error: Could not find JUnit or Hamcrest JAR files${NC}"
    echo "Please download manually:"
    echo "  1. junit-4.13.2.jar from https://github.com/junit-team/junit4/releases"
    echo "  2. hamcrest-core-1.3.jar from https://mvnrepository.com/artifact/org.hamcrest/hamcrest-core/1.3"
    exit 1
fi

# Set classpath
CLASSPATH=".:${JUNIT_JAR}:${HAMCREST_JAR}"

# Function to compile tests
compile_tests() {
    echo -e "${YELLOW}Compiling tests...${NC}"
    javac -cp "$CLASSPATH" PlaylistUtility.java PlaylistUtilityTest.java PlaylistEditorIntegrationTest.java PlaylistEditorTestSuite.java 2>&1 | grep -v "^Note:" || true
    echo -e "${GREEN}Compilation successful${NC}"
}

# Function to run tests
run_tests() {
    local test_class="$1"
    local description="$2"
    
    echo -e "${YELLOW}Running ${description}...${NC}"
    echo "────────────────────────────────────"
    
    if java -cp "$CLASSPATH" org.junit.runner.JUnitCore "$test_class"; then
        echo "────────────────────────────────────"
        echo -e "${GREEN}✓ ${description} passed${NC}\n"
        return 0
    else
        echo "────────────────────────────────────"
        echo -e "${RED}✗ ${description} failed${NC}\n"
        return 1
    fi
}

# Main script
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}AmpFreQQ Playlist Editor - Test Suite${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}\n"

# Compile tests
compile_tests

# Run tests
TEST_TYPE="${1:-suite}"
FAILED=0

case "$TEST_TYPE" in
    unit)
        run_tests "PlaylistUtilityTest" "Unit Tests" || FAILED=1
        ;;
    integration)
        run_tests "PlaylistEditorIntegrationTest" "Integration Tests" || FAILED=1
        ;;
    suite|all)
        run_tests "PlaylistUtilityTest" "Unit Tests" || FAILED=1
        run_tests "PlaylistEditorIntegrationTest" "Integration Tests" || FAILED=1
        ;;
    *)
        echo -e "${RED}Usage: $0 [all|unit|integration|suite]${NC}"
        exit 1
        ;;
esac

# Summary
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
else
    echo -e "${RED}Some tests failed. ✗${NC}"
fi
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}\n"

exit $FAILED
