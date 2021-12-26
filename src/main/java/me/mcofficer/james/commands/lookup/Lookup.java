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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.MessageBuilder;

import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class Lookup extends ShowCommand {

    public Lookup(Lookups lookups) {
        super(lookups);
        this.name = "lookup";
        this.help = "Outputs the image and description of <query>.";

        //Not supported with slash commands
        this.arguments = "<query>";

        //New with slash commands
        
    }

    protected void reply(DataNode node, CommandEvent event) {
        EmbedBuilder embedBuilder = embedImageByNode(node, event.getGuild(), lookups, true);
        String description = lookups.getDescription(node);
        
        if (description == null)
            embedBuilder.appendDescription("Couldn't find a description node!");
        else
            embedBuilder.setDescription(description);

        embedBuilder.appendDescription("\n\n" + lookups.getLinks(node));

        event.reply(embedBuilder.build());
    }

    protected void reply(DataNode node, SlashCommandEvent event) {
        EmbedBuilder embedBuilder = embedImageByNode(node, event.getGuild(), lookups, true);
        String description = lookups.getDescription(node);
        
        if (description == null)
            embedBuilder.appendDescription("Couldn't find a description node!");
        else
            embedBuilder.setDescription(description);

        embedBuilder.appendDescription("\n\n" + lookups.getLinks(node));

        event.replyEmbeds(embedBuilder.build()).queue();
        //event.reply(new MessageBuilder().setEmbeds(embedBuilder.build()).build()).queue();
    }
}
