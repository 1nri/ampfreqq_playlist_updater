import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MetadataFormatterSmartMountAware {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. Ask for sequence number
        System.out.print("Enter sequence number (e.g., 94): ");
        int number = scanner.nextInt();
        scanner.nextLine(); // clear line break

        // 2. Ask for file path
        System.out.print("Enter full file path: ");
        String inputPath = scanner.nextLine().trim();

        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            System.err.println("Error: File not found at path: " + inputPath);
            return;
        }

        String canonicalPath;
        try {
            canonicalPath = inputFile.getCanonicalPath(); // reveals mount points and symlinks
        } catch (IOException e) {
            System.err.println("Invalid path.");
            return;
        }

        String volumePrefix;
        String relativePath;

        if (canonicalPath.startsWith("/Volumes/")) {
            // e.g., /Volumes/mbadrive/Users/henri/Music/song.aiff
            String remainder = canonicalPath.substring("/Volumes/".length()); // mbadrive/Users/...
            int slashIndex = remainder.indexOf('/');
            if (slashIndex != -1) {
                String volumeName = remainder.substring(0, slashIndex);
                relativePath = remainder.substring(slashIndex); // e.g., /Users/henri/...
                volumePrefix = volumeName + ":";
            } else {
                System.err.println("Error: Path does not have directory structure after volume name.");
                return;
            }
        } else {
            // assumed root volume
            volumePrefix = getRootVolumeName();
            relativePath = canonicalPath;
        }

        // 3. Extract file display name without extension
        String fileName = inputFile.getName();
        String displayName = fileName.replaceFirst("\\.[^.]+$", "");

        // 4. Build final row
        String result = String.format(
            "%d, \"%s%s\" \"%s\";",
            number,
            volumePrefix,
            relativePath,
            displayName
        );

        System.out.println("Generated string:");
        System.out.println(result);
    }

    // macOS: use root volume by default
    private static String getRootVolumeName() {
        return "Macintosh HD:";
    }
}