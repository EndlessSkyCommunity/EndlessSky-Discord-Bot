package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.ArrayList;

public class Issue extends JamesSlashCommand{

    public Issue() {
        super();
        name = "issue";
        help = "Gets an issue from the ES repo by it's number.";
        arguments = "<number>";
        category = James.lookup;
        aliases = new String[]{"pull", "pr"};

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.INTEGER, "number", "The #number of the issue or pr to get.").setRequired(true));
        this.options = data;
    }

    @Override
    protected void execute(CommandEvent event) {
        String url = James.ES_GITHUB_URL + "issues/" + event.getMessage().getContentDisplay().split(" ")[1];

        int s = Util.getHttpStatus(url);

        if ( (200 <= s && s < 400) || s >= 500)
            event.reply(url);
        else if(s == 404)
            event.reply("Issue not found, make sure you entered the correct number.");
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String url = James.ES_GITHUB_URL + "issues/" + event.getOption("number").getAsString();

        int s = Util.getHttpStatus(url);

        if ( (200 <= s && s < 400) || s >= 500)
            event.reply(url).queue();
        else if(s == 404)
            event.reply("Issue not found, make sure you entered the correct number.").queue();
    }
}
