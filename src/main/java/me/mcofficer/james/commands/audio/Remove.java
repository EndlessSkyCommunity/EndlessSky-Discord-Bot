package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;

public class Remove extends AudioCommand {

    private final static String optionName = "number";

    public Remove(Audio audio) {
        super(audio);

        name = "remove";
        help = "Removes the track at the specified position in the queue.";
        arguments = "X";

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.INTEGER, optionName, "The position in the queue of hte track to remove.").setRequired(true));
        this.options = data;
    }

    protected void doCommand(CommandEvent event) {
        if (audio.getPlayingTrack() != null) {
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

    protected void doCommand(SlashCommandEvent event) {
        if (audio.getPlayingTrack() != null) {
            long position = event.getOption(optionName).getAsLong();
            int intPosition = (int)position;
            if (position < -1 || position == 0 || intPosition != position) {
                audio.announceInvalidRemove(event);
                return;
            }
            audio.remove(event, intPosition);
        }
    }
}
