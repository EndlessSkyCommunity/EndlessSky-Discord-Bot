package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.commands.lookup.ShowCommand;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import me.mcofficer.james.tools.Lookups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class Lookup extends ShowCommand {

    private final Lookups lookups;

    public Lookup(Lookups lookups) {
        name = "lookup";
        help = "Outputs the image and description of <query>.";
        arguments = "<query>";
        category = James.lookup;
        this.lookups = lookups;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<DataNode> matches = lookups.getNodesByString(event.getArgs());

        if (matches.size() < 1)
            event.reply("Found no matches for `" + event.getArgs() + "`!");
        else if (matches.size() == 1)
            event.reply(createLookupMessage(matches.get(0), event.getGuild()));
        else
            Util.displayNodeSearchResults(matches, event, (message, integer) -> event.reply(createLookupMessage(matches.get(integer - 1), event.getGuild())));
    }

    private MessageEmbed createLookupMessage(DataNode node, Guild guild) {
        EmbedBuilder embedBuilder = embedImageByNode(node, guild, lookups, true);
        String description = lookups.getDescription(node);
        
        if (description == null)
            embedBuilder.appendDescription("Couldn't find a description node!");
        else
            embedBuilder.setDescription(description);

        embedBuilder.appendDescription("\n\n" + lookups.getLinks(node));

        return embedBuilder.build();
    }
}
