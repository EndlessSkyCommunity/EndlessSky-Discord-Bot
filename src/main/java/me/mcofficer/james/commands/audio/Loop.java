package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class Loop extends AudioCommand {

    private final String optionName = "loop";

    public Loop(Audio audio) {
        super(audio);
        name = "loop";
        help = "Checks if and controls whether looping is on or off.";
        this.aliases = new String[]{"repeat"};

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.BOOLEAN, optionName, "Turn looping on or off? Leave blank to check current setting.").setRequired(false));
        this.options = data;
    }

    protected void doCommand(CommandEvent event) {
        if (audio.getPlayingTrack() != null) {
            String args = event.getArgs();
            if (args.length() == 0) {
                audio.getLoop(event);
            }
            else {
                String arg = args.split(" ")[0];
                if (arg.equals("on"))
                    audio.setLoop(event, true);
                else if (arg.equals("off"))
                    audio.setLoop(event, false);
            }
        }
    }

    protected void doCommand(SlashCommandEvent event) {
        if (audio.getPlayingTrack() != null) {
            if (!event.getOptions().isEmpty()) {
                audio.getLoop(event);
            }
            else {
                audio.setLoop(event);
            }
        }
    }
}
