import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite that runs all unit and integration tests for the Playlist Editor.
 * 
 * To run: java -cp .:/path/to/junit-4.13.jar:/path/to/hamcrest-core-1.3.jar org.junit.runner.JUnitCore PlaylistEditorTestSuite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PlaylistUtilityTest.class,
    PlaylistEditorIntegrationTest.class
})
public class PlaylistEditorTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}
