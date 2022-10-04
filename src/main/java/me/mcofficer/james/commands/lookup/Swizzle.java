package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.James;
import me.mcofficer.james.tools.Lookups;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Swizzle extends Command {

    private HashMap<Integer, Map.Entry<String, List<DataNode>>> vectors = new HashMap<>();
    private final Lookups lookups;


    public Swizzle(Lookups lookups) {
        name = "swizzle";
        help = "Displays information about a swizzle X (can range from 0-28)";
        arguments = "X";
        category = James.lookup;
        this.lookups = lookups;
        initVectors();
    }

    private void initVectors() {
        String[] vectorStrings = new String[]{
		    "{GL_RED, GL_GREEN, GL_BLUE, GL_ALPHA}, // 0 red + yellow markings (republic)",
		    "{GL_RED, GL_BLUE, GL_GREEN, GL_ALPHA}, // 1 red + magenta markings",
		    "{GL_GREEN, GL_RED, GL_BLUE, GL_ALPHA}, // 2 green + yellow (free worlds)",
		    "{GL_BLUE, GL_RED, GL_GREEN, GL_ALPHA}, // 3 green + cyan",
		    "{GL_GREEN, GL_BLUE, GL_RED, GL_ALPHA}, // 4 blue + magenta (syndicate)",
    		"{GL_BLUE, GL_GREEN, GL_RED, GL_ALPHA}, // 5 blue + cyan (merchant)",
		    "{GL_GREEN, GL_BLUE, GL_BLUE, GL_ALPHA}, // 6 red and black (pirate)",
		    "{GL_RED, GL_BLUE, GL_BLUE, GL_ALPHA}, // 7 pure red",
		    "{GL_RED, GL_GREEN, GL_GREEN, GL_ALPHA}, // 8 faded red",
		    "{GL_BLUE, GL_BLUE, GL_BLUE, GL_ALPHA}, // 9 pure black",
		    "{GL_GREEN, GL_GREEN, GL_GREEN, GL_ALPHA}, // 10 faded black",
		    "{GL_RED, GL_RED, GL_RED, GL_ALPHA}, // 11 pure white",
		    "{GL_BLUE, GL_BLUE, GL_GREEN, GL_ALPHA}, // 12 darkened blue",
		    "{GL_BLUE, GL_BLUE, GL_RED, GL_ALPHA}, // 13 pure blue",
		    "{GL_GREEN, GL_GREEN, GL_RED, GL_ALPHA}, // 14 faded blue",
		    "{GL_BLUE, GL_GREEN, GL_GREEN, GL_ALPHA}, // 15 darkened cyan",
		    "{GL_BLUE, GL_RED, GL_RED, GL_ALPHA}, // 16 pure cyan",
		    "{GL_GREEN, GL_RED, GL_RED, GL_ALPHA}, // 17 faded cyan",
		    "{GL_BLUE, GL_GREEN, GL_BLUE, GL_ALPHA}, // 18 darkened green",
		    "{GL_BLUE, GL_RED, GL_BLUE, GL_ALPHA}, // 19 pure green",
		    "{GL_GREEN, GL_RED, GL_GREEN, GL_ALPHA}, // 20 faded green",
		    "{GL_GREEN, GL_GREEN, GL_BLUE, GL_ALPHA}, // 21 darkened yellow",
		    "{GL_RED, GL_RED, GL_BLUE, GL_ALPHA}, // 22 pure yellow",
		    "{GL_RED, GL_RED, GL_GREEN, GL_ALPHA}, // 23 faded yellow",
		    "{GL_GREEN, GL_BLUE, GL_GREEN, GL_ALPHA}, // 24 darkened magenta",
		    "{GL_RED, GL_BLUE, GL_RED, GL_ALPHA}, // 25 pure magenta",
		    "{GL_RED, GL_GREEN, GL_RED, GL_ALPHA}, // 26 faded magenta",
		    "{GL_BLUE, GL_ZERO, GL_ZERO, GL_ALPHA}, // 27 red only (cloaked)",
		    "{GL_ZERO, GL_ZERO, GL_ZERO, GL_ALPHA} // 28 black only (outline)"
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
}
