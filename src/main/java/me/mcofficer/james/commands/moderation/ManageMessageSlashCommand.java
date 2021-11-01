package me.mcofficer.james.commands.moderation;

import me.mcofficer.james.Util;
import me.mcofficer.james.commands.ModerationSlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public abstract class ManageMessageSlashCommand extends ModerationSlashCommand {
    public ManageMessageSlashCommand() {
        super();
    }
    @Override
    protected void execute(SlashCommandEvent event) {
        if (!event.getMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            event.reply(Util.getRandomDeniedMessage()).queue();
            return;
        }
        doMessageCommand(event);
    }
    protected abstract void doMessageCommand(SlashCommandEvent event);
}
