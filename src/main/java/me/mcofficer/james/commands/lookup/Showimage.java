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

public class Showimage extends ShowCommand {

    public Showimage(Lookups lookups) {
        name = "showimage";
        help = "Outputs the image associated with <query>.";
        arguments = "<query>";
        category = James.lookup;
        this.lookups = lookups;
    }

    protected void reply(DataNode node, CommandEvent event) {
        event.reply(embedImageByNode(node, event.getGuild(), lookups, false).build());
    }
}
