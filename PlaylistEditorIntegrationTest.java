import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Integration tests for the playlist editor workflow.
 * These tests verify complete scenarios from file creation to manipulation and saving.
 */
public class PlaylistEditorIntegrationTest {

    private Path tempDir;
    private File testPlaylistFile;
    private File testMusicDir;

    @Before
    public void setUp() throws IOException {
        // Create temporary directories
        tempDir = Files.createTempDirectory("playlist_integration_test_");
        testMusicDir = new File(tempDir.toFile(), "music");
        testMusicDir.mkdir();

        // Create a test playlist with 3 songs
        testPlaylistFile = new File(tempDir.toFile(), "test_playlist.txt");
        String playlistContent = "0, \"Macintosh HD:/Users/test/Music\" \"Song 1\";\n" +
                                 "1, \"Macintosh HD:/Users/test/Music\" \"Song 2\";\n" +
                                 "2, \"Macintosh HD:/Users/test/Music\" \"Song 3\";";
        Files.write(testPlaylistFile.toPath(), playlistContent.getBytes());
    }

    @After
    public void tearDown() throws IOException {
        // Clean up all temporary files
        if (testPlaylistFile != null && testPlaylistFile.exists()) {
            testPlaylistFile.delete();
        }
        if (testMusicDir != null && testMusicDir.exists()) {
            for (File f : testMusicDir.listFiles()) {
                f.delete();
            }
            testMusicDir.delete();
        }
        if (tempDir != null && Files.exists(tempDir)) {
            Files.delete(tempDir);
        }
    }

    // ==================== Workflow Tests ====================

    @Test
    public void testWorkflow_addSingleRow() throws IOException {
        // Read original playlist
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        assertEquals(3, content.size());

        // Add a new row
        List<String> updated = PlaylistUtility.addRow(
            content,
            "\"Macintosh HD:/Users/test/Music\" \"Song 4\";",
            3
        );
        assertEquals(4, updated.size());

        // Verify row numbers are sequential
        for (int i = 0; i < updated.size(); i++) {
            assertTrue(updated.get(i).startsWith(i + ","));
        }
    }

    @Test
    public void testWorkflow_insertRowInMiddle() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        // Insert at position 1
        List<String> updated = PlaylistUtility.addRow(
            content,
            "\"Macintosh HD:/Users/test/Music\" \"Song X\";",
            1
        );
        
        assertEquals(4, updated.size());
        assertTrue(updated.get(1).contains("Song X"));
        // Original Song 2 should now be at position 2
        assertTrue(updated.get(2).contains("Song 2"));
    }

    @Test
    public void testWorkflow_swapAdjacentRows() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        // Swap rows 0 and 1
        List<String> updated = PlaylistUtility.swapRows(content, 0, 1);
        
        assertTrue(updated.get(0).contains("Song 2"));
        assertTrue(updated.get(1).contains("Song 1"));
        assertTrue(updated.get(0).startsWith("0,"));  // Row number maintained
        assertTrue(updated.get(1).startsWith("1,"));  // Row number maintained
    }

    @Test
    public void testWorkflow_swapNonAdjacentRows() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        // Swap rows 0 and 2
        List<String> updated = PlaylistUtility.swapRows(content, 0, 2);
        
        assertTrue(updated.get(0).contains("Song 3"));
        assertTrue(updated.get(2).contains("Song 1"));
        assertTrue(updated.get(0).startsWith("0,"));
        assertTrue(updated.get(2).startsWith("2,"));
    }

    @Test
    public void testWorkflow_complexSequence() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        // Step 1: Add a row at end
        content = PlaylistUtility.addRow(content, "\"Macintosh HD:/Users/test/Music\" \"Song 4\";", 3);
        assertEquals(4, content.size());
        
        // Step 2: Swap first and last
        content = PlaylistUtility.swapRows(content, 0, 3);
        assertTrue(content.get(0).contains("Song 4"));
        assertTrue(content.get(3).contains("Song 1"));
        
        // Step 3: Add another row in the middle
        content = PlaylistUtility.addRow(content, "\"Macintosh HD:/Users/test/Music\" \"Song X\";", 2);
        assertEquals(5, content.size());
        
        // Verify sequential numbering throughout
        for (int i = 0; i < content.size(); i++) {
            assertTrue("Row " + i + " should start with row number",
                      content.get(i).startsWith(i + ","));
        }
    }

    // ==================== File Format Validation Tests ====================

    @Test
    public void testFileFormat_validPlaylistStructure() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        for (String line : content) {
            // Each line should have the format: volume:/path" "filename";
            assertTrue("Line should contain volume prefix", line.contains(":"));
            assertTrue("Line should contain quotes", line.contains("\""));
            assertTrue("Line should end with semicolon", line.trim().endsWith(";"));
        }
    }

    @Test
    public void testFileFormat_afterModification() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        content = PlaylistUtility.addRow(content, "\"Macintosh HD:/Users/test\" \"New Song\";", 1);
        
        // Format should still be valid
        for (String line : content) {
            assertTrue(line.contains(":"));
            assertTrue(line.contains("\""));
            assertTrue(line.trim().endsWith(";"));
        }
    }

    // ==================== Edge Cases ====================

    @Test
    public void testEdgeCase_emptyPlaylist() {
        List<String> empty = PlaylistUtility.addRow(
            java.util.Arrays.asList(),
            "\"Macintosh HD:/Users/test\" \"Only Song\";",
            0
        );
        
        assertEquals(1, empty.size());
        assertTrue(empty.get(0).startsWith("0,"));
    }

    @Test
    public void testEdgeCase_singleRowPlaylist() throws IOException {
        // Create single-row playlist
        File singleFile = new File(tempDir.toFile(), "single.txt");
        Files.write(singleFile.toPath(), "0, \"Macintosh HD:/Users/test\" \"Only Song\";".getBytes());
        
        List<String> content = PlaylistUtility.readPlaylistFile(singleFile.getAbsolutePath());
        assertEquals(1, content.size());
        
        // Add another row
        List<String> updated = PlaylistUtility.addRow(content, "\"Macintosh HD:/Users/test\" \"Song 2\";", 1);
        assertEquals(2, updated.size());
        
        singleFile.delete();
    }

    @Test
    public void testEdgeCase_largePlaylist() {
        List<String> largePlaylist = new java.util.ArrayList<>();
        
        // Create 100 songs
        for (int i = 0; i < 100; i++) {
            largePlaylist.add(String.format("\"Macintosh HD:/path\" \"Song %d\";", i));
        }
        
        // Add in the middle
        largePlaylist = PlaylistUtility.addRow(largePlaylist, "\"Macintosh HD:/path\" \"New Song\";", 50);
        assertEquals(101, largePlaylist.size());
        
        // Verify numbering
        for (int i = 0; i < largePlaylist.size(); i++) {
            assertTrue(largePlaylist.get(i).startsWith(i + ","));
        }
    }

    @Test
    public void testEdgeCase_specialCharactersInFilename() {
        List<String> playlist = java.util.Arrays.asList(
            "\"Macintosh HD:/path\" \"Song's Title\";"
        );
        
        playlist = PlaylistUtility.addRow(
            playlist,
            "\"Macintosh HD:/path\" \"Song #2 [Remix]\";",
            1
        );
        
        assertEquals(2, playlist.size());
        assertTrue(playlist.get(1).contains("Song #2 [Remix]"));
    }

    // ==================== Round-trip Tests ====================

    @Test
    public void testRoundTrip_readModifyRead() throws IOException {
        // Read original
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        int originalSize = content.size();
        
        // Modify (add and swap)
        content = PlaylistUtility.addRow(content, "\"Macintosh HD:/path\" \"New Song\";", 0);
        content = PlaylistUtility.swapRows(content, 0, 1);
        
        // Verify structure is still valid
        assertEquals(originalSize + 1, content.size());
        for (int i = 0; i < content.size(); i++) {
            assertTrue(content.get(i).startsWith(i + ","));
        }
    }

    // ==================== Audio File Detection Tests ====================

    @Test
    public void testAudioFileDetection_multipleSupportedFormats() throws IOException {
        // Create files of different types
        File mp3 = new File(testMusicDir, "song.mp3");
        File m4a = new File(testMusicDir, "song.m4a");
        File wav = new File(testMusicDir, "song.wav");
        File txt = new File(testMusicDir, "readme.txt");
        
        mp3.createNewFile();
        m4a.createNewFile();
        wav.createNewFile();
        txt.createNewFile();
        
        List<File> audioFiles = PlaylistUtility.getAudioFilesInDirectory(testMusicDir);
        
        assertEquals(3, audioFiles.size());
        assertTrue(audioFiles.stream().anyMatch(f -> f.getName().equals("song.mp3")));
        assertTrue(audioFiles.stream().anyMatch(f -> f.getName().equals("song.m4a")));
        assertTrue(audioFiles.stream().anyMatch(f -> f.getName().equals("song.wav")));
        assertFalse(audioFiles.stream().anyMatch(f -> f.getName().equals("readme.txt")));
    }

    @Test
    public void testAudioFileDetection_sorting() throws IOException {
        // Create files in reverse alphabetical order
        String[] names = {"z_song.mp3", "a_song.mp3", "m_song.mp3"};
        for (String name : names) {
            new File(testMusicDir, name).createNewFile();
        }
        
        List<File> audioFiles = PlaylistUtility.getAudioFilesInDirectory(testMusicDir);
        
        assertEquals(3, audioFiles.size());
        assertEquals("a_song.mp3", audioFiles.get(0).getName());
        assertEquals("m_song.mp3", audioFiles.get(1).getName());
        assertEquals("z_song.mp3", audioFiles.get(2).getName());
    }
}
