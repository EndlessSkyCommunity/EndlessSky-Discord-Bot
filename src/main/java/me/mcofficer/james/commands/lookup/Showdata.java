package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.commands.lookup.ShowCommand;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import me.mcofficer.james.tools.Lookups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Showdata extends ShowCommand {

    private final Lookups lookups;

    public Showdata(Lookups lookups) {
        name = "showdata";
        help = "Outputs the data associated with <query>.";
        arguments = "<query>";
        category = James.lookup;
        this.lookups = lookups;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<DataNode> matches = lookups.getNodesByString(event.getArgs());

        if (matches.size() < 1)
            event.reply("Found no matches for `" + event.getArgs() + "`!");
        else if (matches.size() == 1)
            Util.sendInChunks(event.getTextChannel(), lookups.getNodeAsText(matches.get(0)).split("(?=\n)"));
        else
            Util.displayNodeSearchResults(matches, event, ((message, integer) ->
                    Util.sendInChunks(event.getTextChannel(), lookups.getNodeAsText(matches.get(integer - 1)).split("(?=\n)"))));
    }
}
