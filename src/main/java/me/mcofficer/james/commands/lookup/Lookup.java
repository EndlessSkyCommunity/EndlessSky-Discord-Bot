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

    public Lookup(Lookups lookups) {
        name = "lookup";
        help = "Outputs the image and description of <query>.";
        arguments = "<query>";
        category = James.lookup;
        this.lookups = lookups;
    }

    protected void reply(DataNode node, CommandEvent event) {
        Guild guild = event.getGuild();
        EmbedBuilder embedBuilder = embedImageByNode(node, guild, lookups, true);
        String description = lookups.getDescription(node);
        
        if (description == null)
            embedBuilder.appendDescription("Couldn't find a description node!");
        else
            embedBuilder.setDescription(description);

        embedBuilder.appendDescription("\n\n" + lookups.getLinks(node));

        event.reply(embedBuilder.build());
    }
}
