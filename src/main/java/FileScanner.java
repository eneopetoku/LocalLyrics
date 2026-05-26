import java.io.File;
import java.util.function.Consumer;

public class FileScanner {

    public void scan(String folderPath, Consumer<String> fileHandler) {

        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();

        if (files == null) return;

        for (File file : files) {

            if (file.isFile()) {
                fileHandler.accept(file.getName());
            }
        }
    }
}