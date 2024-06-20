package me.mcofficer.james.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Purge extends Command {

    public Purge() {
        name = "purge";
        help = "Purges the last X messages from the current channel.";
        arguments = "X";
        category = James.moderation;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            try {
                int amount = Integer.parseInt(event.getArgs());
                if (amount < 2 || 100 < amount)
                    throw new NumberFormatException();
                TextChannel channel = event.getTextChannel();

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

					TextChannel dest = event.getGuild().getTextChannelsByName("mod-log", true).get(0);
	
					// Transport the message content to the new channel.
					if (!toMove.isEmpty())
						Util.sendInChunks(dest, toMove, "Incoming purged content from " + event.getTextChannel().getAsMention() + ":\n```", "```");
				});
            }
            catch (NumberFormatException e) {
                event.reply("'" + event.getArgs() + "' is not a valid integer between 2 and 100!");
            }
        }
        else {
            event.reply(Util.getRandomDeniedMessage());
        }
    }
}
