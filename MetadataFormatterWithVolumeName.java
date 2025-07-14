import java.io.*;
import java.util.Scanner;

public class MetadataFormatterWithVolumeName {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Anna järjestysnumero (esim. 94): ");
        int number = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Anna tiedoston koko polku: ");
        String inputPath = scanner.nextLine().trim();
        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            System.err.println("Virhe: Tiedostoa ei löydy.");
            return;
        }

        String canonicalPath;
        try {
            canonicalPath = inputFile.getCanonicalPath();
        } catch (IOException e) {
            System.err.println("Virheellinen polku.");
            return;
        }

        String mountPoint = getMountPoint(inputFile);
        if (mountPoint == null) {
            System.err.println("Mount-pisteen tunnistus epäonnistui.");
            return;
        }

        String volumeName = getVolumeName(mountPoint);
        if (volumeName == null || volumeName.isEmpty()) {
            volumeName = "Tuntematon Levy";
        }

        // Muodosta suhteellinen polku
        String relativePath = canonicalPath.substring(mountPoint.length());
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }

        String fileName = inputFile.getName();
        String displayName = fileName.replaceFirst("\\.[^.]+$", "");

        String result = String.format(
            "%d, \"%s:%s\" \"%s\";",
            number,
            volumeName,
            relativePath,
            displayName
        );

        System.out.println("Muodostettu merkkijono:");
        System.out.println(result);
    }

    // Suorittaa df ja hakee mount-pisteen
    private static String getMountPoint(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder("df", file.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean foundHeader = false;

                while ((line = reader.readLine()) != null) {
                    if (!foundHeader) {
                        foundHeader = true;
                        continue; // skip header
                    }

                    String[] tokens = line.trim().split("\\s+");
                    if (tokens.length >= 6) {
                        return tokens[5]; // mount point
                    }
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Virhe df-komennossa: " + e.getMessage());
        }

        return null;
    }

    // Suorittaa diskutil info ja hakee volume name
    private static String getVolumeName(String mountPoint) {
        try {
            ProcessBuilder pb = new ProcessBuilder("diskutil", "info", mountPoint);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("Volume Name:")) {
                        return line.substring(line.indexOf(":") + 1).trim();
                    }
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Virhe diskutil-komennossa: " + e.getMessage());
        }

        return null;
    }
}