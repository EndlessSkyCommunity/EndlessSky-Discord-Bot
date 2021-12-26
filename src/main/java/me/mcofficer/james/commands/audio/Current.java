package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;


public class Current extends AudioCommand {

    public Current(Audio audio) {
        super(audio);
        name = "current";
        help = "Displays the currently playing song.";
        aliases = new String[]{"playing"};
    }

    protected void doCommand(CommandEvent event) {
        audio.announceCurrentTrack(event);
    }

    protected void doCommand(SlashCommandEvent event) {
        audio.announceCurrentTrack(event);
    }
}
