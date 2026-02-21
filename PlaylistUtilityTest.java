import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PlaylistUtilityTest {

    private Path tempDir;
    private File testAudioFile;
    private File testPlaylistFile;

    @Before
    public void setUp() throws IOException {
        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("playlist_test_");
        
        // Create a test playlist file
        testPlaylistFile = new File(tempDir.toFile(), "playlist.txt");
        String playlistContent = "0, \"volume:/path/to/file1\" \"Song 1\";\n" +
                                 "1, \"volume:/path/to/file2\" \"Song 2\";\n" +
                                 "2, \"volume:/path/to/file3\" \"Song 3\";";
        Files.write(testPlaylistFile.toPath(), playlistContent.getBytes());
    }

    @After
    public void tearDown() throws IOException {
        // Clean up temporary files (don't use testAudioFile as it's not always created)
        if (testPlaylistFile != null && testPlaylistFile.exists()) {
            testPlaylistFile.delete();
        }
        // Clean up entire temp directory recursively
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    // ==================== Audio File Tests ====================

    @Test
    public void testIsSupportedAudioFile_mp3() {
        File mp3File = new File("test.mp3");
        assertTrue(PlaylistUtility.isSupportedAudioFile(mp3File));
    }

    @Test
    public void testIsSupportedAudioFile_m4a() {
        File m4aFile = new File("test.m4a");
        assertTrue(PlaylistUtility.isSupportedAudioFile(m4aFile));
    }

    @Test
    public void testIsSupportedAudioFile_wav() {
        File wavFile = new File("test.wav");
        assertTrue(PlaylistUtility.isSupportedAudioFile(wavFile));
    }

    @Test
    public void testIsSupportedAudioFile_aiff() {
        File aiffFile = new File("test.aiff");
        assertTrue(PlaylistUtility.isSupportedAudioFile(aiffFile));
    }

    @Test
    public void testIsSupportedAudioFile_flac() {
        File flacFile = new File("test.flac");
        assertTrue(PlaylistUtility.isSupportedAudioFile(flacFile));
    }

    @Test
    public void testIsSupportedAudioFile_unsupported() {
        File textFile = new File("test.txt");
        assertFalse(PlaylistUtility.isSupportedAudioFile(textFile));
    }

    @Test
    public void testIsSupportedAudioFile_caseInsensitive() {
        File mp3File = new File("TEST.MP3");
        assertTrue(PlaylistUtility.isSupportedAudioFile(mp3File));
    }

    // ==================== File Content Tests ====================

    @Test
    public void testPrepareAudioFileContent() throws IOException {
        // Create a test audio file for this test
        File testFile = new File(tempDir.toFile(), "test_song.mp3");
        testFile.createNewFile();
        
        String content = PlaylistUtility.prepareAudioFileContent(testFile);
        
        assertTrue(content.contains(":"));  // Contains volume prefix
        assertTrue(content.contains("test_song"));  // Contains filename without extension
        assertTrue(content.contains("\""));  // Contains quotes for format
        assertTrue(content.endsWith(";"));  // Ends with semicolon
        
        testFile.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAudioFileContent_nonexistentFile() {
        File nonexistent = new File("/nonexistent/file.mp3");
        PlaylistUtility.prepareAudioFileContent(nonexistent);
    }

    @Test
    public void testPrepareAudioFileContent_format() throws IOException {
        // Create a test audio file for this test
        File testFile = new File(tempDir.toFile(), "test_format.mp3");
        testFile.createNewFile();
        
        String content = PlaylistUtility.prepareAudioFileContent(testFile);
        
        // Should match format: "volume:/path" "filename";
        assertTrue(content.matches("\".*?:.*?\"\\s+\".*?\";"));
        
        testFile.delete();
    }

    // ==================== Row Operations Tests ====================

    @Test
    public void testAddRow_atEnd() {
        List<String> lines = Arrays.asList(
            "\"vol1:/path1\" \"Song 1\";",
            "\"vol2:/path2\" \"Song 2\";"
        );
        
        List<String> result = PlaylistUtility.addRow(lines, "\"vol3:/path3\" \"Song 3\";", 2);
        
        assertEquals(3, result.size());
        assertTrue(result.get(2).startsWith("2,"));
        assertEquals("\"vol3:/path3\" \"Song 3\";", result.get(2).substring(3).trim());
    }

    @Test
    public void testAddRow_atBeginning() {
        List<String> lines = Arrays.asList(
            "\"vol1:/path1\" \"Song 1\";",
            "\"vol2:/path2\" \"Song 2\";"
        );
        
        List<String> result = PlaylistUtility.addRow(lines, "\"vol0:/path0\" \"Song 0\";", 0);
        
        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("0,"));
        assertTrue(result.get(1).startsWith("1,"));
        assertTrue(result.get(2).startsWith("2,"));
    }

    @Test
    public void testAddRow_inMiddle() {
        List<String> lines = Arrays.asList(
            "\"vol1:/path1\" \"Song 1\";",
            "\"vol3:/path3\" \"Song 3\";"
        );
        
        List<String> result = PlaylistUtility.addRow(lines, "\"vol2:/path2\" \"Song 2\";", 1);
        
        assertEquals(3, result.size());
        assertTrue(result.get(0).startsWith("0,"));
        assertTrue(result.get(1).startsWith("1,"));
        assertTrue(result.get(2).startsWith("2,"));
    }

    @Test
    public void testAddRow_renumberingCorrect() {
        List<String> lines = Arrays.asList(
            "\"vol1:/path1\" \"Song 1\";",
            "\"vol2:/path2\" \"Song 2\";"
        );
        
        List<String> result = PlaylistUtility.addRow(lines, "\"vol0:/path0\" \"Song 0\";", 0);
        
        // Verify sequential numbering
        for (int i = 0; i < result.size(); i++) {
            assertTrue(result.get(i).startsWith(i + ","));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRow_invalidRowNumberNegative() {
        List<String> lines = Arrays.asList("\"vol1:/path1\" \"Song 1\";");
        PlaylistUtility.addRow(lines, "\"vol2:/path2\" \"Song 2\";", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRow_invalidRowNumberTooLarge() {
        List<String> lines = Arrays.asList("\"vol1:/path1\" \"Song 1\";");
        PlaylistUtility.addRow(lines, "\"vol2:/path2\" \"Song 2\";", 5);
    }

    // ==================== Swap Operations Tests ====================

    @Test
    public void testSwapRows() {
        List<String> lines = Arrays.asList(
            "0, \"vol1:/path1\" \"Song 1\";",
            "1, \"vol2:/path2\" \"Song 2\";",
            "2, \"vol3:/path3\" \"Song 3\";"
        );
        
        List<String> result = PlaylistUtility.swapRows(lines, 0, 2);
        
        assertEquals(3, result.size());
        assertTrue(result.get(0).contains("Song 3"));  // Song 3 is now at position 0
        assertTrue(result.get(2).contains("Song 1"));  // Song 1 is now at position 2
        assertTrue(result.get(0).startsWith("0,"));    // Row number maintained
        assertTrue(result.get(2).startsWith("2,"));    // Row number maintained
    }

    @Test
    public void testSwapRows_adjacentRows() {
        List<String> lines = Arrays.asList(
            "0, \"vol1:/path1\" \"Song 1\";",
            "1, \"vol2:/path2\" \"Song 2\";"
        );
        
        List<String> result = PlaylistUtility.swapRows(lines, 0, 1);
        
        assertTrue(result.get(0).contains("Song 2"));
        assertTrue(result.get(1).contains("Song 1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwapRows_invalidRow1() {
        List<String> lines = Arrays.asList("0, \"vol1:/path1\" \"Song 1\";");
        PlaylistUtility.swapRows(lines, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwapRows_invalidRow2() {
        List<String> lines = Arrays.asList("0, \"vol1:/path1\" \"Song 1\";");
        PlaylistUtility.swapRows(lines, 0, 5);
    }

    @Test
    public void testSwapRowsByContent() {
        List<String> lines = Arrays.asList(
            "0, \"vol1:/path1\" \"Song 1\";",
            "1, \"vol2:/path2\" \"Song 2\";",
            "2, \"vol3:/path3\" \"Song 3\";"
        );
        
        String[] searchKeys = {"Song 1", "Song 3"};
        List<String> result = PlaylistUtility.swapRowsByContent(lines, searchKeys);
        
        assertEquals(3, result.size());
        // After rotation: Song 3 goes to Song 1's position, Song 1 goes to Song 3's position
        assertTrue(result.get(0).contains("Song 3"));
        assertTrue(result.get(2).contains("Song 1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwapRowsByContent_notFound() {
        List<String> lines = Arrays.asList(
            "0, \"vol1:/path1\" \"Song 1\";",
            "1, \"vol2:/path2\" \"Song 2\";"
        );
        
        String[] searchKeys = {"Nonexistent Song"};
        PlaylistUtility.swapRowsByContent(lines, searchKeys);
    }

    // ==================== File I/O Tests ====================

    @Test
    public void testReadPlaylistFile() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        assertEquals(3, content.size());
        assertTrue(content.get(0).contains("Song 1"));
        assertTrue(content.get(1).contains("Song 2"));
        assertTrue(content.get(2).contains("Song 3"));
    }

    @Test
    public void testReadPlaylistFile_removesRowNumbers() throws IOException {
        List<String> content = PlaylistUtility.readPlaylistFile(testPlaylistFile.getAbsolutePath());
        
        // Content should not start with row numbers
        for (String line : content) {
            assertFalse(line.matches("^\\d+,.*"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void testReadPlaylistFile_nonexistent() {
        PlaylistUtility.readPlaylistFile("/nonexistent/file.txt");
    }

    // ==================== Directory Scanning Tests ====================

    @Test
    public void testGetAudioFilesInDirectory() throws IOException {
        // Create multiple audio files
        File mp3 = new File(tempDir.toFile(), "song1.mp3");
        File m4a = new File(tempDir.toFile(), "song2.m4a");
        File txt = new File(tempDir.toFile(), "readme.txt");
        
        mp3.createNewFile();
        m4a.createNewFile();
        txt.createNewFile();
        
        List<File> audioFiles = PlaylistUtility.getAudioFilesInDirectory(tempDir.toFile());
        
        assertEquals(2, audioFiles.size());
        assertFalse(audioFiles.contains(txt));
    }

    @Test
    public void testGetAudioFilesInDirectory_sorted() throws IOException {
        // Create audio files with names that would be out of order
        File file3 = new File(tempDir.toFile(), "z_song.mp3");
        File file1 = new File(tempDir.toFile(), "a_song.mp3");
        File file2 = new File(tempDir.toFile(), "m_song.mp3");
        
        file3.createNewFile();
        file1.createNewFile();
        file2.createNewFile();
        
        List<File> audioFiles = PlaylistUtility.getAudioFilesInDirectory(tempDir.toFile());
        
        assertEquals(3, audioFiles.size());
        assertEquals("a_song.mp3", audioFiles.get(0).getName());
        assertEquals("m_song.mp3", audioFiles.get(1).getName());
        assertEquals("z_song.mp3", audioFiles.get(2).getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAudioFilesInDirectory_notDirectory() throws IOException {
        File testFile = new File(tempDir.toFile(), "notadir.mp3");
        testFile.createNewFile();
        PlaylistUtility.getAudioFilesInDirectory(testFile);  // File, not directory
    }

    // ==================== Display Formatting Tests ====================

    @Test
    public void testFormatForDisplay() {
        List<String> lines = Arrays.asList(
            "\"vol1:/path1\" \"Song 1\";",
            "\"vol2:/path2\" \"Song 2\";"
        );
        
        String formatted = PlaylistUtility.formatForDisplay(lines);
        
        assertTrue(formatted.contains("0,"));
        assertTrue(formatted.contains("1,"));
        assertTrue(formatted.contains("Song 1"));
        assertTrue(formatted.contains("Song 2"));
    }

    @Test
    public void testFormatForDisplay_empty() {
        List<String> lines = Arrays.asList();
        String formatted = PlaylistUtility.formatForDisplay(lines);
        
        assertEquals("", formatted);
    }

    // ==================== Integration Tests ====================

    @Test
    public void testIntegration_addAndSwapRows() {
        List<String> lines = Arrays.asList(
            "\"vol1:/path1\" \"Song 1\";",
            "\"vol2:/path2\" \"Song 2\";"
        );
        
        // Add a row
        lines = PlaylistUtility.addRow(lines, "\"vol0:/path0\" \"Song 0\";", 0);
        assertEquals(3, lines.size());
        
        // After adding at position 0, rows are: Song 0 (row 0), Song 1 (row 1), Song 2 (row 2)
        // Now swap rows 0 and 2
        lines = PlaylistUtility.swapRows(lines, 0, 2);
        assertTrue(lines.get(0).contains("Song 2"));  // Song 2 moved to row 0
        assertTrue(lines.get(2).contains("Song 0"));  // Song 0 moved to row 2
    }

    @Test
    public void testIntegration_multipleAdditions() {
        List<String> lines = Arrays.asList();
        
        lines = PlaylistUtility.addRow(lines, "\"vol1:/path1\" \"Song 1\";", 0);
        assertEquals(1, lines.size());
        
        lines = PlaylistUtility.addRow(lines, "\"vol2:/path2\" \"Song 2\";", 1);
        assertEquals(2, lines.size());
        
        lines = PlaylistUtility.addRow(lines, "\"vol3:/path3\" \"Song 3\";", 1);
        assertEquals(3, lines.size());
        
        // Verify correct numbering
        for (int i = 0; i < lines.size(); i++) {
            assertTrue(lines.get(i).startsWith(i + ","));
        }
    }
}
