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

public class Optin extends OptRoleCommand {

    public Optin(String[] optinRoles, String timeoutRole) {
        super(optinRoles, timeoutRole);
        name = "optin";
        help = "Adds the user to one or more roles X (, Y, Z)."; //A list of free-to-join roles can be found in the rules.";
        arguments = "X [Y Z]";
    }

    protected void updateRoles(SlashCommandEvent event) {
        List<Role> newRoles = new ArrayList<>(event.getMember().getRoles());
        String rolesRequested = event.getOptions().toString();
        newRoles.addAll(Util.getOptinRolesByQuery(rolesRequested, event.getGuild(), optinRoles));
        event.getGuild().modifyMemberRoles(event.getMember(), newRoles).queue(success1 ->
                event.reply("Roles added successfully!").queue()
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        Role timeoutRole = event.getGuild().getRolesByName(sTimeoutRole, true). get(0);
        if (event.getMember().getRoles().contains(timeoutRole)) {
            event.reply(Util.getRandomDeniedMessage());
            return;
        }
        
        List<Role> newRoles = new ArrayList<>(event.getMember().getRoles());
        newRoles.addAll(Util.getOptinRolesByQuery(event.getArgs(), event.getGuild(), optinRoles));
        event.getGuild().modifyMemberRoles(event.getMember(), newRoles).queue(success1 ->
                event.getMessage().addReaction("\uD83D\uDC4C").queue(success2 ->
                        event.getMessage().delete().queueAfter(20, TimeUnit.SECONDS)
                )
        );
    }
}
