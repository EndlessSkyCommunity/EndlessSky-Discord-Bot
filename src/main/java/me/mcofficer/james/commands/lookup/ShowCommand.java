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

    protected final Lookups lookups;
    
    public ShowCommand(Lookups lookups) {
        this.lookups = lookups;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<DataNode> matches = lookups.getNodesByString(event.getArgs());

        if (matches.size() < 1)
            event.reply("Found no matches for `" + event.getArgs() + "`!");
        else if (matches.size() == 1)
            reply(matches.get(0), event);
        else
            Util.displayNodeSearchResults(matches, event, ((message, integer) -> reply(matches.get(integer - 1), event)));
    }

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
    
    protected abstract void reply(DataNode node, CommandEvent event);

}
