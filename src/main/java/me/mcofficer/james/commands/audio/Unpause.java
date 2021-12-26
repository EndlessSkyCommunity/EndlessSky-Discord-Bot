package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Unpause extends AudioCommand {


    public Unpause(Audio audio) {
        super(audio);
        name = "unpause";
        help = "Resumes Playback";
        aliases = new String[]{"resume"};
    }

    protected void doCommand(CommandEvent event) {
        audio.unpause(event);
    }

    protected void doCommand(SlashCommandEvent event) {
        audio.unpause(event);
    }
}
