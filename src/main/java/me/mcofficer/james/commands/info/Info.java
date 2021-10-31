package me.mcofficer.james.commands.info;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Info extends JamesSlashCommand {

    private String commit;

    public Info(String githubToken) {
        name = "info";
        help = "Shows information about the bot.";
        category = James.info;
        getInfo(githubToken);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String description = String.format(
                "- **Author:** Maximilian Korber\n" +
                        "- **Language**: Java\n" +
                        "- **Utilized Libraries:** JDA, JDA-Utilities, ESParser-java\n" +
                        "- **Maintainers:** M\\*C\\*O, tehhowch\n" +
                        "\n" +
                        "**Latest Commit:** %s\n" +
                        "\n" +
                        "[View known issues and feature requests](%s)",
                commit, James.GITHUB_URL + "issues/");

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setColor(event.getGuild().getSelfMember().getColor())
                .setDescription(description);
        event.replyEmbeds(builder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String description = String.format(
                        "- **Author:** Maximilian Korber\n" +
                        "- **Language**: Java\n" +
                        "- **Utilized Libraries:** JDA, JDA-Utilities, ESParser-java\n" +
                        "- **Maintainers:** M\\*C\\*O, tehhowch\n" +
                        "\n" +
                        "**Latest Commit:** %s\n" +
                        "\n" +
                        "[View known issues and feature requests](%s)",
                        commit, James.GITHUB_URL + "issues/");

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setColor(event.getGuild().getSelfMember().getColor())
                .setDescription(description);
        event.reply(builder.build());
    }

    private void getInfo(String githubToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token "+ githubToken);
        JSONObject latest = new JSONArray(Util.getContentFromUrl("https://api.github.com/repos/EndlessSkyCommunity/EndlessSky-Discord-Bot/commits", headers))
                .getJSONObject(0);
        commit = String.format("[%s](%s): %s", latest.getString("sha").substring(0, 7), latest.getString("html_url"),
                latest.getJSONObject("commit").getString("message"));
    }
}
