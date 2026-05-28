import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.jsoup.Connection;

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
   
            Connection.Response res = Jsoup.connect(SEARCH_URL + title)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Language", "en-US,en;q=0.9,al;q=0.8")
                    .header("Referer", "https://teksteshqip.com/")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(10_000)
                    .execute();

            if (res.statusCode() != 200) {
                System.out.println("Search API blocked: " + res.statusCode());
                return null;
            }

            if (res.statusCode() != 200) {
                System.out.println("Search API blocked: " + res.statusCode());
                return null;
            }

            // 3. Parse JSON
            JsonNode root = mapper.readTree(res.body());
            JsonNode items = root.get("items");

            if (items == null || !items.isArray() || items.isEmpty()) {
                return null;
            }

            // 4. Take first result
            String link = items.get(0).get("link").asText();

            // 5. Build song URL
            String songUrl = BASE_URL + "/" + link;

            // 6. Fetch song page
            Document doc = Jsoup.connect(songUrl)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                    .referrer("https://teksteshqip.com/")
                    .timeout(10_000)
                    .get();

            // 7. Extract lyrics
            return extractLyrics(doc);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract lyrics from TeksteShqip song page.
     * NOTE: You may need to adjust selector depending on HTML structure.
     */
    private String extractLyrics(Document doc) {

        if (doc == null) return null;

        // 1. Try common known containers first (TeksteShqip + similar sites)
        String[] selectors = new String[] {
                "div#lyrics",
                "div.lyrics",
                "div#songLyrics",
                "div#teksti",
                "div[class*=lyrics]",
                "div[class*=tekst]",
                "article",
                "div.post-content"
        };

        Element lyricsElement = null;

        for (String selector : selectors) {
            lyricsElement = doc.selectFirst(selector);
            if (lyricsElement != null) break;
        }

        // 2. Fallback: look for largest text block (very useful for messy pages)
        if (lyricsElement == null) {
            Element body = doc.body();
            if (body == null) return null;

            lyricsElement = body;
        }

        // 3. Remove noise elements
        lyricsElement.select("script, style, nav, header, footer, form, ads, .advertisement").remove();

        // 4. Preserve line breaks properly
        String lyrics = lyricsElement.html()
                .replaceAll("(?i)<br[^>]*>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("<p[^>]*>", "\n");

        // 5. Strip remaining HTML
        lyrics = Jsoup.parse(lyrics).text();

        // 6. Cleanup formatting
        lyrics = lyrics
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        // 7. Basic validation (avoid garbage extraction)
        if (lyrics.length() < 30) return null;

        return lyrics;
    }
}