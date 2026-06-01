import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.*;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
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

            Connection.Response res = Jsoup.connect(SEARCH_URL + LyricsValidator.normalize(title))
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


            // 3. Parse JSON
            JsonNode root = mapper.readTree(res.body());
            JsonNode items = root.get("items");

            if (items == null || !items.isArray() || items.isEmpty()) {
                return null;
            }
            JsonNode bestItem = null;
            double bestScore = 0;

            for (JsonNode item : items) {
                String candidateName = item.get("name").asText();

                double score = LyricsValidator.similarity(
                        LyricsValidator.normalize(candidateName),
                        LyricsValidator.normalize(title + " - " + artist)
                );

                if (score > bestScore) {
                    bestScore = score;
                    bestItem = item;
                }
            }

            if (bestItem == null || bestScore < 0.8) {
                boolean additional_logic = false;
                if(additional_logic){
                double bestScore_i = 0;
                for (JsonNode item : items) {
                    String[] parts = item.get("name").asText().split(" - ", 2);

                    if (parts.length < 2) {
                        continue;
                    }
                    String title_i = parts[0];
                    String artist_i = parts[1];
                    double score_title = LyricsValidator.similarity(
                            LyricsValidator.normalize(title),
                            LyricsValidator.normalize(title_i));

                    double score_artist = LyricsValidator.similarity(
                            LyricsValidator.normalize(artist),
                            LyricsValidator.normalize(artist_i));
                 //   double score_i=
                    if((score_title>0.9)&&(score_artist>0.6)){
                        double score_i=score_title+0.5*score_artist;
                        if (score_i>bestScore_i){
                            bestScore_i = score_i;
                            bestItem = item;
                        }

                    }
                }}
                if (bestItem == null) {
                    return null;
                }
            }

            String link = bestItem.get("link").asText();

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
                "div.clCl1",
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
        StringBuilder sb = new StringBuilder();

        for (Node node : lyricsElement.childNodes()) {
            if (node instanceof TextNode) {
                sb.append(((TextNode) node).text());
            }
            else if (node instanceof Element) {
                Element el = (Element) node;

                String tag = el.tagName();

                if (tag.equals("br")) {
                    sb.append("\n");
                } else if (tag.equals("p") || tag.equals("div")) {
                    sb.append("\n");
                    sb.append(el.text());
                    sb.append("\n");
                } else {
                    sb.append(el.text());
                }
            }
        }

        String lyrics = sb.toString();

        // 5. Cleanup formatting
        lyrics = lyrics
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        // 6. Basic validation (avoid garbage extraction)
        if (lyrics.length() < 30) return null;
        return lyrics;
    }
}