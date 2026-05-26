public class SongMetadata {

    private String artist;
    private String title;

    public SongMetadata() {
    }

    public SongMetadata(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "SongMetadata{" +
                "artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}