package me.mcofficer.james.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Ping extends JamesSlashCommand {

    public Ping() {
        super();
        name = "ping";
        help = "Displays the time of the bot's last heartbeat.";
        category = James.info;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setDescription("Last Heartbeat took " + event.getJDA().getGatewayPing() + "ms.")
                .setColor(event.getGuild().getSelfMember().getColor())
                .build()
        ).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setDescription("Last Heartbeat took " + event.getJDA().getGatewayPing() + "ms.")
                .setColor(event.getGuild().getSelfMember().getColor())
                .build()
        );
    }
}
