package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.audio.Audio;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public abstract class AudioCommand extends JamesSlashCommand {

    protected final Audio audio;

    public AudioCommand(Audio audio) {
        super();
        this.audio = audio;
        category = James.audio;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (audio.getVoiceChannel() == null)    //If James isn't in a voice channel, don't continue with the command
            return;
        VoiceChannel callerVoiceChannel = event.getMember().getVoiceState().getChannel();
        if (callerVoiceChannel == null) //If the member calling the command isn't in a voice channel, don't continue with the command
            return;
        if (!callerVoiceChannel.equals(audio.getVoiceChannel()))    //If the caller's voice channel isn't the same as James', don't continue with the command
            return;
        doCommand(event);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (audio.getVoiceChannel() == null)    //If James isn't in a voice channel, don't continue with the command
            return;
        VoiceChannel callerVoiceChannel = event.getMember().getVoiceState().getChannel();
        if (callerVoiceChannel == null) //If the member calling the command isn't in a voice channel, don't continue with the command
            return;
        if (!callerVoiceChannel.equals(audio.getVoiceChannel()))    //If the caller's voice channel isn't the same as James', don't continue with the command
            return;
        doCommand(event);
    }

    protected abstract void doCommand(CommandEvent event);
    protected abstract void doCommand(SlashCommandEvent event);
}
