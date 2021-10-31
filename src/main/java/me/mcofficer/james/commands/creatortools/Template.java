package me.mcofficer.james.commands.creatortools;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Template extends JamesSlashCommand {

    private static final String optionName = "key";
    private HashMap<String, String> templates = new HashMap<>() {
        {
            put("plugin", "exampleplugin.zip");
            put("outfit", "outfittemplate.blend");
            put("ship", "shiptemplate.blend");
            put("thumbnail", "thumbnail.blend");
        }};

    public Template() {
        name = "template";
        //help = "Serves a template X for content creators. Available templates are: "
        //        + String.join(", ", templates.keySet());
        help = "Serves the chosen template.";
        category = James.creatorTools;
        arguments = "X";

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.STRING, optionName, "The key for the template to get.", true).addChoices(generateChoices()));
        this.options = data;
    }

    private List<Command.Choice> generateChoices() {
        List<Command.Choice> choices = new ArrayList<>();
        String[] keys = templates.keySet().toArray(new String[0]);
        for (String key : keys) {
            choices.add(new Command.Choice(key, key));
        }
        return choices;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String key = event.getOption(optionName).getAsString();
        String url = James.GITHUB_RAW_URL + "data/templates/" + templates.get(key);
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(templates.get(key), url)
            .setColor(event.getGuild().getSelfMember().getColor())
            .setDescription("Here's your template, served hot and crunchy :)")
            .build()
        ).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String key = event.getArgs().toLowerCase();
        if (templates.containsKey(key)) {
            String url = James.GITHUB_RAW_URL + "data/templates/" + templates.get(key);
            event.reply(new EmbedBuilder()
                    .setTitle(templates.get(key), url)
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setDescription("Here's your template, served hot and crunchy :)")
                    .build()
            );
        }
        else
            event.reply(String.format("Which template would you like? I have the following flavours available: %s.",
                    String.join(", ", templates.keySet())));
    }
}
