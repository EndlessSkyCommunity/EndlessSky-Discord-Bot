package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.Util;
import me.mcofficer.james.tools.Lookups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.ArrayList;

public abstract class ShowCommand extends JamesSlashCommand {

    protected final Lookups lookups;
    private final String queryName = "query";

    public ShowCommand(Lookups lookups) {
        super();
        this.lookups = lookups;
        this.category = James.lookup;

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.STRING, queryName, "The item to search for.").setRequired(true));
        this.options = data;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<DataNode> matches = lookups.getNodesByString(event.getArgs());

        if (matches.size() < 1)
            event.reply("Found no matches for `" + event.getArgs() + "`!");
        else if (matches.size() == 1)
            reply(matches.get(0), event);
        else
            Util.displayNodeSearchResults(matches, (CommandEvent)event, ((message, integer) -> reply(matches.get(integer - 1), event)));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String query = event.getOption(queryName).getAsString();
        List<DataNode> matches = lookups.getNodesByString(query);

        if (matches.size() < 1)
            event.reply("Found no matches for `" + query + "`!");
        else if (matches.size() == 1)
            reply(matches.get(0), event);
        else
            Util.displayNodeSearchResults(matches, (SlashCommandEvent)event, ((message, integer) -> reply(matches.get(integer - 1), event)));
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

    protected abstract void reply(DataNode node, SlashCommandEvent event);

}
