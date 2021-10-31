package me.mcofficer.james;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.esparser.Sources;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class Util {

    private static Logger log = LoggerFactory.getLogger(Util.class);

    /**
     * Checks a URL for the HTTP status code.
     * <p>
     * Returns 0 if an IOException occurs.
     * Returns 1 if a MalformedURLException occurs.
     * Returns -1 if the Response is invalid.
     * @param url The url to check.
     * @return The HTTP Status Code.
     */
    public static int getHttpStatus(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "MarioB(r)owser4.2");
            connection.connect();
            return connection.getResponseCode();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return 1;
        }
        catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Fetches the content reached at the URL as String. If the response is invalid, returns empty String.
     * @param url
     * @return
     */
    @CheckReturnValue
    public static String getContentFromUrl(String url) { {
        return getContentFromUrl(url, new HashMap<>());
    }}

    /** Fetches the content reached at the URL as String. If the response is invalid, returns empty String.
     * @param url
     * @param headers
     * @return
     */
    @CheckReturnValue
    public static String getContentFromUrl(String url, Map<String, String> headers) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "MarioB(r)owser4.2");
            headers.forEach(connection::setRequestProperty);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1)
                sb.append((char) cp);
            return sb.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /** Sends a message to the guild's #mod-log channel.
     * @param guild
     * @param message
     */
    public static void log(Guild guild, String message) {
        TextChannel modLog;
        try {
            modLog = guild.getTextChannelsByName("mod-log", true).get(0);
            modLog.sendMessage(message).queue();
        }
        catch (IndexOutOfBoundsException | NullPointerException e) {
            log.error("Failed to find #mod-log channel in guild " + guild.getId() + ", moderation actions will not be logged.");
        }
    }

    /**
     * Returns a random "access denied" message, for humorously dismissing
     * Discord chatters who overstep.
     * @return "Access Denied" string.
     */
    public static String getRandomDeniedMessage(){
        final String[] messageList = {
                "You can't order me around.",
                "I don't listen to you.",
                "You're not my boss.",
                "Try harder.",
                "You think you're a hotshot pirate?",
                "Your attempt at using 'Pug Magic' has failed.",
                "You're no Admiral Danforth.",
                "As if.",
                "That prison on Clink is looking rather empty...",
                "Oh yeah?",
                "Nice try.",
                "I may be old, but I'm not dumb.",
                "I'll pretend you didn't say that.",
                "Not today.",
                "Oh, to be young again...",
                "*yawn*",
                "I have the power. You don't.",
                "Go play in a hyperspace lane.",
                "How about I put *you* in the airlock?",
                "Access Denied.",
                "Please don't joke about that sort of thing."
        };
        int choice = new Random().nextInt(messageList.length);
        return messageList[choice];
    }

    /**
     * Searches through a Guild guild to find the Roles associated with the Array's Strings.
     * Returns only those Roles contained in the Array and the Query.
     * @param query The Search String, contains Rolenames separated by spaces
     * @param guild The Guild to search in
     * @param optinRoles The "Free to Join" Roles
     * @return Possibly empty List of Roles.
     */
    public static List<Role> getOptinRolesByQuery(String query, Guild guild, String[] optinRoles) {
        ArrayList<Role> add = new ArrayList<>();
        for (String arg : query.split(" ")) {
            try {
                for (String optinRole : optinRoles)
                    if (arg.equalsIgnoreCase(optinRole))
                        add.add(guild.getRolesByName(arg, true).get(0));
            }
            catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return add;
    }

    /** Downloads a File and saves it in the target dir.
     * @param url
     * @param targetDir
     * @throws IOException
     */
    public static void downloadFile(String url, Path targetDir, @Nullable String filename) throws IOException {
        if (filename == null)
            filename = url.substring(url.lastIndexOf('/'));
        ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
        FileOutputStream outputStream = new FileOutputStream(targetDir + "/" + filename);
        outputStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
    }

    /** Creates an OrderedMenu and sends it to the event's channel.
     * @param matches The Nodes that should be displayed
     * @param event The triggering event.
     * @param selection A BiConsumer that gets called once the user makes a selection.
     */
    public static void displayNodeSearchResults(List<DataNode> matches, CommandEvent event, BiConsumer<Message, Integer> selection) {
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();
        displayNodeSearchResultsHelper(matches, guild, author, channel, selection);
    }

    public static void displayNodeSearchResults(List<DataNode> matches, SlashCommandEvent event, BiConsumer<Message, Integer> selection) {
        Guild guild = event.getGuild();
        User author = event.getUser();
        MessageChannel channel = event.getChannel();
        displayNodeSearchResultsHelper(matches, guild, author, channel, selection);
    }

    private static void displayNodeSearchResultsHelper(List<DataNode> matches, Guild guild, User author, MessageChannel channel, BiConsumer<Message, Integer> selection) {
        OrderedMenu.Builder builder = new OrderedMenu.Builder()
                .setEventWaiter(James.eventWaiter)
                .setSelection(selection)
                .setUsers(author)
                .useCancelButton(true)
                .setDescription("**Found the following Nodes:**")
                .setColor(guild.getSelfMember().getColor());
        matches.forEach(node -> builder.addChoice(String.join(" ", node.getTokens())));
        builder.build().display(channel);
    }

    /** Saves the ES data files and returns the temporary directory they were saved in. Should be removed afterwards.
     * @param githubToken
     * @return
     * @throws IOException
     */
    public static ArrayList<File> fetchGameData(String githubToken) throws IOException {
        Path temp = Files.createTempDirectory("james");
        File data = new File(temp.toAbsolutePath() + "/data/");
        data.mkdir();
        data.deleteOnExit();

        fetchGameDataRecursive(githubToken, data, "data");
        ArrayList<File> sources = Sources.getSources(temp, null);
        Files.walk(temp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::deleteOnExit);
        return sources;
    }

    private static void fetchGameDataRecursive(String githubToken, File dataFolder, String repoPath) {
        String url = String.format("https://api.github.com/repos/endless-sky/endless-sky/contents%s?ref=master", repoPath);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token "+ githubToken);
        JSONArray json = new JSONArray(Util.getContentFromUrl(url, headers));
        for (Object o : json) {
            try {
                JSONObject j = (JSONObject) o;
                if (j.getString("name").endsWith(".txt")) {
                    Util.downloadFile(j.getString("download_url"), dataFolder.toPath(), j.getString("path").replaceAll("/", "_"));
                } else { // assume we have a directory
                    fetchGameDataRecursive(githubToken, dataFolder, j.getString("path"));
                }
            }
            catch (IOException | JSONException e ) {
                e.printStackTrace();
            }
        }
    }

    /** Compiles a list of valid image URLs from the ES repository.
     * @param githubToken
     * @return A List of image URLs. Could theoretically be empty, if GitHub is down.
     */
    public static ArrayList<String> get1xImagePaths(String githubToken) {
        return checkImageDir("https://api.github.com/repos/endless-sky/endless-sky/contents/images?ref=master", githubToken);
    }

    /** Iterates recursively through a directory of the GitHub API and compiles a List of image paths. Only for use in {@link #get1xImagePaths(String)}.
     * @param url
     * @param githubToken
     * @return
     */
    private static ArrayList<String> checkImageDir(String url, String githubToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token "+ githubToken);
        JSONArray json = new JSONArray(Util.getContentFromUrl(url, headers));
        ArrayList<String> arrayList = new ArrayList<>();
        for (Object o : json) {
            JSONObject j = (JSONObject) o;
            if (j.getString("type").equals("dir"))
                arrayList.addAll(checkImageDir(j.getString("url"), githubToken));
            else
                arrayList.add(j.getString("download_url"));
        }
        return arrayList;
    }

    /** Takes a list of image paths and replaces them with paths to the hdpi repository where applicable. Takes several minutes, run async!
     * @param imagePaths
     * @return A List of Image Paths, containing hdpi paths where possible.
     */
    public static ArrayList<String> get2xImagePaths(ArrayList<String> imagePaths) {
        ArrayList<String> revisedPaths = new ArrayList<>();
        for (String path : imagePaths) {
            String revised = path.replace("endless-sky/master", "endless-sky-high-dpi/master")
                    .replace(".png", "@2x.png");
            if (getHttpStatus(revised) == 200)
                revisedPaths.add(revised);
            else
                revisedPaths.add(path);
        }
        return revisedPaths;
    }

    public static String sendInChunksReturnLast(TextChannel channel, String... output) {
        return sendInChunksHelper(channel, Arrays.asList(output), "```", "```", true);
    }

    /**
     * Convenience method for {@link #sendInChunks(TextChannel, List, String, String)},
     * accepts Arrays of Strings and assumes triple backticks as footer & header.
     * @param channel
     * @param output
     */
    public static void sendInChunks(TextChannel channel, String... output) {
        sendInChunks(channel, Arrays.asList(output), "```", "```");
    }

    /**
     * Utility function that will concatenate a list of strings into valid
     * Discord messages.
     * @param channel   The desired output channel
     * @param output    The list of strings to write.
     * @param header    A string that should prefix every chunk.
     * @param footer    A string that should end every chunk.
     */
    public static void sendInChunks(TextChannel channel, List<String> output, String header, String footer) {
        String whatever = sendInChunksHelper(channel, output, header, footer, false);
        return;
    }

    private static String sendInChunksHelper(TextChannel channel, List<String> output, String header, String footer, boolean returnLastChunk) {
        if(output.isEmpty())
            return "";

        StringBuilder chunk = new StringBuilder(header);
        int chunkSize = chunk.length() + footer.length();
        final int sizeLimit = 1990;
        if(chunkSize > sizeLimit){
            System.out.println("Cannot ever print: header + footer too large.");
            return "";
        }
        for(String str : output){
            if(chunkSize + str.length() <= sizeLimit)
                chunk.append(str);
            else{
                channel.sendMessage(chunk.append(footer).toString()).queue();
                chunk = new StringBuilder(header + str);
            }
            chunkSize = chunk.length() + footer.length();
        }
        // Write, or return the final chunk.
        if(chunk.length() > header.length()) {
            if (returnLastChunk) {
                return chunk.append(footer).toString();
            }
            channel.sendMessage(chunk.append(footer).toString()).queue();
        }
        return "";
    }


    /** Replaces a Member's Roles with temporaryRole for seconds, and logs the action to #mod-log.
     * @param temporaryRole
     * @param seconds
     * @param member
     * @param logOnCommand
     * @param logOnRelease
     */
    public static void replaceRolesTemporarily(Role temporaryRole, long seconds, Member member, String logOnCommand, String logOnRelease) {
        List<Role> originalRoles = member.getRoles();
        // Remove current roles & add temporary role
        if (member.getRoles().contains(temporaryRole))
            Util.log(member.getGuild(), String.format("Attempted to give Role %s to %s, but %s already has it! Aborting Role Removal.",
                    temporaryRole.getAsMention(), member.getAsMention(), member.getAsMention()));
        else {
            Guild guild = member.getGuild();

            guild.modifyMemberRoles(member, new ArrayList<>() {{ add(temporaryRole); }}, originalRoles).queue(success1 ->
                    // Remove timout role & re-add old roles
                    guild.removeRoleFromMember(member, temporaryRole).queueAfter(seconds, TimeUnit.SECONDS, success2 -> {
                        originalRoles.forEach(role -> guild.addRoleToMember(member, role).queue());
                        Util.log(member.getGuild(), logOnRelease);
                    })
            );
            Util.log(member.getGuild(), logOnCommand);
        }
    }

    /**
     * @param milis
     * @return A String of the format HH:MM:SS (hours will be omitted if 0)
     */
    public static String MilisToTimestring(long milis) {
        int seconds = (int) ((milis / 1000) % 60);
        int minutes = (int) ((milis / (1000 * 60)) % 60);
        int hours = (int) ((milis / (1000 * 60 * 60)) % 24);
        int days = (int) (milis / (1000 * 60 * 60 * 24));

        if(days > 0)
            return String.format("%2dd %02d:%02d:%02d", days, hours, minutes, seconds);
        else if(hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Returns the a String containing what e's printStacktrace method would print.
     * @param e
     * @return
     */
    public static String ExceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
