public class Main {

    public static void main(String[] args) {

        // 1. Initialize database (creates table if needed)
        Database.init();

        // 2. Folder containing music files
        String folderPath = "/home/eneo/mysoftwarefiles/mytestfiles/Youtube";

        FileScanner scanner = new FileScanner();
        AIService aiService = new AIService();

        scanner.scan(folderPath, filename -> {

            System.out.println("Processing: " + filename);

            String artist;
            String title;

            // 3. Check if metadata already exists in DB
            if (!SongRepository.hasMetadata(filename)) {

                System.out.println("No metadata found → calling AI");

                SongMetadata result_metadata = aiService.extractMetadata(filename);

                System.out.println("AI result: Artist:" + result_metadata.getArtist() + "   Title:"+result_metadata.getTitle());



                // store metadata
                SongRepository.insertOrUpdate(
                        filename,
                        result_metadata.getArtist(),
                        result_metadata.getTitle()
                );
            } else {

                System.out.println("Metadata already exists → loading from DB");

                // optional: you could fetch from DB instead of recomputing
                artist = null;
                title = null;
            }

            // 4. Lyrics stage (future use)
            if (!SongRepository.isDownloaded(filename)) {
                System.out.println("Lyrics not downloaded yet → ready for next step");
                // TODO: fetch lyrics here later
            } else {
                System.out.println("Lyrics already downloaded");
            }

            System.out.println("-------------------");
        });
    }
}