package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;

public class Remove extends Command {
    private final Audio audio;

    public Remove(Audio audio) {
        name = "remove";
        help = "Removes the track at the specified position in the queue.";
        arguments = "X";
        category = James.audio;
        this.audio = audio;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (audio.getVoiceChannel() != null
                && event.getMember().getVoiceState().getChannel().equals(audio.getVoiceChannel())
                && audio.getPlayingTrack() != null) {
            int position = 0;
            boolean validNumber = true;
            try {
                position = Integer.parseInt(event.getArgs());
            }
            catch (NumberFormatException e) {
                validNumber = false;
            }
            if (!validNumber || position < -1 || position == 0) {
                audio.announceInvalidRemove(event);
                return;
            }
            audio.remove(event, position);
        }
    }
}
