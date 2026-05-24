import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class AIService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama2:13b";

    private final HttpClient client = HttpClient.newHttpClient();

    public SongMetadata extractMetadata(String filename) {

        try {
            String prompt = buildPrompt(filename);

            String jsonBody = buildRequestJson(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // Debug (keep for now)
            System.out.println("OLLAMA RESPONSE:");
            System.out.println(response.body());

            String result = response.body();
            SongMetadata metadata = new SongMetadata();
            metadata.setArtist(extractArtist(result));
            metadata.setTitle(extractTitle(result));

            return metadata;


        } catch (Exception e) {
            e.printStackTrace();

            // IMPORTANT FIX: return valid object, not String
            return new SongMetadata("UNKNOWN", "UNKNOWN");
        }
    }

    private String buildPrompt(String filename) {

        return """
You are a music filename parser.

Extract ONLY:
1. artist
2. song title

Ignore:
- file extensions
- "official video"
- "lyrics"
- "HD"
- "feat"
- uploader names
- years
- emojis
- extra tags
- ANY text inside parentheses ( ... )
- ANY text inside brackets [ ... ]
- ANY text inside braces { ... }

You are a deterministic JSON extractor.

You are NOT an assistant.

You MUST output ONLY valid JSON.
No text before or after.
No markdown.
No explanation.
No greetings.

If input is ambiguous, still output best guess JSON only.

Output rule: the first character must be { and the last must be }

Filename:
""" + filename;
    }

    private String buildRequestJson(String prompt) {

        // minimal safe escaping
        String safePrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ");

        return """
        {
          "model": "%s",
          "prompt": "%s",
          "stream": false
        }
        """.formatted(MODEL, safePrompt);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private String extractArtist(String responseBody) {
        try {
            // Ollama often wraps output like:
            // { "response": "{...json...}" }
            JsonNode root = mapper.readTree(responseBody);

            String raw = root.has("response")
                    ? root.get("response").asText()
                    : responseBody;
            String jsonOnly = extractJsonObject(raw);
            JsonNode json = mapper.readTree(jsonOnly);
            System.out.println("extractArtist-jsonNode: " + json);
            return json.has("artist") ? json.get("artist").asText() : "UNKNOWN";

        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String extractTitle(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);

            String raw = root.has("response")
                    ? root.get("response").asText()
                    : responseBody;
            String jsonOnly = extractJsonObject(raw);
            JsonNode json = mapper.readTree(jsonOnly);
            System.out.println("extractTitle-jsonNode: "+json);
            return json.has("songTitle") ? json.get("songTitle").asText() : json.has("song_title") ? json.get("song_title").asText():"UNKNOWN";

        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
    private String extractJsonObject(String text) throws Exception {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("No JSON found in response");
        }

        return text.substring(start, end + 1);
    }
}