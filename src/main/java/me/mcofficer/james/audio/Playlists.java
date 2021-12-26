package me.mcofficer.james.audio;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Playlists {

    private Path playlistsFile;

    public Playlists() {
        playlistsFile = Paths.get("data/playlists.json");

        try {
            if (!playlistsFile.toFile().exists())
                writeFile("{}");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isOwner(String key, String ownerId) throws IOException, JSONException {
        return readFile().getJSONObject(key).getString("ownerId").equals(ownerId);
    }

    public boolean keyExists(String key) throws IOException {
        return readFile().has(key);
    }

    public String getPlaylistUrl(String key) throws IOException, JSONException {
            return readFile().getJSONObject(key).getString("url");
    }

    public Map.Entry<String, String> getPlaylistInfo(String key) throws IOException, JSONException {
        JSONObject json = readFile().getJSONObject(key);
        return Map.entry(json.getString("url"), json.getString("ownerId"));
    }

    public void changePlaylistUrl(String key, String url) throws IOException {
        JSONObject json = readFile();
        json.getJSONObject(key).put("url", url);
        writeFile(json);
    }

    public void removePlaylist(String key) throws IOException {
        JSONObject json = readFile();
        json.remove(key);
        writeFile(json);
    }

    public List<String> getKeys() throws IOException {
        ArrayList<String> keys = new ArrayList<>();
        readFile().keys().forEachRemaining(keys::add);
        return keys;
    }

    public void addPlaylist(String key, String url, String ownerId) throws IOException {
        JSONObject json = readFile();
        JSONObject playlist = new JSONObject()
                .put("url", url)
                .put("ownerId", ownerId);
        json.put(key, playlist);
        writeFile(json);
    }

    private JSONObject readFile() throws IOException {
        JSONTokener jsonTokener = new JSONTokener(new FileReader(playlistsFile.toFile()));
        return new JSONObject(jsonTokener);
    }

    private void writeFile(JSONObject json) throws IOException {
        writeFile(json.toString(2));
    }

    private void writeFile(String string) throws IOException {
        Files.write(playlistsFile, string.getBytes(StandardCharsets.UTF_8));
    }
}
