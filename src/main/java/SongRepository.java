import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

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

    public static SongMetadata getMetadata(String filename) {

        String sql = "SELECT artist, title FROM songs WHERE filename = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, filename);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String artist = rs.getString("artist");
                String title = rs.getString("title");

                return new SongMetadata(artist, title);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void saveLyrics(String filename, String lyrics) {

        try {
            // ensure folder exists
            Files.createDirectories(Paths.get("lyrics"));

            // convert .mp3 → .txt
            String baseName;

            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex == -1) {
                baseName = filename;
            } else {
                baseName = filename.substring(0, dotIndex);
            }

            String lyricsFilename = baseName + ".txt";

            Path filePath = Paths.get("lyrics", lyricsFilename);

            Files.writeString(
                    filePath,
                    lyrics,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}