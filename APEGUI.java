import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
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
    private JButton previewContentButton;
    private JTextArea outputArea;
    private JTextArea logArea;
    private String absolutePath;

    /* This is not functional as of yet
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Drag and drop example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            // Otetaan käyttöön raahaus ja pudotus
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
        previewContentButton = new JButton("Preview Content");

        filePathField.setDragEnabled(true);
        newLineField.setDragEnabled(true);

        filePathField.setEditable(false);
        filePathField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        filePathField.setHorizontalAlignment(JTextField.CENTER);
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
                        filePathField.setText("Virhe tiedoston lukemisessa");
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

        panel.add(previewContentButton);
        panel.add(previewButton);
        panel.add(clearPreviewButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
        absolutePath = "";

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
                    int number = 0; // default number
                    File file = (File) object.get(0);
                    String canonicalPath;
                    try {
                        canonicalPath = file.getCanonicalPath(); // reveals mount points and symlinks
                        } catch (IOException e) {
                            System.err.println("Virheellinen polku.");
                            return;
                        }
                        String volumePrefix;
                        String relativePath;
                        
                        if(file.getName().endsWith(".mp3") || file.getName().endsWith(".m4a") ||  file.getName().endsWith(".wav") 
                        || file.getName().endsWith(".aiff") || file.getName().endsWith(".flac"))
                        {
                            
                            try {
                                absolutePath = file.getCanonicalPath(); // resolves symlinks and gets the absolute path
                                } 
                                catch (IOException e) {
                                    System.err.println("Incorrect path.");
                                    return;
                                }}
                                else{
                            outputArea.setText("Please drop a supported audio file.");
                            return;
                        }

                                    
                // 3. Determine the of Volume (volumename)

                volumePrefix = "";

                if (absolutePath.startsWith("/Volumes/")) {

                        // esim: /Volumes/mbadrive/Users/henri/Music/song.aiff
                    String remainder = canonicalPath.substring("/Volumes/".length()); // mbadrive/Users/...
                        int slashIndex = remainder.indexOf('/');
                        if (slashIndex != -1) {
                            String volumeName = remainder.substring(0, slashIndex);
                            relativePath = remainder.substring(slashIndex); // esim. /Users/henri/...
                            volumePrefix = volumeName + ":";
                        } else {
                            System.err.println("Virhe: Polussa ei ole kansiorakennetta levynimen jälkeen.");
                            return;
                        }
                    } else {
                        // oletetaan root-levy
                        volumePrefix = getRootVolumeName();
                        relativePath = canonicalPath;
                    }

                        // 4. Determine display name for given file
                        String fileName = file.getName(); 
                        String displayName = fileName.replaceFirst("\\.[^.]+$", "");

                        // 5. Format the result string

                        String result = String.format(
                            "%d, \"%s%s\" \"%s\";",
                            number,
                            volumePrefix,
                            absolutePath,
                            displayName
                            );

                            // 6. Print the result
                            System.out.println("Muodostettu merkkijono:");
                            System.out.println(result);
                            // newLineField.setText(result);
                            newLineField.setText(file.getAbsolutePath());
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
        previewContentButton.addActionListener(e -> previewContent());
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
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            logChange("File selected: " + selectedFile.getName());
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
            List<String> previewLines = new ArrayList<>();

            for (int i = 0; i <= lines.size(); i++) {
                if (i == insertAt) {
                    previewLines.add(insertAt + ", " + newContent);
                }
                if (i < lines.size()) {
                    String line = lines.get(i);
                    int commaIndex = line.indexOf(',');
                    if (commaIndex != -1) {
                        String content = line.substring(commaIndex + 1).trim();
                        previewLines.add((i >= insertAt ? i + 1 : i) + ", " + content);
                    } else {
                        previewLines.add(line);
                    }
                }
            }

            modifiedLines = previewLines;
            outputArea.setText(String.join("\n", previewLines));
            logChange("Preview: Row added to " + insertAt);
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
            List<Integer> indices = Arrays.stream(swapInput.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (indices.size() < 2) {
                outputArea.setText("You need to provide at least two numbers separated by a comma.");
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(filePath));
            Map<Integer, String> contentMap = new HashMap<>();

            for (int i : indices) {
                String line = lines.get(i);
                int commaIndex = line.indexOf(',');
                contentMap.put(i, line.substring(commaIndex + 1).trim());
            }

            Collections.rotate(indices, 1);

            for (int i = 0; i < indices.size(); i++) {
                int index = indices.get(i);
                lines.set(index, index + ", " + contentMap.get(indices.get((i + 1) % indices.size())));
            }

            modifiedLines = lines;
            outputArea.setText(String.join("\n", lines));
            logChange("Swapped the rows: " + swapInput);
        } catch (Exception e) {
            outputArea.setText("Failed swapping the rows: " + e.getMessage());
        }
    }

    private void swapLinesByContent() {
        String filePath = filePathField.getText().trim();
        String[] keys = matchStringsField.getText().split(",");
        String[] targets = matchTargetsField.getText().split(",");

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            Map<String, Integer> matches = new HashMap<>();

            for (String key : keys) {
                int foundIndex = -1;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains(key.trim())) {
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

            StringBuilder confirmation = new StringBuilder("Confirm the following results:\n");
            for (String key : matches.keySet()) {
                confirmation.append("[" + key + "] => row " + matches.get(key) + "\n");
            }

            int result = JOptionPane.showConfirmDialog(this, confirmation.toString(), "Confirm swap", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                outputArea.setText("Canceled.");
                return;
            }

            List<Integer> indices = new ArrayList<>(matches.values());
            Collections.rotate(indices, 1);
            for (int i = 0; i < keys.length; i++) {
                int index = matches.get(keys[i].trim());
                int commaIndex = lines.get(index).indexOf(',');
                String newContent = lines.get(indices.get(i)).substring(commaIndex + 1).trim();
                lines.set(index, index + ", " + newContent);
            }

            modifiedLines = lines;
            outputArea.setText(String.join("\n", lines));
            logChange("Swapped the following rows based on content: " + matchStringsField.getText());
        } catch (IOException e) {
            outputArea.setText("Failed swapping the rowsbased on the following content: " + e.getMessage());
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

    // Determines name for root disk
    private String getRootVolumeName() {
        File root = new File("/");
        File[] roots = File.listRoots();

        for (File f : roots) {
            try {
                if (f.getCanonicalPath().equals(root.getCanonicalPath())) {
                    // "/" is this root, so we can use its name
                    String[] pathParts = f.getAbsolutePath().split("/");
                    if (pathParts.length > 0) {
                        // Return default root name
                        return "Macintosh HD:";
                    }
                }
            } catch (IOException e) {
                outputArea.setText("Couldn't read the file");
            }
        }

        // default root name if nothing else is found
        return "root:";
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
