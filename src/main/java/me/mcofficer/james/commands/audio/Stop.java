package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Stop extends AudioCommand {

    public Stop(Audio audio) {
        super(audio);
        name = "stop";
        help = "Stops playback and disconnects from the VoiceChannel";
        arguments = "<query>";
    }

    protected void doCommand(CommandEvent event) {
        audio.stopAndDisconnect();
    }

    protected void doCommand(SlashCommandEvent event) {
        audio.stopAndDisconnect();
    }
}
