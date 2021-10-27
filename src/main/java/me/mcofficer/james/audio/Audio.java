package me.mcofficer.james.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.CheckForNull;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class Audio {

    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final AudioPlayer player;
    private final AudioPlayerSendHandler audioPlayerSendHandler;
    private final TrackScheduler trackScheduler;
    private AudioManager audioManager;

    public Audio() {
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();

        trackScheduler = new TrackScheduler(player);
        audioPlayerSendHandler = new AudioPlayerSendHandler(player);
    }

    /** Connects to bot to a VoiceChannel. Silently fails if the bot is already connected.
     * @param voiceChannel
     */
    public void connect(VoiceChannel voiceChannel) {
        if (audioManager == null || !audioManager.isConnected()) {
            audioManager = voiceChannel.getGuild().getAudioManager();
            audioManager.openAudioConnection(voiceChannel);
            audioManager.setSendingHandler(audioPlayerSendHandler);
        }
    }

    /**
     * Stops Playback, clears the Queue and disconnects from the VoiceChannel.
     */
    public void stopAndDisconnect() {
        trackScheduler.stop();
        audioManager.closeAudioConnection();
    }

    /**
     * Attempts to load a track/playlist that matches the identifier.
     * If successful, enqueues the item and calls {@link #announceTrack(AudioTrack, CommandEvent)}
     * or {@link #announcePlaylist(AudioPlaylist, CommandEvent)} respectively.
     * @param identifier
     * @param event
     */
    public void loadItem(String identifier, CommandEvent event) {
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.enqueue(track);
                announceTrack(track, event);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack track = playlist.getTracks().get(0);
                    trackScheduler.enqueue(track);
                    announceTrack(track, event);
                }
                else {
                    playlist.getTracks().forEach(trackScheduler::enqueue);
                    announcePlaylist(playlist, event);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    /**
     * @param guild
     * @return an EmbedBuilder with title and color set.
     */
    private EmbedBuilder createEmbedTemplate(Guild guild) {
        return new EmbedBuilder()
                .setTitle("Audio-Player", James.GITHUB_URL)
                .setColor(guild.getSelfMember().getColor());
    }

    /**
     * Announces that a new Track has been enqueued.
     * @param track
     * @param event
     */
    private void announceTrack(AudioTrack track, CommandEvent event) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                .setDescription(
                        String.format("Queueing `%s` [\uD83D\uDD17](%s)\n(requested by %s)",
                                track.getInfo().title, track.getInfo().uri, event.getMember().getAsMention())
                )
                .setThumbnail(getThumbnail(track));
        event.reply(embedBuilder.build());
    }

    /**
     * Announces that a new Playlist has been enqueued.
     * @param playlist
     * @param event
     */
    private void announcePlaylist(AudioPlaylist playlist, CommandEvent event) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                .setDescription(
                        String.format("Queueing Playlist `%s` (%s tracks, requested by %s)",
                                playlist.getName(), playlist.getTracks().size(), event.getMember().getAsMention())
                )
                .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/play.png");
        event.reply(embedBuilder.build());
    }

    /** Skips a number of Tracks and announces it.
     * @param event
     * @param amount
     */
    public void skip(CommandEvent event, int amount) {
        for (int i = 0; i < amount; i++)
            trackScheduler.skip();
        announceSkip(event, amount);
    }

    private void announceSkip(CommandEvent event, int amount) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                .setDescription(
                        String.format("Skipped %s track(s)\n(requested by %s)", amount, event.getMember().getAsMention())
                )
                .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/skip.png");
        event.reply(embedBuilder.build());
    }

    public void remove(CommandEvent event, int position) {
        announceRemove(event, trackScheduler.remove(position));
    }

    private void announceRemove(CommandEvent event, AudioTrack removed) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild());
        if (removed == null)
            embedBuilder.setDescription(String.format("No track removed. There are only %s tracks in the queue!", trackScheduler.getQueueSize()));
        else
            embedBuilder.setDescription(String.format("Removed a track from the queue\n(requested by %s)", event.getMember().getAsMention()));
        event.reply(embedBuilder.build());
    }

    /**
     * @return the VoiceChannel the bot is connected to, or null if it's not connected at all.
     */
    @CheckForNull
    public VoiceChannel getVoiceChannel() {
        return audioManager.getConnectedChannel();
    }

    public void shuffle(CommandEvent event) {
        trackScheduler.shuffle();
        announceShuffle(event);
    }

    private void announceShuffle(CommandEvent event) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                .setDescription(String.format("The Queue has been shuffled by %s", event.getMember().getAsMention()))
                .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/shuffle.png");
        event.reply(embedBuilder.build());
    }

    /**
     * @return The currently playing AudioTrack or null.
     */
    @CheckForNull
    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    /** Announces the currently playing Track.
     * @param event
     */
    public void announceCurrentTrack(CommandEvent event) {
        AudioTrack track = player.getPlayingTrack();
        if (track == null)
            event.reply("Not playing anything!");
        else {
            String trackString = String.format("**Playing:** %s [\uD83D\uDD17](%s)\n",
                            track.getInfo().title, track.getInfo().uri);
            EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                    .setDescription(trackString)
                    .appendDescription(String.format("**Time:** [%s / %s]",
                            Util.MilisToTimestring(track.getPosition()),
                            Util.MilisToTimestring(track.getDuration())))
                    .setThumbnail(getThumbnail(track));

            event.getTextChannel().sendMessage(embedBuilder.build()).queue(message -> {
                while (track.equals(getPlayingTrack())) {
                    embedBuilder.setDescription(trackString)
                            .appendDescription(String.format("**Time:** [%s / %s]",
                                    Util.MilisToTimestring(track.getPosition()),
                                    Util.MilisToTimestring(track.getDuration()))
                            );

                    message.editMessage(embedBuilder.build()).queue();
                    try {
                        TimeUnit.SECONDS.sleep(15);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /** Pauses playback and announces it.
     * @param event
     */
    public void pause(CommandEvent event) {
        player.setPaused(true);
        announcePause(event);
    }

    private void announcePause(CommandEvent event) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                .setDescription(String.format("The Audio Player has been paused.\n(requested by %s)",
                        event.getMember().getAsMention()))
                .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/pause.png");
        event.reply(embedBuilder.build());
    }

    /** Pauses playback and announces it.
     * @param event
     */
    public void unpause(CommandEvent event) {
        if (player.isPaused()) {
            player.setPaused(false);
            announceUnpause(event);
        }
    }

    private void announceUnpause(CommandEvent event) {
        EmbedBuilder embedBuilder = createEmbedTemplate(event.getGuild())
                .setDescription(String.format("The Audio Player has been unpaused\n(requested by %s)",
                        event.getMember().getAsMention()))
                .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/play.png");
        event.reply(embedBuilder.build());
    }

    /**
     * @param event
     */
    public void createQueueEmbed(CommandEvent event) {
        LinkedList<AudioTrack> queue = trackScheduler.getQueue();

        if(queue.isEmpty())
            event.reply("The Queue is empty!");
        else {
            ArrayList<String> items = new ArrayList<>();
            long queueLength = 0;
            for(AudioTrack track : queue){
                items.add(String.format("`[%s]` %s", Util.MilisToTimestring(track.getDuration()), track.getInfo().title));
                queueLength += track.getDuration();
            }

            new Paginator.Builder()
                    .setText(String.format("Showing %s Tracks. \n Total Queue Time Length: %s", queue.size(), Util.MilisToTimestring(queueLength)))
                    .setItems(items.toArray(new String[0]))
                    .setItemsPerPage(10)
                    .setEventWaiter(James.eventWaiter)
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .useNumberedItems(true)
                    .waitOnSinglePage(true)
                    .setBulkSkipNumber(items.size() > 50 ? 5 : 0) // Only show bulk skip buttons when the Queue is sufficiently large
                    .build()
                    .display(event.getChannel());
        }
    }

    /**
     * Writes the currently playing track & queue's URIs to a file and sends it to the user.
     * @param event
     * @param fileName
     */
    public void sendQueueFile(CommandEvent event, String fileName) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(getPlayingTrack().getInfo().uri);
        }
        catch (NullPointerException e) {
            event.reply("Nothing Playing!");
        }
        for (AudioTrack track : trackScheduler.getQueue())
            builder.append("\n").append(track.getInfo().uri);

        event.getTextChannel().sendFile(
                new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8)), fileName
        ).queue();
    }

    /** Attempts to find a Thumbnail URL for the AudioTrack track.
     * If nothing is found (or the service is not supported), returns the default "Playing" Icon.
     * @param track An AudioTrack.
     * @return A valid Thumbnail URL.
     */
    public String getThumbnail(AudioTrack track) {
        String url = null; // Util.getHttpStatus will catch it

        if (track.getInfo().uri.startsWith("https://www.youtube.com/watch?v=")) // lavaplayer *always* constructs YT URLs like this
            url = "http://i1.ytimg.com/vi/" + track.getIdentifier() + "/0.jpg";
        else if (track.getInfo().uri.startsWith("https://soundcloud.com/"))
            url = getSoundcloudThumbnail(track.getInfo().uri);

        if (Util.getHttpStatus(url) == 200)
            return url;
        return James.GITHUB_RAW_URL + "thumbnails/play.png";
    }

    /** Not the most reliable method, but i doesn't *have* to work, and i don't want to depend on yet another API.
     * @param trackUrl The URL of a Soundcloud track.
     * @return A Soundcloud Thumbnail URL (500x500) or null.
     */
    @CheckForNull
    private String getSoundcloudThumbnail(String trackUrl){
        String html = Util.getContentFromUrl(trackUrl);
        int pos = html.indexOf("\"artwork_url\":") + 15;
        try{
            String artwork_url = html.substring(pos, html.indexOf("-large.jpg\"", pos) + 10);
            if(artwork_url.contains("-large.png"))
                artwork_url = html.substring(pos, html.indexOf("-large.png\"", pos) + 10);
            return artwork_url;
        }
        catch(IndexOutOfBoundsException e){
            return null;
        }
    }
}
