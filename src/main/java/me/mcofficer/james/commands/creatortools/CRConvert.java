package me.mcofficer.james.commands.creatortools;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import net.dv8tion.jda.api.MessageBuilder;

import java.lang.Math;
import java.util.Arrays;

public class CRConvert extends Command {

    public CRConvert() {
        name = "crconvert";
        help = "Converts between combat ratings and points.\n`-crconvert cr X` where X is the raw points score returns the corresponding combat rating.\n`-crconvert points X` where X is the combat rating returns the minimum number of points to achieve that rating.\nX must be an integer.";
        category = James.creatorTools;
        arguments = "[cr|points] X";
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        if (args == null) {
            invalidInputPrintHelp(event);
        }
        String[] argsList = args.split(" ");
        if (argsList.length < 2) {
            invalidInputPrintHelp(event);
            return;
        }
        String X = argsList[1];
        int value = 0;
        try {
            value = Integer.parseInt(X);
        }
        catch (NumberFormatException e) {
            invalidInputPrintHelp(event);
            return;
        }
        if (argsList[0].equals("cr")) {
            int rating = getRatingFromPoints(value);
            event.reply(new MessageBuilder().append(String.format("Combat points %s gives a rating of %s.", value, rating)).build());
        }
        else if (argsList[0].equals("points")) {
            int points = getPointsFromRating(value);
            event.reply(new MessageBuilder().append(String.format("Combat rating %s requires %s combat points.", value, points)).build());
        }
    }

    private int getRatingFromPoints(int points) {
        return (int)Math.log(points);
    }

    private int getPointsFromRating(int rating) {
        return (int)Math.ceil(Math.exp(rating));
    }

    private void invalidInputPrintHelp(CommandEvent event) {
        event.reply(new MessageBuilder().append("That's not how this works.\n").append(help).build());
    }


}