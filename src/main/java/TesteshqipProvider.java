import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TeksteshqipProvider implements LyricsProvider {

    private static final String BASE_URL = "https://teksteshqip.com";
    private static final String SEARCH_URL =
            "https://teksteshqip.com/api/modals/search/top.php?model=song_archive&page=0&search=";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getProviderName() {
        return "TeksteShqip";
    }

    @Override
    public String getLyrics(String artist, String title) {

        try {


            return lyrics;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract lyrics from TeksteShqip song page.
     */
    private String extractLyrics(Document doc) {


        return lyricsElement.text().trim();
    }
}