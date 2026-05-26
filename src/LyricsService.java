import java.util.List;

public class LyricsService {

    private final AIService aiService;

    private final List<LyricsProvider> providers = List.of(
            new TeksteshqipProvider()
    );

    public LyricsService(AIService aiService) {
        this.aiService = aiService;
    }

    public SongMetadata getOrFetchMetadata(String filename) {

        if (!SongRepository.hasMetadata(filename)) {
            System.out.println("No metadata found → calling AI");
            SongMetadata metadata = aiService.extractMetadata(filename);

            System.out.println("AI result: Artist:" + metadata.getArtist() + "   Title:"+metadata.getTitle());

            // store metadata
            SongRepository.insertOrUpdate(
                    filename,
                    metadata.getArtist(),
                    metadata.getTitle()
            );

            return metadata;
        }

        return SongRepository.getMetadata(filename); // better than null
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
}