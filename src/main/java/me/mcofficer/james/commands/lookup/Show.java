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

public class Show extends Command {

    private final Lookups lookups;

    public Show(Lookups lookups) {
        name = "show";
        help = "Outputs the image and data associated with <query>.";
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
        checkLength(event, matches.get(0));
    else
        Util.displayNodeSearchResults(matches, event, (((message, integer) -> checkLength(event, matches.get(integer - 1)))));
    }

    private void checkLength(CommandEvent event, DataNode node) {
        String description = lookups.getNodeAsText(node);
        int length = description.length();
        if (length <= 1994) //2000 character limit per message, and we need 6 characters for leading and trailing backticks
            event.reply(createShowMessage(node, event.getGuild(), description));
        else {
            int excess = length % 2000;
            int trailStarts = length - excess;
            Util.sendInChunks(event.getTextChannel(), description.split("(?=\n)"));
            event.reply(createShowMessage(node, event.getGuild(), ""));
        }
    }

    private Message createShowMessage(DataNode node, Guild guild, String description) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(guild.getSelfMember().getColor())
                .setImage(lookups.getImageUrl(node, false));
        return new MessageBuilder()
                .setEmbed(embedBuilder.isEmpty() ? null : embedBuilder.build()) // if no image was found, the embed builder cannot be built
                .append("```")
                .append(description)
                .append("```")
                .build();
    }
}
