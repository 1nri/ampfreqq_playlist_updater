
// Täydellinen Java-lähdekoodi ominaisuuksineen: GUI, CLI, lokitus, drag & drop jne.
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BandcampEditorGUI {
    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private static File currentFile = null;
    private static File logFile = null;
    private static JFrame frame;

    public static void main(String[] args) {
        if (args.length >= 3) {
            handleCLI(args);
        } else {
            SwingUtilities.invokeLater(BandcampEditorGUI::createAndShowGUI);
        }
    }

    private static void handleCLI(String[] args) {
        try {
            Path filePath = Paths.get(args[0]);
            List<String> lines = Files.readAllLines(filePath);
            int insertIndex = Integer.parseInt(args[2]) - 1;

            String newPath = args[1];
            Path newFile = Paths.get(newPath);
            String newLine = ""mbadrive:" + newFile.toString() + "" "" + newFile.getFileName().toString() + """;

            lines.add(insertIndex, newLine);
            Files.write(filePath, lines);

            System.out.println("Lisätty rivi: " + newLine);
        } catch (Exception e) {
            System.err.println("Virhe komentoriviltä: " + e.getMessage());
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Bandcamp Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new BorderLayout());
        JList<String> list = new JList<>(listModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        new DropTarget(list, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        if (file.isDirectory()) {
                            Files.walk(file.toPath())
                                 .filter(Files::isRegularFile)
                                 .forEach(p -> listModel.addElement(makeLineFromFile(p.toFile())));
                        } else {
                            listModel.addElement(makeLineFromFile(file));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton saveBtn = new JButton("Tallenna");
        saveBtn.addActionListener(e -> {
            if (currentFile == null) {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    currentFile = fc.getSelectedFile();
                } else return;
            }
            saveToFile(currentFile);
        });

        JButton openBtn = new JButton("Avaa");
        openBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                openFile(fc.getSelectedFile());
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openBtn);
        buttonPanel.add(saveBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    private static String makeLineFromFile(File file) {
        return ""mbadrive:" + file.getAbsolutePath() + "" "" + file.getName() + """;
    }

    private static void openFile(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            listModel.clear();
            for (String line : lines) listModel.addElement(line);
            currentFile = file;
            logFile = new File(file.getParent(), "muutosloki_" + file.getName() + ".log");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Virhe tiedostoa avattaessa: " + e.getMessage());
        }
    }

    private static void saveToFile(File file) {
        try {
            List<String> oldLines = Files.exists(file.toPath())
                ? Files.readAllLines(file.toPath())
                : new ArrayList<>();
            List<String> newLines = Collections.list(listModel.elements());

            Files.write(file.toPath(), newLines);

            if (logFile != null) {
                List<String> log = new ArrayList<>();
                log.add("TALLENNUS " + LocalDateTime.now());
                log.add("VANHA:");
                log.addAll(oldLines);
                log.add("UUSI:");
                log.addAll(newLines);
                log.add("----------");
                Files.write(logFile.toPath(), log, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Virhe tallennettaessa: " + e.getMessage());
        }
    }
}
