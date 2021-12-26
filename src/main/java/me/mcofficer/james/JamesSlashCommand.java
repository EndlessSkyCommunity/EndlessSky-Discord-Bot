package me.mcofficer.james;

import com.jagrosh.jdautilities.command.SlashCommand;
import me.mcofficer.james.James;

public abstract class JamesSlashCommand extends SlashCommand {

    public JamesSlashCommand() {
        super();
        this.guildOnly = true;
        
    }
}