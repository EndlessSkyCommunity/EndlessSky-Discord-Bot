package me.mcofficer.james.commands.moderation;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.Util;
import me.mcofficer.james.commands.ModerationSlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class Timeout extends ModerationSlashCommand {

    private final String sTimeoutRole;
    private static final String optionNameUser = "user";
    private static final String optionNameTime = "time";
    private static final String optionNameTimeUnits = "timeunits";
    private static final String optionNameReason = "reason";
    private static final String[] unitsChoiceNames = new String[] {"Seconds", "Minutes", "Hours", "Days", "Weeks"};
    private static final long[] unitsChoiceValues = new long[] {1L, 60L, 3600L, 86400L, 604800L};

    public Timeout(String timeoutRole) {
        super();
        name = "timeout";
        help = "Sends the Member(s) X [Y, Z] for S seconds to #the-corner. S must always be the last argument.";
        arguments = "X [Y Z] S";
        sTimeoutRole = timeoutRole;

        List<OptionData> data = new ArrayList<>();
        data.add(new OptionData(OptionType.USER, optionNameUser, "The user to timeout.", true));
        data.add(new OptionData(OptionType.INTEGER, optionNameTime, "How long to timeout the user for.", true));
        OptionData units = new OptionData(OptionType.INTEGER, optionNameTimeUnits, "The units for the timeout time.", true);
        List<Command.Choice> unitChoices = new ArrayList<>();
        int unitCount = unitsChoiceValues.length;
        for (int i = 0; i < unitCount - 1; i++) {
            unitChoices.add(new Command.Choice(unitsChoiceNames[i], unitsChoiceValues[i]));
        }
        units.addChoices(unitChoices);
        data.add(units);
        data.add(new OptionData(OptionType.STRING, optionNameReason, "The reason for the timeout.", false));
        this.options = data;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply(Util.getRandomDeniedMessage()).queue();
            return;
        }
        Role timeoutRole = event.getGuild().getRolesByName(sTimeoutRole, true).get(0);
        Member toTimeout = event.getOption(optionNameUser).getAsMember();
        long time = event.getOption(optionNameTime).getAsLong() * event.getOption(optionNameTimeUnits).getAsLong();
        String onCommand = String.format("Sent Member %s to the corner for %s seconds (Ordered by `%s#%s`).",
                toTimeout.getAsMention(), time, event.getMember().getUser().getName(), event.getMember().getUser().getDiscriminator());
        String onRelease = "Released Member " + toTimeout.getAsMention() + " from the corner.";
        Util.replaceRolesTemporarily(timeoutRole, time, toTimeout, onCommand, onRelease);
    }

    @Override
    protected void execute(CommandEvent event) {
        Role timeoutRole = event.getGuild().getRolesByName(sTimeoutRole, true).get(0);

        if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply(Util.getRandomDeniedMessage());
            return;
        }

        List<Member> toTimeout = event.getMessage().getMentionedMembers();
        String[] args = event.getArgs().split(" ");
        long time;
        try {
            time = Long.parseLong(args[toTimeout.size()]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            event.reply("Failed to parse \"" + args[toTimeout.size()] + "\" as Long!");
            return;
        }

        for (Member member : toTimeout) {
            String onCommand = String.format("Sent Member %s to the corner for %s seconds (Ordered by `%s#%s`).",
                    member.getAsMention(), time, event.getMember().getUser().getName(), event.getMember().getUser().getDiscriminator());
            String onRelease = "Released Member " + member.getAsMention() + " from the corner.";
            Util.replaceRolesTemporarily(timeoutRole, time, member, onCommand, onRelease);
        }
    }
}
