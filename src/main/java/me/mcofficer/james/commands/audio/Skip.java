package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;

public class Skip extends AudioCommand {

    private final static String optionName = "number";

    public Skip(Audio audio) {
        super(audio);
        name = "skip";
        help = "Skips X songs (defaults to 1).";
        arguments = "X";
        this.aliases = new String[]{"next"};

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.INTEGER, optionName, "The number off tracks to skip. Defaults to 1.").setRequired(false));
        this.options = data;
    }

    protected void doCommand(CommandEvent event) {
        if (audio.getPlayingTrack() != null) {
            int amount = 1;
            try {
                amount = Integer.parseInt(event.getArgs());
            }
            catch (NumberFormatException e) {}
            audio.skip(event, amount);
        }
    }

    protected void doCommand(SlashCommandEvent event) {
        if (audio.getPlayingTrack() != null) {
            int amount = 1;
            List<OptionMapping> choice = event.getOptions();
            if (!choice.isEmpty()) {
                long value = choice.get(0).getAsLong();
                amount = ((int)value == value ? (int)value : Integer.MAX_VALUE);
            }
            audio.skip(event, amount);
        }
    }
}
