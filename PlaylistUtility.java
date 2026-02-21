import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for playlist operations. Extracted from APEGUI to enable testing.
 */
public class PlaylistUtility {

    /**
     * Detects volume name from a file path using diskutil on macOS.
     */
    public static String getVolumeNameFromPath(String canonicalPath) {
        if (canonicalPath.startsWith("/Volumes/")) {
            String remainder = canonicalPath.substring("/Volumes/".length());
            int slashIndex = remainder.indexOf('/');
            if (slashIndex != -1) {
                return remainder.substring(0, slashIndex);
            }
        }

        // System volume - detect using FileStore
        try {
            java.nio.file.FileStore store = Files.getFileStore(Paths.get(canonicalPath));
            String deviceName = store.toString(); // e.g., /dev/disk3s5

            if (deviceName.contains("/dev/disk")) {
                String diskPart = deviceName.replaceAll(".*(/dev/disk\\d+).*", "$1");
                ProcessBuilder pb = new ProcessBuilder("diskutil", "list");
                Process process = pb.start();
                Scanner scanner = new Scanner(process.getInputStream());

                String currentDisk = null;
                String mainVolume = null;

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("/dev/disk")) {
                        currentDisk = line.split("\\s+")[0];
                    }
                    if (currentDisk != null && currentDisk.equals(diskPart) && line.contains("APFS Volume")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length > 0) {
                            String volumeName = parts[parts.length - 1];
                            if (!volumeName.matches("(Preboot|Recovery|VM|Data)")) {
                                mainVolume = volumeName;
                                break;
                            }
                        }
                    }
                }
                scanner.close();
                process.waitFor();

                if (mainVolume != null) {
                    return mainVolume;
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }

        return "Macintosh HD";
    }

    /**
     * Prepares audio file content in the format: "volume:/path" "filename";
     */
    public static String prepareAudioFileContent(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        String canonical;
        try {
            canonical = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not get canonical path for: " + file, e);
        }

        String volumePrefix;
        String relativePath;

        if (canonical.startsWith("/Volumes/")) {
            String remainder = canonical.substring("/Volumes/".length());
            int slashIndex = remainder.indexOf('/');
            if (slashIndex != -1) {
                String volumeName = remainder.substring(0, slashIndex);
                relativePath = remainder.substring(slashIndex);
                volumePrefix = volumeName + ":";
            } else {
                throw new RuntimeException("Invalid path structure: " + canonical);
            }
        } else {
            String detectedVolumeName = getVolumeNameFromPath(canonical);
            volumePrefix = detectedVolumeName + ":";
            relativePath = canonical;
        }

        String fileName = file.getName();
        String displayName = fileName.replaceFirst("\\.[^.]+$", "");

        return String.format(
            "\"%s%s\" \"%s\";",
            volumePrefix,
            relativePath,
            displayName
        );
    }

    /**
     * Reads playlist file and returns content rows (without row numbers).
     */
    public static List<String> readPlaylistFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            return lines.stream()
                .map(line -> {
                    int commaIndex = line.indexOf(',');
                    return (commaIndex != -1) ? line.substring(commaIndex + 1).trim() : line;
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not read playlist file: " + filePath, e);
        }
    }

    /**
     * Adds a single row at the specified position and renumbers all rows sequentially.
     */
    public static List<String> addRow(List<String> lines, String content, int rowNumber) {
        if (rowNumber < 0 || rowNumber > lines.size()) {
            throw new IllegalArgumentException(
                String.format("Invalid row number %d (valid range: 0-%d)", rowNumber, lines.size())
            );
        }

        List<String> result = new ArrayList<>(lines);
        result.add(rowNumber, content);

        // Renumber all rows sequentially
        for (int i = 0; i < result.size(); i++) {
            result.set(i, i + ", " + result.get(i));
        }

        return result;
    }

    /**
     * Swaps content between two rows, maintaining row numbers.
     */
    public static List<String> swapRows(List<String> lines, int row1, int row2) {
        if (row1 < 0 || row1 >= lines.size() || row2 < 0 || row2 >= lines.size()) {
            throw new IllegalArgumentException(
                String.format("Invalid row numbers: %d, %d (valid range: 0-%d)", row1, row2, lines.size() - 1)
            );
        }

        List<String> result = new ArrayList<>(lines);

        // Extract content (remove row numbers)
        String line1 = result.get(row1);
        String line2 = result.get(row2);
        int comma1 = line1.indexOf(',');
        int comma2 = line2.indexOf(',');
        String content1 = (comma1 != -1) ? line1.substring(comma1 + 1).trim() : line1;
        String content2 = (comma2 != -1) ? line2.substring(comma2 + 1).trim() : line2;

        // Swap with row numbers maintained
        result.set(row1, row1 + ", " + content2);
        result.set(row2, row2 + ", " + content1);

        return result;
    }

    /**
     * Swaps rows based on content search and circular rotation.
     */
    public static List<String> swapRowsByContent(List<String> lines, String[] searchKeys) {
        Map<String, Integer> matches = new HashMap<>();

        // Find rows containing the search keys in their content
        for (String key : searchKeys) {
            int foundIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int commaIndex = line.indexOf(',');
                String content = (commaIndex != -1) ? line.substring(commaIndex + 1).trim() : line;
                if (content.contains(key.trim())) {
                    foundIndex = i;
                    break;
                }
            }
            if (foundIndex == -1) {
                throw new IllegalArgumentException("Could not find content: " + key.trim());
            }
            matches.put(key.trim(), foundIndex);
        }

        // Extract content from matched rows
        Map<Integer, String> contentMap = new HashMap<>();
        for (int rowNum : matches.values()) {
            String line = lines.get(rowNum);
            int commaIndex = line.indexOf(',');
            String content = (commaIndex != -1) ? line.substring(commaIndex + 1).trim() : line;
            contentMap.put(rowNum, content);
        }

        // Rotate the list of row indices to swap their content
        List<Integer> rowIndices = new ArrayList<>(matches.values());
        Collections.rotate(rowIndices, 1);

        // Reassign rotated content back to rows
        List<String> result = new ArrayList<>(lines);
        for (int i = 0; i < rowIndices.size(); i++) {
            int currentRow = rowIndices.get(i);
            int previousRow = rowIndices.get((i + rowIndices.size() - 1) % rowIndices.size());
            result.set(currentRow, currentRow + ", " + contentMap.get(previousRow));
        }

        return result;
    }

    /**
     * Formats lines with sequential row numbers for display.
     */
    public static String formatForDisplay(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // If line already has row number, skip it; otherwise add it
            if (line.matches("^\\d+,\\s.*")) {
                sb.append(line);
            } else {
                sb.append(i).append(", ").append(line);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Checks if a file is a supported audio format.
     */
    public static boolean isSupportedAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".m4a") ||
               name.endsWith(".wav") || name.endsWith(".aiff") ||
               name.endsWith(".flac");
    }

    /**
     * Scans directory for audio files and sorts them alphabetically.
     */
    public static List<File> getAudioFilesInDirectory(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        File[] files = directory.listFiles(PlaylistUtility::isSupportedAudioFile);
        if (files == null) {
            return new ArrayList<>();
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        return Arrays.asList(files);
    }
}
