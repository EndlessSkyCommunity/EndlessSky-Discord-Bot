package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import me.mcofficer.james.tools.Lookups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public abstract class ShowCommand extends Command {

    protected EmbedBuilder embedImageByNode(DataNode node, Guild guild, Lookups lookups, boolean thumbnail) {
        String imageToEmbedUrl = lookups.getImageUrl(node, thumbnail);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(guild.getSelfMember().getColor());
        if (imageToEmbedUrl == null)
            embedBuilder.appendDescription("Couldn't find an image node!\n\n");
        else
            embedBuilder.setImage(imageToEmbedUrl);
        return embedBuilder;
    }


}