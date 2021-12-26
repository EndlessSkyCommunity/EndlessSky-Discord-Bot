package me.mcofficer.james.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final LinkedList<AudioTrack> queue = new LinkedList<>();
    
    private boolean looping;

    public TrackScheduler(@NotNull AudioPlayer player) {
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

    /**
     * Removes the track at the position given and returns it as an AudioTrack.
     * @param index The index of the track in the queue to be removed.
     * @return The track that was removed as an AudioTrack, or
     */
    public AudioTrack remove(int index) {
        if (index >= getQueueSize() || index < 0)
            return null;
        return queue.remove(index);
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

    /**
     * Called when a track ends. If looping is on, plays the track that just ended, otherwise, calls skip().
     * @param player The AudioPlayer object.
     * @param track The track that just ended.
     * @param endReason The reason the track ended.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            if (looping)
                play(track.makeClone());
            else
                skip();
        }
    }

    /**
     * Stops Playback and clears the {@link #queue} and turns looping off.
     */
    public void stop() {
        player.stopTrack();
        looping = false;
        queue.clear();
    }

    public void setLooping(boolean loop) {
        looping = loop;
    }

    public boolean getLooping() {
        return looping;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public LinkedList<AudioTrack> getQueue() {
        return queue;
    }
}
