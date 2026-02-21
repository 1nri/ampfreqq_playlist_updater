import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class APEGUI extends JFrame {

    private JTextField filePathField;
    private JTextField rowNumberField;
    private JTextField newLineField;
    private JTextField swapRowsField;
    private JTextField saveAsField;
    private JTextField matchStringsField;
    private JTextField matchTargetsField;
    private JButton insertButton;
    private JButton browseButton;
    private JButton previewButton;
    private JButton swapButton;
    private JButton swapByContentButton;
    private JButton saveButton;
    private JButton clearPreviewButton;
    private JTextArea outputArea;
    private JTextArea logArea;

    /* This is not functional as of yet
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Drag and drop example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            // Enable drag and drop
            textArea1.setDragEnabled(true);
            textArea2.setDragEnabled(true);

            // Layout
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                  new JScrollPane(textArea1),
                                                  new JScrollPane(textArea2));
            frame.add(splitPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });
     */

    private List<String> modifiedLines = null;
    private final StringBuilder changeLog = new StringBuilder();

    /* This is the version to work on, ignore the other branch */

    // Constructor to set up the GUI

    public APEGUI() {
        setTitle("AmpFreQQ Playlist Editor APE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(10, 3));
        filePathField = new JTextField();
        rowNumberField = new JTextField();
        newLineField = new JTextField();
        swapRowsField = new JTextField();
        saveAsField = new JTextField();
        matchStringsField = new JTextField();
        matchTargetsField = new JTextField();

        insertButton = new JButton("Add Row");
        browseButton = new JButton("Browse...");
        previewButton = new JButton("Preview Changes");
        swapButton = new JButton("Swap Rows");
        swapByContentButton = new JButton("Swap Rows By Content");
        saveButton = new JButton("Save Changes");
        clearPreviewButton = new JButton("Clear Preview");
        JButton musicDirectoryButton = new JButton("Music Directory...");

        filePathField.setDragEnabled(true);
        newLineField.setDragEnabled(true);

        filePathField.setEditable(false);
        filePathField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        filePathField.setHorizontalAlignment(JTextField.CENTER);
        filePathField.setBackground(new Color(220, 220, 220)); // Gray background
        filePathField.setForeground(Color.BLACK);
/*
            // Lisätään pudotustuki
            new DropTarget(filePathField, new DropTargetListener() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {}

                @Override
                public void dragOver(DropTargetDragEvent dtde) {}

                @Override
                public void dropActionChanged(DropTargetDragEvent dtde) {}

                @Override
                public void dragExit(DropTargetEvent dte) {}

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                        if (transferData instanceof List) {
                            List<?> droppedFiles = (List<?>) transferData;
                            if (!droppedFiles.isEmpty() && droppedFiles.get(0) instanceof File) {
                                File file = (File) droppedFiles.get(0);
                                filePathField.setText(file.getAbsolutePath());
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        filePathField.setText("Error reading file");
                    }
                }

                @Override
                public void dragExit(DropTargetEvent arg0) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'dragExit'");
                }

                @Override
                public void drop(DropTargetDropEvent arg0) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'drop'");
                }
            });

            */
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        panel.add(new JLabel("Path To File:"));
        panel.add(filePathField);
        panel.add(browseButton);

        panel.add(new JLabel("Row Number (Insert):"));
        panel.add(rowNumberField);
        panel.add(new JLabel());

        panel.add(new JLabel("The row to be added (without numbering):"));
        panel.add(newLineField);
        panel.add(insertButton);

        panel.add(new JLabel("Numbers for Rows to Swap (eg. 3,7):"));
        panel.add(swapRowsField);
        panel.add(swapButton);

        panel.add(new JLabel("Content of Rows to Swap (Character Strings):"));
        panel.add(matchStringsField);
        panel.add(new JLabel("Character Strings to Match, separated by commas:"));

        panel.add(new JLabel("Target rows (number or character strings):"));
        panel.add(matchTargetsField);
        panel.add(swapByContentButton);

        panel.add(new JLabel("New File Name (optional):"));
        panel.add(saveAsField);
        panel.add(saveButton);

        panel.add(musicDirectoryButton);
        panel.add(previewButton);
        panel.add(clearPreviewButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        new DropTarget(newLineField, new DropTargetListener() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {}

                @Override
                public void dragOver(DropTargetDragEvent dtde) {}

                @Override
                public void dropActionChanged(DropTargetDragEvent dtde) {}

                @Override
                public void dragExit(DropTargetEvent dte) {}

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                        if (transferData instanceof List) {
                            List<?> droppedFiles = (List<?>) transferData;
                            if (!droppedFiles.isEmpty() && droppedFiles.get(0) instanceof File) {
                                prepareFileName(droppedFiles);
                                   
                            }
                            else{
                            outputArea.setText("Please drop a supported audio file.");
                            return;
                        }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        outputArea.setText("Couldn't read the file");
                    }
                }

                private void prepareFileName(List<?> object) {
                    File file = (File) object.get(0);
                    String canonicalPath;
                    try {
                        canonicalPath = file.getCanonicalPath(); // reveals mount points and symlinks
                        } catch (IOException e) {
                            System.err.println("Invalid path.");
                            return;
                        }
                        String volumePrefix;
                        String relativePath;
                        
                        if(file.getName().endsWith(".mp3") || file.getName().endsWith(".m4a") ||  file.getName().endsWith(".wav") 
                        || file.getName().endsWith(".aiff") || file.getName().endsWith(".flac"))
                        {
                            
                        } 
                        else{
                            outputArea.setText("Please drop a supported audio file.");
                            return;
                        }

                                    
                // 3. Determine the of Volume (volumename)

                volumePrefix = "";
                relativePath = "";
                
                if (canonicalPath.startsWith("/Volumes/")) {
                    // External/mounted volume: /Volumes/volumename/path/to/file
                    String remainder = canonicalPath.substring("/Volumes/".length()); // volumename/path/...
                    int slashIndex = remainder.indexOf('/');
                    if (slashIndex != -1) {
                        String volumeName = remainder.substring(0, slashIndex);
                        relativePath = remainder.substring(slashIndex); // /path/to/file
                        volumePrefix = volumeName + ":";
                    } else {
                        System.err.println("Error: Path does not have directory structure after volume name.");
                        return;
                    }
                } else {
                    // System volume - detect actual volume name using FileStore
                    String detectedVolumeName = getVolumeNameFromPath(canonicalPath);
                    volumePrefix = detectedVolumeName + ":";
                    relativePath = canonicalPath;
                }

                        // 4. Determine display name for given file
                        String fileName = file.getName(); 
                        String displayName = fileName.replaceFirst("\\.[^.]+$", "");

                        // 5. Format the content (without row number - row number will be added during preview)
                        String contentOnly = String.format(
                            "\"%s%s\" \"%s\";",
                            volumePrefix,
                            relativePath,
                            displayName
                            );

                            newLineField.setText(contentOnly);
                        }
                        
                    }
                     
                
            );

            new DropTarget(filePathField, new DropTargetListener() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {}

                @Override
                public void dragOver(DropTargetDragEvent dtde) {}

                @Override
                public void dropActionChanged(DropTargetDragEvent dtde) {}

                @Override
                public void dragExit(DropTargetEvent dte) {}

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                        if (transferData instanceof List) {
                            List<?> droppedFiles = (List<?>) transferData;
                            if (!droppedFiles.isEmpty() && droppedFiles.get(0) instanceof File) {
                                File file = (File) droppedFiles.get(0);
                                if(file.getName().endsWith(".txt")){    
                                    filePathField.setText(file.getAbsolutePath());
                                }
                                else{
                                    outputArea.setText("Please drop a valid TXT file.");
                                    return;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        outputArea.setText("Couldn't read the file");
                    }
                }
            });

        

        insertButton.addActionListener(e -> insertLine());
        browseButton.addActionListener(e -> browseFile());
        previewButton.addActionListener(e -> previewChanges());
        swapButton.addActionListener(e -> swapLines());
        swapByContentButton.addActionListener(e -> swapLinesByContent());
        saveButton.addActionListener(e -> saveChanges());
        clearPreviewButton.addActionListener(e -> clearPreview());
        musicDirectoryButton.addActionListener(e -> selectMusicDirectory());
    }

    private void logChange(String description) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        changeLog.append("[" + time + "] ").append(description).append("\n");
        logArea.setText(changeLog.toString());
    }

    private void saveLogAutomatically(Path original) {
        Path logPath = original.resolveSibling("change_log.txt");
        try {
            Files.writeString(logPath, changeLog.toString());
        } catch (IOException e) {
            outputArea.setText("Log file couldn't be saved: " + e.getMessage());
        }
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (selected.isDirectory()) {
                // User selected a directory - load multiple audio files
                if (rowNumberField.getText().trim().isEmpty()) {
                    outputArea.setText("Please enter a starting row number for batch import.");
                    return;
                }
                try {
                    int startRow = Integer.parseInt(rowNumberField.getText().trim());
                    addMultipleRows(selected, startRow);
                } catch (NumberFormatException e) {
                    outputArea.setText("Invalid starting row number.");
                }
            } else {
                // User selected a file
                filePathField.setText(selected.getAbsolutePath());
                logChange("File selected: " + selected.getName());
            }
        }
    }

    private void selectMusicDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Music Directory");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            if (rowNumberField.getText().trim().isEmpty()) {
                outputArea.setText("Please enter a starting row number for batch import.");
                return;
            }
            try {
                int startRow = Integer.parseInt(rowNumberField.getText().trim());
                addMultipleRows(selectedDirectory, startRow);
            } catch (NumberFormatException e) {
                outputArea.setText("Invalid starting row number.");
            }
        }
    }

    private void previewChanges() {
        String filePath = filePathField.getText().trim();
        String rowStr = rowNumberField.getText().trim();
        String newContent = newLineField.getText().trim();

        int insertAt;
        try {
            insertAt = Integer.parseInt(rowStr);
        } catch (NumberFormatException e) {
            outputArea.setText("Incorrect Row.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            List<String> contentList = new ArrayList<>();

            // Extract content from all existing rows
            for (String line : lines) {
                int commaIndex = line.indexOf(',');
                if (commaIndex != -1) {
                    String content = line.substring(commaIndex + 1).trim();
                    contentList.add(content);
                } else {
                    contentList.add(line);
                }
            }

            // Validate insertAt
            if (insertAt < 0 || insertAt > contentList.size()) {
                outputArea.setText("Row number out of range. Valid range: 0 to " + contentList.size());
                return;
            }

            // Insert new content at the specified position
            contentList.add(insertAt, newContent);

            // Create preview with sequential numbering
            List<String> previewLines = new ArrayList<>();
            for (int i = 0; i < contentList.size(); i++) {
                previewLines.add(i + ", " + contentList.get(i));
            }

            modifiedLines = previewLines;
            outputArea.setText(String.join("\n", previewLines));
            logChange("Preview: Row added at position " + insertAt);
        } catch (IOException ex) {
            outputArea.setText("Editing of file failed: " + ex.getMessage());
        }
    }

    private void insertLine() {
        outputArea.setText("Preview first and select 'Save file' afterwards.");
    }

    private void swapLines() {
        String filePath = filePathField.getText().trim();
        String swapInput = swapRowsField.getText().trim();

        try {
            List<Integer> rowNumbers = Arrays.stream(swapInput.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (rowNumbers.size() < 2) {
                outputArea.setText("You need to provide at least two numbers separated by a comma.");
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(filePath));
            
            // Extract content from specified row numbers
            Map<Integer, String> contentMap = new HashMap<>();
            for (int rowNum : rowNumbers) {
                if (rowNum < 0 || rowNum >= lines.size()) {
                    outputArea.setText("Row number " + rowNum + " is out of range (0-" + (lines.size() - 1) + ")");
                    return;
                }
                String line = lines.get(rowNum);
                int commaIndex = line.indexOf(',');
                if (commaIndex != -1) {
                    contentMap.put(rowNum, line.substring(commaIndex + 1).trim());
                } else {
                    contentMap.put(rowNum, line);
                }
            }

            // Rotate content cyclically
            Collections.rotate(rowNumbers, 1);

            // Reassign rotated content back to rows
            for (int i = 0; i < rowNumbers.size(); i++) {
                int currentRow = rowNumbers.get(i);
                int previousContent = rowNumbers.get((i + rowNumbers.size() - 1) % rowNumbers.size());
                lines.set(currentRow, currentRow + ", " + contentMap.get(previousContent));
            }

            modifiedLines = lines;
            outputArea.setText(String.join("\n", lines));
            logChange("Swapped the rows: " + swapInput);
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid row numbers provided.");
        } catch (Exception e) {
            outputArea.setText("Failed swapping the rows: " + e.getMessage());
        }
    }

    private void swapLinesByContent() {
        String filePath = filePathField.getText().trim();
        String[] keys = matchStringsField.getText().split(",");

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            Map<String, Integer> matches = new HashMap<>();

            // Find rows containing the search keys in their content
            for (String key : keys) {
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
                    outputArea.setText("Couldn't find the following string of characters: " + key.trim());
                    return;
                }
                matches.put(key.trim(), foundIndex);
            }

            // Show confirmation dialog
            StringBuilder confirmation = new StringBuilder("Confirm the following results:\n");
            for (String key : matches.keySet()) {
                confirmation.append("[" + key + "] => row " + matches.get(key) + "\n");
            }

            int dialogResult = JOptionPane.showConfirmDialog(this, confirmation.toString(), "Confirm swap", JOptionPane.YES_NO_OPTION);
            if (dialogResult != JOptionPane.YES_OPTION) {
                outputArea.setText("Canceled.");
                return;
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

            // Reassign rotated content back to rows, maintaining row numbers
            for (int i = 0; i < rowIndices.size(); i++) {
                int currentRow = rowIndices.get(i);
                int previousRow = rowIndices.get((i + rowIndices.size() - 1) % rowIndices.size());
                lines.set(currentRow, currentRow + ", " + contentMap.get(previousRow));
            }

            modifiedLines = lines;
            outputArea.setText(String.join("\n", lines));
            logChange("Swapped the following rows based on content: " + matchStringsField.getText());
        } catch (IOException e) {
            outputArea.setText("Failed swapping the rows based on the following content: " + e.getMessage());
        }
    }

    private void saveChanges() {
        String saveName = saveAsField.getText().trim();
        String originalPath = filePathField.getText().trim();

        if (modifiedLines == null) {
            outputArea.setText("No changes to save. Preview or swap records first.");
            return;
        }

        try {
            Path original = Paths.get(originalPath);
            Path target = saveName.isEmpty() ? original : original.resolveSibling(saveName);
            Files.write(target, modifiedLines);
            saveLogAutomatically(original);
            outputArea.setText("Saved changes to file: " + target);
            logChange("Saved to file: " + target.getFileName());
            
            // Auto-preview the saved content
            previewContent();
            
            // Clear input fields
            clearInputs();
        } catch (IOException e) {
            outputArea.setText("Error saving the file: " + e.getMessage());
        }
    }

    private void previewContent() {
        String filePath = filePathField.getText().trim();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            List<String> previewLines = new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                previewLines.add(line);
            }
            outputArea.setText(String.join("\n", previewLines));
            logChange("Preview ");
        } catch (IOException ex) {
            outputArea.setText("Preview  failed: " + ex.getMessage());
        }
    }


    // Detects actual volume name from a file path using diskutil
    private String getVolumeNameFromPath(String canonicalPath) {
        try {
            // Get the device from FileStore
            Path path = Paths.get(canonicalPath);
            FileStore store = Files.getFileStore(path);
            String deviceName = store.name(); // e.g., /dev/disk3s5
            
            // Extract the base disk (e.g., disk3 from /dev/disk3s5)
            String baseDisk = deviceName.replaceAll("/dev/", "").replaceAll("s\\d+.*$", "");
            
            // Use diskutil list to get volume information
            ProcessBuilder pb = new ProcessBuilder("diskutil", "list");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String containerVolumeName = null;
            
            while ((line = reader.readLine()) != null) {
                
                // Looking for lines like: "     1:                APFS Volume mbadrive                12.3 GB    disk3s1"
                if (line.contains("APFS Volume") && line.contains(baseDisk + "s")) {
                    // Extract volume name - it's between "APFS Volume" and the size
                    int volumeIndex = line.indexOf("APFS Volume");
                    if (volumeIndex != -1) {
                        String afterVolume = line.substring(volumeIndex + 11).trim();
                        // Split by whitespace to get the name and size
                        String[] parts = afterVolume.split("\\s+");
                        if (parts.length > 0) {
                            String volumeName = parts[0];
                            // Only consider the main volume (not Preboot, Recovery, VM)
                            if (!volumeName.equals("Preboot") && !volumeName.equals("Recovery") && 
                                !volumeName.equals("VM") && !volumeName.equals("Data") && 
                                !volumeName.matches(".*update.*") && !volumeName.matches(".*Snapshot.*")) {
                                containerVolumeName = volumeName;
                                break;
                            }
                        }
                    }
                }
            }
            
            process.waitFor();
            
            if (containerVolumeName != null && !containerVolumeName.isEmpty()) {
                return containerVolumeName;
            }
            
            return "Macintosh HD";
        } catch (Exception e) {
            return "Macintosh HD";
        }
    }

    private void clearInputs() {
        rowNumberField.setText("");
        newLineField.setText("");
        swapRowsField.setText("");
        saveAsField.setText("");
        matchStringsField.setText("");
        matchTargetsField.setText("");
        modifiedLines = null;
    }

    private void addMultipleRows(File directory, int startRow) {
        File[] audioFiles = directory.listFiles((dir, name) -> 
            name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav") ||
            name.endsWith(".aiff") || name.endsWith(".flac")
        );
        
        if (audioFiles == null || audioFiles.length == 0) {
            outputArea.setText("No audio files found in the selected directory.");
            return;
        }
        
        // Sort files alphabetically
        java.util.Arrays.sort(audioFiles);
        
        String originalPath = filePathField.getText().trim();
        if (originalPath.isEmpty()) {
            outputArea.setText("Please select a playlist file first.");
            return;
        }
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(originalPath));
            List<String> contentList = new ArrayList<>();
            
            // Extract content from existing rows
            for (String line : lines) {
                int commaIndex = line.indexOf(',');
                if (commaIndex != -1) {
                    String content = line.substring(commaIndex + 1).trim();
                    contentList.add(content);
                } else {
                    contentList.add(line);
                }
            }
            
            // Validate starting row
            if (startRow < 0 || startRow > contentList.size()) {
                outputArea.setText("Starting row number out of range. Valid range: 0 to " + contentList.size());
                return;
            }
            
            // Add all audio files starting at startRow
            for (File audioFile : audioFiles) {
                String contentOnly = prepareAudioFileContent(audioFile);
                if (contentOnly != null) {
                    contentList.add(startRow, contentOnly);
                    startRow++;
                }
            }
            
            // Create preview with sequential numbering
            List<String> previewLines = new ArrayList<>();
            for (int i = 0; i < contentList.size(); i++) {
                previewLines.add(i + ", " + contentList.get(i));
            }
            
            modifiedLines = previewLines;
            outputArea.setText(String.join("\n", previewLines));
            logChange("Preview: " + audioFiles.length + " audio files added starting at row " + (startRow - audioFiles.length));
        } catch (IOException ex) {
            outputArea.setText("Error processing directory: " + ex.getMessage());
        }
    }
    
    private String prepareAudioFileContent(File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            System.err.println("Invalid path: " + file.getName());
            return null;
        }
        
        String volumePrefix;
        String relativePath;
        
        if (canonicalPath.startsWith("/Volumes/")) {
            String remainder = canonicalPath.substring("/Volumes/".length());
            int slashIndex = remainder.indexOf('/');
            if (slashIndex != -1) {
                String volumeName = remainder.substring(0, slashIndex);
                relativePath = remainder.substring(slashIndex);
                volumePrefix = volumeName + ":";
            } else {
                return null;
            }
        } else {
            String detectedVolumeName = getVolumeNameFromPath(canonicalPath);
            volumePrefix = detectedVolumeName + ":";
            relativePath = canonicalPath;
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

    private void clearPreview() {
        modifiedLines = null;
        outputArea.setText("Preview cleared.");
        logChange("Preview cleared.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new APEGUI().setVisible(true));
    }
}
