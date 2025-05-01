import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Updater {
    public static void main(String[] args) {
        // Example usage
        String filePath = "/path/to/your/file.txt";
        String contentToInsert = "This is the content to insert.";

        try {
            String content = readFile(filePath);
            System.out.println("Original Content:");
            System.out.println(content);

            String modifiedContent = operateContent(content, contentToInsert);
            System.out.println("Modified Content:");
            System.out.println(modifiedContent);

            writeFile(filePath, modifiedContent);
            System.out.println("Content written to the file successfully.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    public static void writeFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }

    public static String operateContent(String content, String contentToInsert) {
        // Example format: Insert the new content at the beginning with a separator
        String separator = "\n--- New Content ---\n";
        return contentToInsert + separator + content;
    }
}