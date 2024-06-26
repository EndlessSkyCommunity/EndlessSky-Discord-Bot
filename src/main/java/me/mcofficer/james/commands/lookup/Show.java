package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.commands.lookup.ShowCommand;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import me.mcofficer.james.tools.Lookups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class Show extends ShowCommand {

    public Show(Lookups lookups) {
        super(lookups);
        name = "show";
        help = "Outputs the image and data associated with <query>.";
        arguments = "<query>";
        category = James.lookup;
    }

    protected void reply(DataNode node, CommandEvent event) {
        Guild guild = event.getGuild();
        EmbedBuilder embedBuilder = embedImageByNode(node, guild, lookups, false);
        event.reply(new MessageBuilder()
                .setEmbed(embedBuilder.isEmpty() ? null : embedBuilder.build()) // if no image was found, the embed builder cannot be built
                .append(Util.sendInChunksReturnLast(event.getTextChannel(), lookups.getNodeAsText(node).split("(?=\n)")))
                .build());
    }
}
