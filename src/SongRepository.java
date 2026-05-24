import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SongRepository {

    public static void insertOrUpdate(String filename, String artist, String title) {
        String sql = """
            INSERT INTO songs (filename, artist, title, lyrics_downloaded)
            VALUES (?, ?, ?, 0)
            ON CONFLICT(filename) DO UPDATE SET
                artist = excluded.artist,
                title = excluded.title;
        """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filename);
            ps.setString(2, artist);
            ps.setString(3, title);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDownloaded(String filename) {
        String sql = "SELECT lyrics_downloaded FROM songs WHERE filename = ?";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filename);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("lyrics_downloaded") == 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void markDownloaded(String filename) {
        String sql = "UPDATE songs SET lyrics_downloaded = 1 WHERE filename = ?";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filename);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasMetadata(String filename) {
        String sql = """
        SELECT artist, title FROM songs WHERE filename = ?
    """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filename);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String artist = rs.getString("artist");
                String title = rs.getString("title");

                return artist != null && title != null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}