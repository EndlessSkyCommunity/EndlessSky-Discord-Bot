package me.mcofficer.james.commands.moderation;

import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;

public abstract class ModerationSlashCommand extends JamesSlashCommand {

    public ModerationSlashCommand() {
        super();
        this.category = James.moderation;
    }
}
