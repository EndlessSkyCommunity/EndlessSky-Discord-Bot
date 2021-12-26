package me.mcofficer.james.commands.creatortools;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.tools.ImageSwizzler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SwizzleImage extends JamesSlashCommand {

    private final ImageSwizzler swizzler = new ImageSwizzler();
    private final String optionNameSwizzleNumber = "swizzle";

    public SwizzleImage() {
        super();
        name = "swizzleimage";
        help = "Applies the Swizzle X to the uploaded image[s]. If X is not defined, applies swizzles 1-6.";
        arguments = "[X] <attached images>";
        category = James.creatorTools;

        List<OptionData> data = new ArrayList<>();
        //OptionData swizzles = new OptionData(OptionType.INTEGER, optionNameSwizzleNumber, "The number of the swizzle to apply, leave blank to apply them all.", false);
        //List<Command.Choice> swizzleChoices = new ArrayList<>();
        //for (int i = 1; i <= 6; i++) {
            //swizzleChoices.add(new Command.Choice(Integer.toString(i), i));
        //}
        //swizzles.addChoices(swizzleChoices);
        //data.add(swizzles);
        data.add(new OptionData(OptionType.STRING, optionNameSwizzleNumber, "The swizzle(s) to apply. Leave blank for all.", false));
        this.options = data;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Member requester = event.getMember();

        event.getChannel().getIterableHistory().takeAsync(1).thenAccept(latestMessage -> {
            if (latestMessage.isEmpty()) {
                return;
            }
            Message message = latestMessage.get(0);
            if (message.getAuthor().getIdLong() != requester.getIdLong()) {
                return;
            }
            OptionMapping option = event.getOption(optionNameSwizzleNumber);
            swizzleImageHelper(message, event.getTextChannel(), option != null ? option.getAsString() : "");
        });
    }

    private void swizzleImageHelper(Message messageToSwizzle, TextChannel channel, String args) {
        List<Message.Attachment> attachments = messageToSwizzle.getAttachments();
        if (attachments.isEmpty())
            channel.sendMessage("Please attach one or more images.");
        else
            for (Message.Attachment a : attachments) {
                if (a.getWidth() > 1000 || a.getHeight() > 1000) {
                    channel.sendMessage(a.getFileName() + " is larger than 1000px.");
                    continue;
                }

                a.retrieveInputStream().thenAccept(inputStream -> {
                    try {
                        channel.sendFile(swizzler.swizzle(inputStream, args), "swizzled.png").queue();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty())
            event.reply("Please attach one or more images.");
        else
            for (Message.Attachment a : attachments) {
                if (a.getWidth() > 1000 || a.getHeight() > 1000) {
                    event.reply(a.getFileName() + " is larger than 1000px.");
                    continue;
                }

                a.retrieveInputStream().thenAccept(inputStream -> {
                    try {
                        event.getTextChannel().sendFile(swizzler.swizzle(inputStream, event.getArgs()), "swizzled.png").queue();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
    }

}
