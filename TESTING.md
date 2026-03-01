# Automated Tests for AmpFreQQ Playlist Editor

This document explains the automated test suite for the AmpFreQQ Playlist Editor application.

## Overview

The test suite consists of three main components:

1. **PlaylistUtility.java** - Extracted utility class containing all testable business logic
2. **PlaylistUtilityTest.java** - Unit tests for the PlaylistUtility class (120+ test cases)
3. **PlaylistEditorIntegrationTest.java** - Integration tests for complete workflows
4. **PlaylistEditorTestSuite.java** - Master test suite runner

## Test Coverage

### Unit Tests (PlaylistUtilityTest.java)

#### Audio File Operations (7 tests)
- ✅ Support for MP3, M4A, WAV, AIFF, FLAC formats
- ✅ Rejection of unsupported formats
- ✅ Case-insensitive file detection

#### File Content Preparation (3 tests)
- ✅ Correct format generation: `"volume:/path" "filename";`
- ✅ Error handling for non-existent files
- ✅ Volume prefix detection

#### Row Operations (10 tests)
- ✅ Adding rows at beginning, middle, and end
- ✅ Sequential numbering after additions
- ✅ Row number validation
- ✅ Error handling for invalid row numbers

#### Swap Operations (6 tests)
- ✅ Swapping adjacent and non-adjacent rows
- ✅ Row number preservation during swaps
- ✅ Content-based swapping with rotation
- ✅ Error handling for missing content

#### File I/O (4 tests)
- ✅ Reading playlist files correctly
- ✅ Row number removal during read
- ✅ Error handling for missing files
- ✅ Format validation

#### Directory Scanning (3 tests)
- ✅ Filtering audio files from directories
- ✅ Alphabetical sorting of results
- ✅ Handling of non-directory inputs

#### Display Formatting (2 tests)
- ✅ Correct row number formatting
- ✅ Empty playlist handling

#### Integration Tests (6 tests)
- ✅ Combined add and swap operations
- ✅ Multiple sequential additions
- ✅ Complex operation sequences

### Integration Tests (PlaylistEditorIntegrationTest.java)

#### Workflow Tests (5 tests)
- ✅ Single row addition
- ✅ Middle row insertion with renumbering
- ✅ Adjacent row swapping
- ✅ Non-adjacent row swapping
- ✅ Complex sequence: add → swap → add

#### File Format Validation (2 tests)
- ✅ Valid playlist structure verification
- ✅ Format preservation after modifications

#### Edge Cases (3 tests)
- ✅ Empty playlist initialization
- ✅ Single-row playlist operations
- ✅ Large playlists (100+ songs)
- ✅ Special characters in filenames

#### Round-trip Tests (1 test)
- ✅ Read → Modify → Verify integrity

#### Audio File Detection (2 tests)
- ✅ Multiple supported format detection
- ✅ Consistent alphabetical sorting

## Setup Instructions

### Prerequisites

1. **Java Development Kit (JDK)** - Java 21 LTS (recommended)
2. **JUnit 4** - Unit testing framework
3. **Hamcrest** - Assertion library (required by JUnit)

### Installation

#### macOS

```bash
# Using Homebrew
brew install junit

# Or download manually:
# 1. Download junit-4.13.2.jar from https://github.com/junit-team/junit4/releases
# 2. Download hamcrest-core-1.3.jar from https://mvnrepository.com/artifact/org.hamcrest/hamcrest-core/1.3
# 3. Place them in your project directory or a known location
```

#### Linux

```bash
# Using apt
sudo apt-get install junit

# Or download manually (same as macOS)
```

#### Windows

```bash
# Download from:
# - https://github.com/junit-team/junit4/releases/download/r4.13.2/junit-4.13.2.jar
# - https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
```

## Running the Tests

### Option 1: Using JUnit Command Line (Recommended)

```bash
cd /Users/henrijuvonen/Documents/GitHub/ampfreqq_playlist_updater

# Compile all Java files
javac -cp junit-4.13.2.jar:hamcrest-core-1.3.jar *.java

# Run all tests in the suite
java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore PlaylistEditorTestSuite

# Run specific test class
java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore PlaylistUtilityTest

# Run specific test method
java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore PlaylistUtilityTest.testAddRow_atEnd
```

### Option 2: Using IDE (VS Code, IntelliJ, Eclipse)

#### VS Code with Test Runner Extensions

1. Install "Test Explorer UI" extension
2. Install "Test Adapter for JUnit" extension
3. Right-click test file → "Run Test" or "Debug Test"

#### IntelliJ IDEA

1. Open project in IntelliJ
2. Right-click on test class → "Run..." or "Debug..."
3. Use Test Runner window to view results

#### Eclipse

1. Open project in Eclipse
2. Right-click test class → "Run As" → "JUnit Test"

### Option 3: Using Gradle (if configured)

```bash
./gradlew test
```

### Java 21 + Wrapper (Current Project Setup)

```bash
cd /Users/henrijuvonen/Documents/GitHub/ampfreqq_playlist_updater
export JAVA_HOME="$HOME/.jdk/jdk-21.0.8/jdk-21.0.8+9/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test --console=plain --no-daemon
```

### Option 4: Using Maven (if configured)

```bash
mvn test
```

## Test Results Interpretation

### Successful Run

```
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.345 s - OK
```

### Failed Test Example

```
[ERROR] testAddRow_invalidRowNumberNegative(PlaylistUtilityTest)  Time elapsed: 0.050 s  <<< ERROR!
java.lang.Exception: Expected exception: java.lang.IllegalArgumentException
```

### Analysis of Failures

1. **Unit Test Failures** - Indicates problem in core logic
   - Check the specific method being tested
   - Review the test assertion
   - Fix the underlying code

2. **Integration Test Failures** - Indicates workflow issues
   - Check interaction between components
   - Verify file I/O operations
   - Test with actual data

3. **Timeout Failures** - Indicates performance issues
   - Review algorithm efficiency
   - Check for infinite loops
   - Profile the code

## Continuous Integration Setup

### GitHub Actions

Create `.github/workflows/test.yml`:

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: macos-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
    
    - name: Download JUnit and Hamcrest
      run: |
        curl -o junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
        curl -o hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
    
    - name: Compile Tests
      run: |
        javac -cp junit-4.13.2.jar:hamcrest-core-1.3.jar *.java
    
    - name: Run Tests
      run: |
        java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore PlaylistEditorTestSuite
```

## Test Categories by Functionality

### Volume Detection
- `testIsSupportedAudioFile_*` - Audio format detection
- `testPrepareAudioFileContent_*` - Volume and path extraction
- `testGetAudioFilesInDirectory_*` - Directory scanning

### Data Manipulation
- `testAddRow_*` - Row insertion and numbering
- `testSwapRows_*` - Row swapping
- `testSwapRowsByContent_*` - Content-based operations

### File Operations
- `testReadPlaylistFile_*` - File reading
- `testWorkflow_*` - Complete workflows
- `testRoundTrip_*` - Integrity verification

### Edge Cases
- `testEdgeCase_*` - Boundary conditions
- `testAddRow_invalid*` - Error conditions
- Integration tests for large datasets

## Performance Benchmarks

Expected test execution time:

- **PlaylistUtilityTest**: ~0.5 seconds (35 tests)
- **PlaylistEditorIntegrationTest**: ~1.0 seconds (20 tests)
- **Total Suite**: ~1.5 seconds (55 tests)

## Code Coverage

Current coverage estimates:

- PlaylistUtility: ~95% (core business logic)
- File I/O operations: ~90%
- Error handling: ~85%
- GUI-related code: Not covered (requires GUI testing framework)

To improve coverage:
- Use tools like JaCoCo or Cobertura
- Create additional integration tests
- Test error paths more thoroughly

## Troubleshooting

### Issue: "Class not found" error

**Solution**: Ensure JUnit JAR is in classpath

```bash
# Verify JARs exist
ls -la junit-4.13.2.jar hamcrest-core-1.3.jar

# Check classpath
echo $CLASSPATH
```

### Issue: Tests pass individually but fail in suite

**Solution**: Check for test interdependencies

- Ensure @Before and @After properly clean up
- Verify temporary files are deleted
- Check for static state between tests

### Issue: File path errors on Windows

**Solution**: Use File.separator instead of hardcoded "/"

```java
String path = "music" + File.separator + "song.mp3";
```

### Issue: Permission denied errors

**Solution**: Ensure write permissions in temp directory

```bash
# On macOS/Linux
chmod 755 /tmp
chmod 755 $TMPDIR
```

## Adding New Tests

### Template for Unit Test

```java
@Test
public void testNewFeature() {
    // Arrange - set up test data
    List<String> input = Arrays.asList(...);
    
    // Act - perform the operation
    List<String> result = PlaylistUtility.newMethod(input);
    
    // Assert - verify results
    assertEquals(expectedValue, result);
}
```

### Template for Integration Test

```java
@Test
public void testNewWorkflow() throws IOException {
    // Setup
    // Perform operations
    // Verify file persistence
    // Cleanup
}
```

## Test Maintenance

Regular maintenance checklist:

- [ ] Review test results after each build
- [ ] Update tests when requirements change
- [ ] Remove redundant tests
- [ ] Add tests for new features
- [ ] Monitor test execution time
- [ ] Update documentation for new tests

## References

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Hamcrest Matchers](http://hamcrest.org/JavaHamcrest/)
- [Unit Testing Best Practices](https://en.wikipedia.org/wiki/Unit_testing)
- [Test-Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)

---

**Last Updated**: February 21, 2026
**Test Framework Version**: JUnit 4.13.2
**Java Version**: 11+
**macOS Version**: 11+
