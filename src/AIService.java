import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama2:13b";

    private final HttpClient client = HttpClient.newHttpClient();

    public String extractMetadata(String filename) {

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

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
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
                
                        Return the result STRICTLY in this format:
                
                        ARTIST: <artist>
                        TITLE: <title>
                
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
}