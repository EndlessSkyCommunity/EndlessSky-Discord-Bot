package me.mcofficer.james.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.entities.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Activity extends Command {

    private final String[] ontopicCategories;

    public Activity(String[] ontopicCategories) {
        name = "activity";
        arguments = "[member]";
        help = "Provides stats about Member Activity over the last 2 weeks in ontopic channels. For Mods only, has 30 seconds cooldown.";
        cooldown = 30;
        category = James.moderation;
        this.ontopicCategories = ontopicCategories;
    }

    @Override
    protected void execute(CommandEvent event) {
        // only allowed for Mods
        if (event.getMember().getRoles().get(0).getPosition() < event.getGuild().getSelfMember().getRoles().get(0).getPosition()) {
            event.reply(Util.getRandomDeniedMessage());
            return;
        }

        OffsetDateTime cutoffTime = OffsetDateTime.ofInstant(Instant.now().minusSeconds(1209600L), ZoneId.systemDefault());

        List<Message> messages = new ArrayList<>();
        for (String catId : ontopicCategories) {
            net.dv8tion.jda.api.entities.Category category = event.getJDA().getCategoryById(catId);
            if (category != null) {
                for (TextChannel channel : category.getTextChannels()) {
                    try {
                        messages.addAll(
                                channel
                                        .getIterableHistory()
                                        .takeUntilAsync(msg -> msg.getTimeCreated().isBefore(cutoffTime))
                                        .get()
                        );
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                event.reply("Failed to find category with ID " + catId + ", results will be inaccurate. Please contact my hoster.");
            }
        }

        Map<Member, Integer> results = new HashMap<>();
        for (Message msg : messages) {
            if (msg.getMember() != null) {
                results.merge(msg.getMember(), 1, Integer::sum);
            }
        }

        try {
            Member query = event.getMessage().getMentionedMembers().get(0);
            if (results.containsKey(query)) {
                event.reply(query.getEffectiveName() + " sent " + results.getOrDefault(query, 0) + " Messages in Ontopic-Channels over the last 2 weeks.");
            }
        } catch (IndexOutOfBoundsException oob) {
            int maxLength = results
                    .keySet()
                    .stream()
                    .max(Comparator.comparingInt(o -> o.getEffectiveName().length()))
                    .orElseThrow()
                    .getEffectiveName()
                    .length();
            String formatString = "%-" + (maxLength + 5) + "s%04d\n";

            List<String> tableLines = results.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .map(e -> String.format(formatString, e.getKey().getEffectiveName(), e.getValue()))
                    .collect(Collectors.toList());
            Util.sendInChunks(event.getTextChannel(), tableLines, "```", "```");
        }
    }
}
