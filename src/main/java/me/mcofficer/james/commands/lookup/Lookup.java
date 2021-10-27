package me.mcofficer.james.tools;

import me.mcofficer.esparser.DataFile;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.Util;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Lookups {

    private OkHttpClient okHttpClient;
    private final ArrayList<DataFile> dataFiles;
    private ArrayList<String> imagePaths;

    public Lookups(OkHttpClient okhttpClient, ArrayList<DataFile> dataFiles, ArrayList<String> imagePaths) {
        this.okHttpClient = okhttpClient;
        this.dataFiles = dataFiles;
        this.imagePaths = imagePaths;
    }

    /**
     * Compiles online references for the given Node, which can be used in Embeds.
     * @param node  The Node in question.
     * @return      A String containing markdown links (may be empty)
     */
    public String getLinks(DataNode node) {
        StringBuilder stringBuilder = new StringBuilder();

        String nodeName = String.join(" ", node.getTokens().subList(1, node.getTokens().size()));
        String nodeType = node.getTokens().get(0);

        // endless-sky.7vn.io/endless-ships/
        if (nodeType.equals("outfit")) {
            String url = "http://endless-sky.7vn.io/outfits/" + nodeName.replace(" ", "-").toLowerCase();
            if (Util.getHttpStatus(url) == 200)
                stringBuilder.append(String.format("[\"%s\" on 7vn.io](%s)\n", nodeName, url));
        }
        else if (nodeType.equals("ship")) {
            String url = "http://endless-sky.7vn.io/ships/" + nodeName.replace(" ", "-").toLowerCase();
            if (Util.getHttpStatus(url) == 200)
                stringBuilder.append(String.format("[\"%s\" on 7vn.io](%s)\n", nodeName, url));
        }

        // bunker.tejat.net/endless-ships | bunker.tejat.net/endless-outfits
        if (nodeType.equals("outfit")) {
            String url = "https://bunker.tejat.net/endless-outfits/"
                    + nodeName.replace(" ", "_").replace("-", "_").toLowerCase() + ".html";
            if (Util.getHttpStatus(url) == 200)
                stringBuilder.append(String.format("[\"%s\" on bunker.tejat.net](%s)\n", nodeName, url));
        }
        else if (nodeType.equals("ship")) {
            String url = "https://bunker.tejat.net/endless-ships/"
                    + nodeName.replace(" ", "_").replace("-", "_").toLowerCase() + ".html";
            if (Util.getHttpStatus(url) == 200)
                stringBuilder.append(String.format("[\"%s\" on bunker.tejat.net](%s)\n", nodeName, url));
        }

        // endlesssky.mcofficer.me/ship_gallery/
        if (nodeType.equals("ship")) {
            String base_url = "https://endlesssky.mcofficer.me/ship_gallery/";
            String file = "assets/" + nodeName.toLowerCase().replace(" ", "%20") + ".json";
            if (Util.getHttpStatus(base_url + file) == 200)
                stringBuilder.append(String.format("[View the %s model in 3D](%swebplayer.html?load=%s)\n", nodeName, base_url, file));
        }

        // endlesssky.mcofficer.me/assets/
        for (String link : getAssetsUrls(node))
            stringBuilder.append(String.format("[%s](%s)\n", link.substring(link.lastIndexOf('/') + 1).replace("%20", " "), link));

        // TODO: endless-sky.wikia.com

        return stringBuilder.toString();
    }

    /**
     * Attempts to fetch the source files corresponding with the DataNode node from endlesssky.mcofficer.me/assets/
     * @param node
     * @return A possibly-empty list of download URLs.
     */
    @CheckReturnValue
    private List<String> getAssetsUrls(DataNode node) {
        String baseUrl = "https://endlesssky.mcofficer.me/";
        String nodeType = node.getTokens().get(0);
        String nodeName = String.join(" ", node.getTokens().subList(1, node.getTokens().size()));
        List<String> returnUrls = new ArrayList<>();

        String dir;
        if (nodeType.equals("ship"))
            dir = "ships";
        else if (nodeType.equals("outfit"))
            dir = "outfits";
        else if (nodeType.equals("projectile"))
            dir = "projectiles";
        else
            return returnUrls;

        String query = String.format("{ \"action\": \"get\", \"search\": { \"href\": \"/\",\"pattern\": \"%s\", \"ignorecase\": true } }", nodeName);
        RequestBody body = RequestBody.create(MediaType.get("application/json"), query);
        Request request = new Request.Builder()
                .url(baseUrl + "assets/?")
                .post(body)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            for (Object result : json.getJSONArray("search"))
                if (result instanceof JSONObject) { // sigh
                    String url = baseUrl + ((JSONObject) result).getString("href");
                    String[] urlSegments = url.split("/");
                    if (urlSegments[urlSegments.length - 2].equals(dir))
                        returnUrls.add(url);
                }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return returnUrls;
    }

    /**
     * Searches through all DataNodes and returns any that <br>
     *     a) are of the type "government" and <br>
     *     b) have a subnode of the type "swizzle", whose value matches the swizzle parameter.
     * @param swizzle an Integer
     * @return A List of DataNodes
     */
    public List<DataNode> getGovernmentsBySwizzle(int swizzle) {
        List<DataNode> governments = new ArrayList<>();
        for (DataFile file : dataFiles)
            for (DataNode node : file.getNodes())
                if (node.getTokens().get(0).equals("government"))
                    for (DataNode child : node.getChildren())
                        if (child.getTokens().get(0).equals("swizzle") && child.getTokens().get(1).equals(String.valueOf(swizzle)))
                            governments.add(node);
        return governments;
    }

    /** Searches through the datafiles and returns 10 or less Nodes matching the query.
     * @param query
     * @return A possibly empty List of Nodes.
     */
    @CheckReturnValue
    public List<DataNode> getNodesByString(String query) {
        Map<DataNode, Float> matches = new HashMap<>();
        for (DataFile file : dataFiles) {
            for (DataNode node : file.getNodes()) {
                StringMetric metric = StringMetrics.needlemanWunch();
                String tokens = String.join(" ", node.getTokens()).trim();
                matches.put(node, metric.compare(query.toLowerCase(), tokens.toLowerCase()));
            }
        }
        // Sort in descending order & limit to 10 results
        return matches.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public String getNodeAsText(DataNode node) {
        return printNodeRecursive(node);
    }

    private String printNodeRecursive(DataNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n")
                .append(String.join(" ", node.getTokens()).trim());
        for(DataNode child : node.getChildren())
            sb.append("\t\t")
                    .append(printNodeRecursive(child).replace("\n", "\n\t"));
        return sb.toString();
    }

    /** Gets a description and image from a Node.
     * @param node
     * @return A String[] possibly containing null.
     */
    @CheckReturnValue
    public String[] getLookupByNode(DataNode node) {
        return new String[]{getImageUrl(node, true), getDescription(node)};
    }

    /** Gets the description of a Node.
     * @param node
     * @return A String or null.
     */
    @CheckForNull
    public String getDescription(DataNode node) {
        DataNode descNode = getDescriptionChildNode(node);
        if (descNode == null)
            return null;
        return String.join(" ", descNode.getTokens().subList(1, descNode.getTokens().size()));
    }

    /** Tries to get an Image URL from the Node node. If thumbnail is true, tries to get a thumbnail (instead of a sprite/landscape/...).
     * @param node
     * @param thumbnail
     * @return A String or null.
     */
    @CheckForNull
    public String getImageUrl(DataNode node, boolean thumbnail) {
        DataNode imageNode = getImageChildNode(node, thumbnail);
        if (imageNode == null)
            return null;
        String path = String.join(" ", imageNode.getTokens().subList(1, imageNode.getTokens().size()));
        String bestMatch = null;
        try {
            // Find the shortest possible image url containing `path`
            for (String imagePath : imagePaths) {
                String decoded = java.net.URLDecoder.decode(imagePath, StandardCharsets.UTF_8.name());
                if (decoded.contains(path) &&
                        (bestMatch == null ? Integer.MAX_VALUE : bestMatch.substring(bestMatch.lastIndexOf('/')).length())
                                > imagePath.substring(imagePath.lastIndexOf('/')).length())
                    bestMatch = imagePath;
            }
        }
        catch (UnsupportedEncodingException e) { //Should never happen since UTF-8 is from StandardCharsets
            e.printStackTrace();
        }
        return bestMatch;
    }

    /** Searches a Node for a description Subnode.
     * @param node
     * @return The Subnode containing the description.
     */
    @CheckForNull
    private DataNode getDescriptionChildNode(DataNode node) {
        for (DataNode child : node.getChildren()) {
            String identifier = child.getTokens().get(0);
            if (identifier.equals("description"))
                return child;
        }
        return null;
    }

    /** Searches a Node for an image Subnode. If thumbnail is true, tries to get a thumbnail, otherwise sprite takes priority.
     * @param node
     * @param thumbnail
     * @return The Subnode containing the relative image path.
     */
    @CheckForNull
    private DataNode getImageChildNode(DataNode node, boolean thumbnail) {
        DataNode imageNode = null;

        if (thumbnail)
            imageNode = getThumbnailChildNode(node);
        if (imageNode != null)
            return imageNode;

        for (DataNode child : node.getChildren()) {
            String identifier = child.getTokens().get(0);
            if (identifier.equals("sprite") || identifier.equals("landscape"))
                return child;
        }

        // No need to search for a thumbnail twice
        if (!thumbnail)
            return getThumbnailChildNode(node);
        return null;
    }

    /** Searches a Node for an image Subnode of the type thumbnail.
     * If you want to fallback to sprites/landscapes if no thumbnail exists, use {@link #getImageChildNode(DataNode, boolean)}.
     * @param node
     * @return The Subnode containing the relative image path.
     */
    @CheckForNull
    private DataNode getThumbnailChildNode(DataNode node) {
        for (DataNode child : node.getChildren())
            if (child.getTokens().get(0).equals("thumbnail"))
                return child;
        return null;
    }

    public ArrayList<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(ArrayList<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public ArrayList<DataFile> getDataFiles() {
        return dataFiles;
    }
}
