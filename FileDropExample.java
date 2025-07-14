import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class FileDropExample {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Raahaa tiedosto tähän");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 150);

            // Tiedoston nimen ja polun vastaanottava tekstikenttä
            JTextField filePathField = new JTextField("Pudota tiedosto tähän");
            filePathField.setEditable(false);
            filePathField.setFont(new Font("Monospaced", Font.PLAIN, 14));
            filePathField.setHorizontalAlignment(JTextField.CENTER);

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
            });

            frame.setLayout(new BorderLayout());
            frame.add(filePathField, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}