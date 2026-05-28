import java.util.List;

public class LyricsService {

    private final AIService aiService;

  private final List<LyricsProvider> providers = List.of(
            new TeksteshqipProvider()
   );

    public LyricsService(AIService aiService) {
        this.aiService = aiService;
    }

    public SongMetadata getOrFetchMetadata(String folderPath, String filename) {

        if (!SongRepository.hasMetadata(filename)) {
            System.out.println("No metadata found → calling AI");
            boolean useEmbeddedTags = false;
            SongMetadata metadata = getMetadata(
                    folderPath,
                    filename,
                    aiService,
                    useEmbeddedTags
            );
            //SongMetadata metadata = aiService.extractMetadata(filename);

            System.out.println("AI result: Artist:" + metadata.getArtist() + "   Title:"+metadata.getTitle());

            // store metadata
            SongRepository.insertOrUpdate(
                    filename,
                    metadata.getArtist(),
                    metadata.getTitle()
            );

            return metadata;
        }

        return SongRepository.getMetadata(filename);
    }



    public void downloadLyricsIfNeeded(String filename, SongMetadata metadata) {

        if (SongRepository.isDownloaded(filename)) {
            System.out.println("Lyrics already downloaded");
            return;
        }

        for (LyricsProvider provider : providers) {

            try {
                String lyrics = provider.getLyrics(metadata.getArtist(), metadata.getTitle());

                if (lyrics != null && !lyrics.isEmpty()) {
                    SongRepository.saveLyrics(filename, lyrics);
                    SongRepository.markDownloaded(filename);

                    System.out.println("Lyrics downloaded using: " + provider.getProviderName());
                    return;
                }

            } catch (Exception e) {
                System.out.println("Provider failed: " + provider.getProviderName());
            }
        }

        System.out.println("All providers failed ❌");
    }

    private static SongMetadata getMetadata(
            String folderPath,
            String fileName,
            AIService aiService,
            boolean useEmbeddedTags
    ) {

        if (useEmbeddedTags) {

            EmbeddedMetadataReader reader = new EmbeddedMetadataReader();

            SongMetadata metadata = reader.getMetadata(folderPath + "/"+fileName);

            if (metadata != null) {
                return metadata;
            }
        }

        return aiService.extractMetadata(fileName);
    }

}