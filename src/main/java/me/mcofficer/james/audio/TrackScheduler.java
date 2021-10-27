package me.mcofficer.james.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final LinkedList<AudioTrack> queue = new LinkedList<>();

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        player.addListener(this);
        looping = false;
    }

    /**
     * Shuffles the Queue.
     */
    public void shuffle() {
        Collections.shuffle(queue);
    }

    /**
     * Removes the first song in the {@link #queue} and plays it. Stops playback if the queue is empty.
     */
    public void skip() {
        play(queue.poll());
    }

    public AudioTrack remove(int position) {
        if (position >= getQueueSize())
            return null;
        return queue.remove(position - 1);
    }

    /**
     * Plays a track immediately. You probably want to use {@link #enqueue(AudioTrack)} instead.
     * @param track
     */
    public void play(AudioTrack track) {
        player.playTrack(track);
    }

    /** Plays a track if nothing is playing currently, otherwise adds it to the {@link #queue}.
     * @param track
     */
    public void enqueue(AudioTrack track) {
        if(!player.startTrack(track, true)) //something is currently playing
            queue.offer(track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(looping)
            player.playTrack(player.getPlayingTrack());
        else if(endReason.mayStartNext)
            skip();
    }

    /**
     * Stops Playback and clears the {@link #queue}.
     */
    public void stop() {
        player.stopTrack();
        queue.clear();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public LinkedList<AudioTrack> getQueue() {
        return queue;
    }
}
