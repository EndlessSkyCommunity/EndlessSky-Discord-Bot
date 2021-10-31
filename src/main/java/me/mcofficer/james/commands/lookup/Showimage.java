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

import java.util.function.*;

import java.util.List;

public class Showimage extends ShowCommand {

    public Showimage(Lookups lookups) {
        super(lookups);
        this.name = "showimage";
        this.help = "Outputs the image associated with <query>.";
        this.arguments = "<query>";
    }

    protected void reply(DataNode node, CommandEvent event) {
        event.reply(embedImageByNode(node, event.getGuild(), lookups, false).build());
    }

    protected void reply(DataNode node, SlashCommandEvent event) {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = embedImageByNode(node, event.getGuild(), lookups, false);
        messageBuilder.setEmbeds(embedBuilder.build());
        event.reply(messageBuilder.build()).queue();
        //event.reply(new MessageBuilder().setEmbed(embedImageByNode(node, event.getGuild(), lookups, false).build()).build()).queue();
    }
}
