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

        // IMPORTANT: keep prompt SIMPLE (no nested JSON, no heavy escaping)
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
                
          You are a deterministic JSON extractor.
                
                  You are NOT an assistant.
                
                  You MUST output ONLY valid JSON.
                  No text before or after.
                  No markdown.
                  No explanation.
                  No greetings.
                
                  If input is ambiguous, still output best guess JSON only.
                
                  Output rule: the first character must be { and the last must be }
                        Anything else = failure.
                        
           Filename:
        """+ filename;
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

            JsonNode json = mapper.readTree(raw);
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

            JsonNode json = mapper.readTree(raw);
            return json.has("songTitle") ? json.get("songTitle").asText() : "UNKNOWN";

        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}