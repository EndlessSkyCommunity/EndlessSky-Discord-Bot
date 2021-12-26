package me.mcofficer.james.commands.creatortools;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class CRConvert extends JamesSlashCommand {

    public CRConvert() {
        super();
        name = "crconvert";
        //help = "Converts between combat ratings and points.\n`-crconvert cr X` where X is the raw points score returns the corresponding combat rating.\n`-crconvert points X` where X is the combat rating returns the minimum number of points to achieve that rating.\nX must be an integer.";
        help = "Converts between combat ratings and points.";
        category = James.creatorTools;
        arguments = "[cr|points] X";

        this.children = new SlashCommand[]{new CRConvertCR(), new CRConvertPoint()};
    }

    //Slash command related code

    private abstract static class CRConvertHelper extends JamesSlashCommand {
        public CRConvertHelper() {
            super();
        }
        @Override
        protected void execute(SlashCommandEvent event) {
            long value = event.getOptions().get(0).getAsLong();
            if (value < 0) {
                event.reply("You can only enter positive numbers here.").queue();
                return;
            }
            doConversion(event, value);
        }
        protected abstract void doConversion(SlashCommandEvent event, long input);
    }

    private static class CRConvertCR extends CRConvertHelper {
        private static final String optionName = "points";
        public CRConvertCR() {
            super();
            this.name = "rating";
            this.help = "Gets the combat rating of the given number of points.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.INTEGER, optionName, "The number of points to convert to a combat rating.", true));
            this.options = data;
        }
        protected void doConversion(SlashCommandEvent event, long value) {
            double points = (double)value;
            int rating = (points < 1) ? 0 : (int)Math.log(points);
            event.reply(new MessageBuilder().append(String.format("Combat points %s gives a rating of %s.", points, rating)).build()).queue();
        }
    }

    private static class CRConvertPoint extends CRConvertHelper {
        private static final String optionName = "rating";
        public CRConvertPoint() {
            super();
            this.name = "points";
            this.help = "Gets the number of points you need to achieve the given combat rating.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.INTEGER, optionName, "The combat rating to get the required number of points for.", true));
            this.options = data;
        }
        protected void doConversion(SlashCommandEvent event, long rating) {
            long points = (long)Math.ceil(Math.exp(rating));
            event.reply(new MessageBuilder().append(String.format("Combat rating %s requires %s combat points.", rating, points)).build()).queue();
        }
    }

    @Override
    protected void execute(SlashCommandEvent event) {}

    //Old command related code

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