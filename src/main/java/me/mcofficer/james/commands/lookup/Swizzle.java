package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.tools.Lookups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Swizzle extends JamesSlashCommand {

    private HashMap<Integer, Map.Entry<String, List<DataNode>>> vectors = new HashMap<>();
    private final Lookups lookups;


    public Swizzle(Lookups lookups) {
        super();
        name = "swizzle";
        help = "Displays information about a swizzle X (can range from 0-8)";
        arguments = "X";
        category = James.lookup;
        this.lookups = lookups;
        initVectors();

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.INTEGER, "swizzle", "The number of the swizzle to look up. Ranges from 0-8.").setRequired(true));
        this.options = data;
    }

    private void initVectors() {
        String[] vectorStrings = new String[]{
                "{GL_RED, GL_GREEN, GL_BLUE, GL_ALPHA} // red + yellow markings (republic)",
                "{GL_RED, GL_BLUE, GL_GREEN, GL_ALPHA} // red + magenta markings",
                "{GL_GREEN, GL_RED, GL_BLUE, GL_ALPHA} // green + yellow (freeholders)",
                "{GL_BLUE, GL_RED, GL_GREEN, GL_ALPHA} // green + cyan",
                "{GL_GREEN, GL_BLUE, GL_RED, GL_ALPHA} // blue + magenta (syndicate)",
                "{GL_BLUE, GL_GREEN, GL_RED, GL_ALPHA} // blue + cyan (merchant)",
                "{GL_GREEN, GL_BLUE, GL_BLUE, GL_ALPHA} // red and black (pirate)",
                "{GL_BLUE, GL_ZERO, GL_ZERO, GL_ALPHA} // red only (cloaked)",
                "{GL_ZERO, GL_ZERO, GL_ZERO, GL_ALPHA} // black only (outline)"
        };

        for (int i = 0; i < vectorStrings.length; i++)
            vectors.put(i, Map.entry(vectorStrings[i], lookups.getGovernmentsBySwizzle(i)));
    }

    @Override
    protected void execute(CommandEvent event) {
        int swizzle = Integer.parseInt(event.getArgs());

        if (!vectors.containsKey(swizzle))
            event.reply("Swizzle not found!");
        else {
            Map.Entry<String, List<DataNode>> vector = vectors.get(swizzle);
            StringBuilder govStringBuilder = new StringBuilder();
            for (DataNode node : vector.getValue())
                govStringBuilder.append("\n\u2022 ")
                        .append(String.join(" ", node.getTokens().subList(1, node.getTokens().size())));

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setDescription(String.format("**Swizzle Vector:**\n```%s```\n\n**Governments using this swizzle:**\n%s",
                            vector.getKey(), govStringBuilder.toString()))
                    .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/swizzles/" + swizzle + ".png");
            event.reply(embedBuilder.build());
        }
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int swizzle = (int)event.getOption("swizzle").getAsLong();

        if (!vectors.containsKey(swizzle))
            event.reply("Swizzle not found!");
        else {
            Map.Entry<String, List<DataNode>> vector = vectors.get(swizzle);
            StringBuilder govStringBuilder = new StringBuilder();
            for (DataNode node : vector.getValue())
                govStringBuilder.append("\n\u2022 ")
                        .append(String.join(" ", node.getTokens().subList(1, node.getTokens().size())));

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setDescription(String.format("**Swizzle Vector:**\n```%s```\n\n**Governments using this swizzle:**\n%s",
                            vector.getKey(), govStringBuilder.toString()))
                    .setThumbnail(James.GITHUB_RAW_URL + "thumbnails/swizzles/" + swizzle + ".png");
            event.replyEmbeds(embedBuilder.build()).queue();
        }
	}
}
