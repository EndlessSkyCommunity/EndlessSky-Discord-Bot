package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Queue extends AudioCommand {

    public Queue(Audio audio) {
        super(audio);
        name = "queue";
        help = "Displays the current queue, or returns it in a file.";

        this.children = new SlashCommand[]{new displayQueue(audio), new printQueue(audio)};
    }

    protected void doCommand(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args[0].equalsIgnoreCase("print")) {
            String fileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (fileName.length() == 0)
                fileName = "queue.txt";
            else if (!fileName.endsWith(".txt"))
                fileName += ".txt";
            audio.sendQueueFile(event, fileName);
        }
        else
            audio.createQueueEmbed(event);
    }

    protected void doCommand(SlashCommandEvent event) {}

    private static class displayQueue extends AudioCommand {
        public displayQueue(Audio audio) {
            super(audio);
            this.name = "display";
            this.help = "Displays the queue.";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            audio.createQueueEmbed(event);
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }

    private static class printQueue extends AudioCommand {

        public printQueue(Audio audio) {
            super(audio);
            this.name = "print";
            this.help = "Returns a text file containing the Queue and currently playing song.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.STRING, "filename", "The name to give the returned file. Defaults to 'queue.txt'.", false));
            this.options = data;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String fileName = event.getOptions().get(0).getAsString();
            if (fileName.length() == 0)
                fileName = "queue.txt";
            else if (!fileName.endsWith(".txt"))
                fileName += ".txt";
            audio.sendQueueFile(event, fileName);
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }
}
