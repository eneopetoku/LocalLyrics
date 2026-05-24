🎵 LocalLyrics

LocalLyrics is a lightweight tool that scans a folder of music files, extracts artist and song title information, and automatically downloads lyrics for each song.

⚠️ This project is currently in early development. Features and structure may change.

✨ Overview

* The goal of LocalLyrics is to automate the process of retrieving lyrics for a local music library by:

* Reading music files from a selected folder

* Extracting artist and song title from filenames

* Querying an online lyrics source

* Saving lyrics locally for later use

🧠 How It Works

* The program scans a given music directory

* It reads each file and extracts a best-guess of:

Artist name

Song title

It uses this information to search for lyrics online

Lyrics are downloaded and stored locally

📁 Example Input / Output

Input

Music/

 ├── Eminem - Lose Yourself.mp3
 ├── Coldplay - Yellow.mp3

Output

Artist: Eminem
Title: Lose Yourself
Lyrics: saved locally


Artist: Coldplay
Title: Yellow
Lyrics: saved locally

🚀 Current Status

* Basic filename parsing is being implemented

* Lyrics fetching logic is in progress

* File processing is designed for single-folder scanning

🔧 Future Ideas

These may or may not be implemented:

* Better metadata extraction from audio files

* Improved filename parsing accuracy

* Support for processing multiple folders recursively

* Caching lyrics to avoid repeated downloads

* Simple CLI improvements or configuration options

⚠️ Notes

* Accuracy depends heavily on how well filenames are formatted

* Some songs may not match correctly if naming is inconsistent

* Lyrics availability depends on external sources

📌 Status

🚧 Work in progress — not production ready

👤 Author

Eneo Petoku
