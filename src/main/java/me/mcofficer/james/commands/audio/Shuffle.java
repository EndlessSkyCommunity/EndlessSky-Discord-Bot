package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Shuffle extends AudioCommand {

    public Shuffle(Audio audio) {
        super(audio);
        name = "shuffle";
        help = "Shuffles the AudioPlayer's queue.";
    }

    protected void doCommand(CommandEvent event) {
        audio.shuffle(event);
    }

    protected void doCommand(SlashCommandEvent event) {
        audio.shuffle(event);
    }
}
