package me.mcofficer.james.commands.lookup;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.james.commands.lookup.ShowCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.Util;
import me.mcofficer.james.tools.Lookups;

import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Showdata extends ShowCommand {

    public Showdata(Lookups lookups) {
        super(lookups);
        this.name = "showdata";
        this.help = "Outputs the data associated with <query>.";
        this.arguments = "<query>";
    }

    protected void reply(DataNode node, CommandEvent event) {
        Util.sendInChunks(event.getTextChannel(), lookups.getNodeAsText(node).split("(?=\n)"));
    }

    protected void reply(DataNode node, SlashCommandEvent event) {
        Util.sendInChunks(event.getTextChannel(), lookups.getNodeAsText(node).split("(?=\n)"));
    }
}
