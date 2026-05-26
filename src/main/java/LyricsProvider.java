public interface LyricsProvider {

    String getLyrics(String artist, String title);

    String getProviderName();

}