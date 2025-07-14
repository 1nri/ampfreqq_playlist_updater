import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MetadataFormatterSmartMountAware {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. Kysy järjestysnumero
        System.out.print("Anna järjestysnumero (esim. 94): ");
        int number = scanner.nextInt();
        scanner.nextLine(); // tyhjennä rivinvaihto

        // 2. Kysy tiedoston polku
        System.out.print("Anna tiedoston koko polku: ");
        String inputPath = scanner.nextLine().trim();

        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            System.err.println("Virhe: Tiedostoa ei löydy polusta: " + inputPath);
            return;
        }

        String canonicalPath;
        try {
            canonicalPath = inputFile.getCanonicalPath(); // paljastaa mounttipisteet ja symlinkit
        } catch (IOException e) {
            System.err.println("Virheellinen polku.");
            return;
        }

        String volumePrefix;
        String relativePath;

        if (canonicalPath.startsWith("/Volumes/")) {
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

        // 3. Poimi tiedoston esitysnimi ilman päätettä
        String fileName = inputFile.getName();
        String displayName = fileName.replaceFirst("\\.[^.]+$", "");

        // 4. Muodosta lopullinen rivi
        String result = String.format(
            "%d, \"%s%s\" \"%s\";",
            number,
            volumePrefix,
            relativePath,
            displayName
        );

        System.out.println("Muodostettu merkkijono:");
        System.out.println(result);
    }

    // MacOS: käytetään oletuksena root-levyä
    private static String getRootVolumeName() {
        return "Macintosh HD:";
    }
}