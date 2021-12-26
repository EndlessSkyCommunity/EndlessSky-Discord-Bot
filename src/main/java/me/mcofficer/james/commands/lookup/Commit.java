package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class Commit extends JamesSlashCommand{

    public Commit() {
        super();
        name = "commit";
        help = "Gets a commit from the ES repo by it's hash.";
        arguments = "<hash>";
        category = James.lookup;

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.STRING, "hash", "The hash of the commit to lookup.").setRequired(true));
        this.options = data;
    }

    @Override
    protected void execute(CommandEvent event) {
        String url = James.ES_GITHUB_URL + "commit/" + event.getMessage().getContentDisplay().split(" ")[1];

        int s = Util.getHttpStatus(url);

        if ( (200 <= s && s < 400) || s >= 500)
            event.reply(url);
        else if(s == 404)
            event.reply("Commit not found, make sure you entered a valid commithash.");
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String url = James.ES_GITHUB_URL + "commit/" + event.getOption("hash").getAsString();

        int s = Util.getHttpStatus(url);

        if ( (200 <= s && s < 400) || s >= 500)
            event.reply(url).queue();
        else if(s == 404)
            event.reply("Commit not found, make sure you entered a valid commithash.").queue();
    }
}
