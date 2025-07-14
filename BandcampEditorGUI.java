import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BandcampEditorGUI extends JFrame {

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

    private List<String> modifiedLines = null;
    private final StringBuilder changeLog = new StringBuilder();

    /* This is the version to work on, ignore the other branch */
// Constructor to set up the GUI

    public BandcampEditorGUI() {
        setTitle("Bandcamp Tiedoston Muokkaus");
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

        insertButton = new JButton("Lisää rivi");
        browseButton = new JButton("Selaa...");
        previewButton = new JButton("Esikatsele muutokset");
        swapButton = new JButton("Vaihda rivien paikkaa");
        swapByContentButton = new JButton("Vaihda rivit sisällön perusteella");
        saveButton = new JButton("Tallenna muutokset");
        clearPreviewButton = new JButton("Tyhjennä esikatselu");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        panel.add(new JLabel("Tiedoston polku:"));
        panel.add(filePathField);
        panel.add(browseButton);

        panel.add(new JLabel("Rivin numero (lisäys):"));
        panel.add(rowNumberField);
        panel.add(new JLabel());

        panel.add(new JLabel("Lisättävä rivi (ilman numerointia):"));
        panel.add(newLineField);
        panel.add(insertButton);

        panel.add(new JLabel("Rivit vaihdettavaksi (esim. 3,7):"));
        panel.add(swapRowsField);
        panel.add(swapButton);

        panel.add(new JLabel("Vaihda rivit sisällön mukaan (merkkijonot):"));
        panel.add(matchStringsField);
        panel.add(new JLabel("merkkijonot pilkulla eroteltuna"));

        panel.add(new JLabel("Vastaavat kohderivit (numerot tai merkkijonot):"));
        panel.add(matchTargetsField);
        panel.add(swapByContentButton);

        panel.add(new JLabel("Uusi tiedostonimi (valinnainen):"));
        panel.add(saveAsField);
        panel.add(saveButton);

        panel.add(new JLabel());
        panel.add(previewButton);
        panel.add(clearPreviewButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        insertButton.addActionListener(e -> insertLine());
        browseButton.addActionListener(e -> browseFile());
        previewButton.addActionListener(e -> previewChanges());
        swapButton.addActionListener(e -> swapLines());
        swapByContentButton.addActionListener(e -> swapLinesByContent());
        saveButton.addActionListener(e -> saveChanges());
        clearPreviewButton.addActionListener(e -> clearPreview());
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
            outputArea.setText("Lokitiedoston tallennus epäonnistui: " + e.getMessage());
        }
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            logChange("Tiedosto valittu: " + selectedFile.getName());
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
            outputArea.setText("Virheellinen rivinumero.");
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
            logChange("Esikatselu: rivi lisätty kohtaan " + insertAt);
        } catch (IOException ex) {
            outputArea.setText("Virhe tiedoston käsittelyssä: " + ex.getMessage());
        }
    }

    private void insertLine() {
        outputArea.setText("Käytä ensin esikatselutoimintoa ja sen jälkeen 'Tallenna muutokset'.");
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
                outputArea.setText("Anna vähintään kaksi rivinumeroa erotettuna pilkulla.");
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
            logChange("Rivien paikat vaihdettu: " + swapInput);
        } catch (Exception e) {
            outputArea.setText("Virhe rivien vaihdossa: " + e.getMessage());
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
                    outputArea.setText("Merkkijonoa ei löytynyt: " + key.trim());
                    return;
                }
                matches.put(key.trim(), foundIndex);
            }

            StringBuilder confirmation = new StringBuilder("Vahvista seuraavat löydöt:\n");
            for (String key : matches.keySet()) {
                confirmation.append("[" + key + "] => rivi " + matches.get(key) + "\n");
            }

            int result = JOptionPane.showConfirmDialog(this, confirmation.toString(), "Vahvista vaihto", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                outputArea.setText("Toiminto peruttu.");
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
            logChange("Rivien paikat vaihdettu sisällön perusteella: " + matchStringsField.getText());
        } catch (IOException e) {
            outputArea.setText("Virhe rivien vaihdossa sisällön perusteella: " + e.getMessage());
        }
    }

    private void saveChanges() {
        String saveName = saveAsField.getText().trim();
        String originalPath = filePathField.getText().trim();

        if (modifiedLines == null) {
            outputArea.setText("Ei muutoksia tallennettavaksi. Tee ensin esikatselu tai vaihto.");
            return;
        }

        try {
            Path original = Paths.get(originalPath);
            Path target = saveName.isEmpty() ? original : original.resolveSibling(saveName);
            Files.write(target, modifiedLines);
            saveLogAutomatically(original);
            outputArea.setText("Muutokset tallennettu tiedostoon: " + target);
            logChange("Tallennettu tiedostoon: " + target.getFileName());
        } catch (IOException e) {
            outputArea.setText("Virhe tallennuksessa: " + e.getMessage());
        }
    }

    private void clearPreview() {
        modifiedLines = null;
        outputArea.setText("Esikatselu tyhjennetty.");
        logChange("Esikatselu tyhjennetty.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BandcampEditorGUI().setVisible(true));
    }
}
