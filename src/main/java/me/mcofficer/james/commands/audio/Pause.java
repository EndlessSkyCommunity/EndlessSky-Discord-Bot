package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Pause extends AudioCommand {

    public Pause(Audio audio) {
        super(audio);

        name = "pause";
        help = "Pauses playback.";
    }

    protected void doCommand(CommandEvent event) {
        audio.pause(event);
    }

    protected void doCommand(SlashCommandEvent event) {
        audio.pause(event);
    }
}
