import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class EmbeddedMetadataReader {
    // Extracts metadata from the embedded tags within the audio file,
// avoiding the need to query the AIService.
    public SongMetadata getMetadata(String file) {
        File fileObj = new File(file);
        try {

            AudioFile audioFile = AudioFileIO.read(fileObj);

            Tag tag = audioFile.getTag();

            if (tag == null) {
                return null;
            }

            String artist = tag.getFirst(FieldKey.ARTIST);
            String title = tag.getFirst(FieldKey.TITLE);

            if (artist.isBlank() || title.isBlank()) {
                return null;
            }

            return new SongMetadata(artist, title);

        } catch (Exception e) {

            System.out.println(
                    "Failed to read embedded metadata: "
                            + fileObj.getName()
            );

            return null;
        }
    }
}