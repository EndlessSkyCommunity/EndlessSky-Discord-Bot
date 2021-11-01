package me.mcofficer.james.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Move extends ManageMessageSlashCommand {

    private static final Logger log = LoggerFactory.getLogger(Move.class);
    private static final String optionNameDest = "destination";
    private static final String optionNameCount = "count";

    public Move() {
        super();
        name = "move";
        help = "Moves X messages to Channel C. Removes Embeds in the process.";
        arguments = "C X";
        aliases = new String[]{"wormhole"};

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.CHANNEL, optionNameDest, "The channel to move messages to.", true));
        data.add(new OptionData(OptionType.INTEGER, optionNameCount, "The number of messages to move.", true));
        this.options = data;
    }

    protected void doMessageCommand(SlashCommandEvent event) {
        OptionMapping destOpt = event.getOption(optionNameDest);
        //MessageChannel dest = destOpt.getAsMessageChannel();
        GuildChannel destGuildC = destOpt.getAsGuildChannel();
        TextChannel dest = (TextChannel)destGuildC;

        long amountL = event.getOption(optionNameCount).getAsLong();
        int amount = ((int)amountL == amountL ? (int)amountL : Integer.MAX_VALUE);
        if (!event.getMember().hasPermission(destGuildC, Permission.MESSAGE_WRITE)
                || !event.getGuild().getSelfMember().hasPermission(destGuildC, Permission.MESSAGE_WRITE)) {
            event.reply(Util.getRandomDeniedMessage()).queue();
            return;
        }
        // Use a lambda to asynchronously perform this request:
        event.getTextChannel().getIterableHistory().takeAsync(amount).thenAccept(toDelete -> {
            if (toDelete.isEmpty())
                return;
            LinkedList<String> toMove = new LinkedList<>();
            for (Message m : toDelete) {
                String authorName = Optional.ofNullable(m.getMember()).map(Member::getEffectiveName).orElse("unknown author");
                String content = m.getContentStripped().trim();
                if (content.isEmpty())
                    continue;
                toMove.addFirst(m.getTimeCreated()
                        .format(DateTimeFormatter.ISO_INSTANT).substring(11, 19)
                        + "Z " + authorName + ": " + content + "\n"
                );
            }

            // Remove the messages from the original channel
            for (CompletableFuture<Void> future : event.getChannel().purgeMessages(toDelete))
            {
                future.exceptionally(t -> {
                    t.printStackTrace();
                    return null;
                });
            }

            EmbedBuilder replyEmbed = new EmbedBuilder();
            replyEmbed.setDescription(dest.getAsMention());
            replyEmbed.setThumbnail("https://cdn.discordapp.com/emojis/344684586904584202.png");
            replyEmbed.appendDescription("\n(" + toMove.size() + " messages await)");
            if (toDelete.size() - toMove.size() > 0)
                replyEmbed.appendDescription("\n(Some embeds were eaten)");
            event.getTextChannel().sendMessage(replyEmbed.build()).queue();

            // Transport the message content to the new channel.
            if (!toMove.isEmpty())
                Util.sendInChunks(dest, toMove, "Incoming wormhole content from " + event.getTextChannel().getAsMention() + ":\n```", "```");

            // Log the move in mod-log.
            String report = "Moved " + toMove.size() +
                    " messages from " + event.getTextChannel().getAsMention() +
                    " to " + dest.getAsMention() + ", ordered by `" +
                    event.getMember().getEffectiveName() + "`.";
            Util.log(event.getGuild(), report);
        }).exceptionally(e -> {
                    log.error("Failed to to move messages", e);
                    return null;
                }
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        int amount;
        TextChannel dest = event.getMessage().getMentionedChannels().get(0);
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            event.reply("Failed to parse \"" + args[1] + "\"as Integer!");
            return;
        }

        if (event.getMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)
                && event.getMember().hasPermission(dest, Permission.MESSAGE_WRITE)
                && event.getGuild().getSelfMember().hasPermission(dest, Permission.MESSAGE_WRITE)) {
            event.getMessage().delete().complete();

            // Use a lambda to asynchronously perform this request:
            event.getTextChannel().getIterableHistory().takeAsync(amount).thenAccept(toDelete -> {
                if (toDelete.isEmpty())
                    return;
                LinkedList<String> toMove = new LinkedList<>();
                for (Message m : toDelete) {
                    String authorName = Optional.ofNullable(m.getMember()).map(Member::getEffectiveName).orElse("unknown author");
                    String content = m.getContentStripped().trim();
                    if (content.isEmpty())
                        continue;
                    toMove.addFirst(m.getTimeCreated()
                            .format(DateTimeFormatter.ISO_INSTANT).substring(11, 19)
                            + "Z " + authorName + ": " + content + "\n"
                    );
                }

                // Remove the messages from the original channel
                for (CompletableFuture<Void> future : event.getChannel().purgeMessages(toDelete))
                {
                    future.exceptionally(t -> {
                        t.printStackTrace();
                        return null;
                    });
                }

                EmbedBuilder replyEmbed = new EmbedBuilder();
                replyEmbed.setDescription(dest.getAsMention());
                replyEmbed.setThumbnail("https://cdn.discordapp.com/emojis/344684586904584202.png");
                replyEmbed.appendDescription("\n(" + toMove.size() + " messages await)");
                if (toDelete.size() - toMove.size() > 0)
                    replyEmbed.appendDescription("\n(Some embeds were eaten)");
                event.getTextChannel().sendMessage(replyEmbed.build()).queue();

                // Transport the message content to the new channel.
                if (!toMove.isEmpty())
                    Util.sendInChunks(dest, toMove, "Incoming wormhole content from " + event.getTextChannel().getAsMention() + ":\n```", "```");

                // Log the move in mod-log.
                String report = "Moved " + toMove.size() +
                        " messages from " + event.getTextChannel().getAsMention() +
                        " to " + dest.getAsMention() + ", ordered by `" +
                        event.getMember().getEffectiveName() + "`.";
                Util.log(event.getGuild(), report);
            }).exceptionally(e -> {
                        log.error("Failed to to move messages", e);
                        return null;
                    }
            );
        } else
            event.reply(Util.getRandomDeniedMessage());
    }
}
