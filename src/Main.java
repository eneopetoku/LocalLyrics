
public class Main {

    public static void main(String[] args) {

        // 1. Initialize database (creates table if needed)
        Database.init();

        // 2. Folder containing music files
        String folderPath = "/home/eneo/mysoftwarefiles/mytestfiles/Youtube";

        FileScanner scanner = new FileScanner();
        AIService aiService = new AIService();
        LyricsService lyricsService = new LyricsService(aiService);
        
        scanner.scan(folderPath, filename -> {
            System.out.println("Processing: " + filename);

            SongMetadata metadata = lyricsService.getOrFetchMetadata(filename);

            lyricsService.downloadLyricsIfNeeded(filename, metadata);

            System.out.println("-------------------");
        });
    }
}