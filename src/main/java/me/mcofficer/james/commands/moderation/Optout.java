package me.mcofficer.james.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Optout extends OptRoleCommand {

    public Optout(String[] optinRoles, String timeoutRole) {
        super(optinRoles, timeoutRole);
        name = "optout";
        help = "Removes the user from one or more roles X (, Y, Z)."; //A list of free-to-join roles can be found in the rules.";
        arguments = "X [Y Z]";
    }
    protected void updateRoles(SlashCommandEvent event) {
        List<Role> newRoles = new ArrayList<>(event.getMember().getRoles());
        String requestedRoles = event.getOptions().toString();
        newRoles.removeAll(Util.getOptinRolesByQuery(requestedRoles, event.getGuild(), optinRoles));
        event.getGuild().modifyMemberRoles(event.getMember(), newRoles).queue(success1 ->
                event.reply("Roles removed successfully!").queue()
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Role> newRoles = new ArrayList<>(event.getMember().getRoles());
        newRoles.removeAll(Util.getOptinRolesByQuery(event.getArgs(), event.getGuild(), optinRoles));
        event.getGuild().modifyMemberRoles(event.getMember(), newRoles).queue(success1 ->
                event.getMessage().addReaction("\uD83D\uDC4C").queue(success2 ->
                        event.getMessage().delete().queueAfter(20, TimeUnit.SECONDS)
                )
        );
    }
}
