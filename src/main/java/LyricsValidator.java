public class LyricsValidator {
    /**
            * Normalize text for comparisons.
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

}
