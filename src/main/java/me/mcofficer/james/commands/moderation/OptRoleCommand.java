package me.mcofficer.james.commands.moderation;

import me.mcofficer.james.Util;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public abstract class OptRoleCommand extends ModerationSlashCommand {
    protected final String[] optinRoles;
    protected final String sTimeoutRole;
    public OptRoleCommand(String[] optinRoles, String timeoutRole) {
        super();
        this.optinRoles = optinRoles;
        this.sTimeoutRole = timeoutRole;

        List<OptionData> data = getOptinRolesAsOptions();
        data.get(0).setRequired(true);
        this.options = data;
    }
    protected List<Command.Choice> getOptinRolesAsChoiceList() {
        List<Command.Choice> choices = new ArrayList<>();
        for (String role : optinRoles) {
            choices.add(new Command.Choice(role, role));
        }
        return choices;
    }
    protected List<OptionData> getOptinRolesAsOptions() {
        List<OptionData> data = new ArrayList<>();
        List<Command.Choice> choices = getOptinRolesAsChoiceList();
        for (Command.Choice choice : choices) {
            data.add(new OptionData(OptionType.STRING, "role", "The role add/remove.", false).addChoices(choices));
        }
        return data;
    }
    @Override
    protected void execute(SlashCommandEvent event) {
        Role timeoutRole = event.getGuild().getRolesByName(sTimeoutRole, true). get(0);
        if (event.getMember().getRoles().contains(timeoutRole)) {
            event.reply(Util.getRandomDeniedMessage()).queue();
            return;
        }
        updateRoles(event);
    }
    protected abstract void updateRoles(SlashCommandEvent event);
}
