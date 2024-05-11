package me.mcofficer.james.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;

public class UnixTime extends Command {

    public UnixTime() {
        name = "unix-time";
        help = "Returns the unix time representation of the current time, or the time X units after this time. The unit for the time offset is given as a single, case-insensitive character: 'S' for seconds, 'M' for minutes, 'H' for hours, 'D' for days, 'W' for weeks.";
        arguments = "[X [U]]";
        category = James.misc;
    }

    @Override
    protected void execute(CommandEvent event) {
        long unixTime = event.getMessage().getTimeCreated().toEpochSecond();
        unixTime += GetOffset(event);
        event.reply(Long.toString(unixTime));
    }

    private static long GetOffset(CommandEvent event) {
        String argString = event.getArgs();
        if(argString.isEmpty())
            return 0;
        String[] args = argString.split(" ");
        long offset = 0;
        try {
            offset = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            event.reply("Failed to parse \"" + args[1] + "\" as Long!");
            return 0;
        }
        if(args.length > 1)
            return offset * GetMultiplier(args[1]);
        return offset;
    }

    private static int GetMultiplier(String multiplierString) {
        if(multiplierString.isEmpty())
            return 1;
        char multiplier = multiplierString.toUpperCase().charAt(0);
        switch (multiplier) {
            case 'S':
                return 1;
            case 'M':
                return 60;
            case 'H':
                return 60 * 60;
            case 'D':
                return 24 * 60 * 60;
            case 'W':
                return 7 * 24 * 60 * 60;
            default:
                return 1;
        }
    }
}
