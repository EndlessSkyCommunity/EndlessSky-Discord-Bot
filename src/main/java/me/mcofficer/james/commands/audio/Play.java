package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Play extends AudioCommand {

    private final String optionName = "query";

    public Play(Audio audio) {
        super(audio);
        name = "play";
        help = "Plays a track by it's url, or searches for it by a query";
        arguments = "<query>";

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.STRING, optionName, "The URL or (YouTube) search query for the track to play.").setRequired(true));
        this.options = data;
    }

    @Override
    protected void execute(@NotNull CommandEvent event) {
        if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel() ||
                event.getMember().getVoiceState().getChannel().equals(audio.getVoiceChannel())) {
            String query = event.getArgs();
            try {
                URL url = new URL(query);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (IOException e) { // URL is invalid or unreachable
                query = "ytsearch:" + query;
            }

            audio.connect(event.getMember().getVoiceState().getChannel());
            audio.loadItem(query, event);
        }
    }

    @Override
    protected void execute(@NotNull SlashCommandEvent event) {
        if(!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel() ||
                event.getMember().getVoiceState().getChannel().equals(audio.getVoiceChannel())) {
            String query = event.getOption(optionName).getAsString();
            try {
                URL url = new URL(query);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (IOException e) { // URL is invalid or unreachable
                query = "ytsearch:" + query;
            }

            audio.connect(event.getMember().getVoiceState().getChannel());
            audio.loadItem(query, event);
        }
    }

    protected void doCommand(CommandEvent event) {}
    protected void doCommand(SlashCommandEvent event) {}
}
