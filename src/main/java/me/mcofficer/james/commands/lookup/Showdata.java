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

    public Showdata(Lookups lookups) {
        name = "showdata";
        help = "Outputs the data associated with <query>.";
        arguments = "<query>";
        category = James.lookup;
        this.lookups = lookups;
    }

    protected void reply(DataNode node, CommandEvent event) {
        Util.sendInChunks(event.getTextChannel(), lookups.getNodeAsText(node).split("(?=\n)"));
    }
}
