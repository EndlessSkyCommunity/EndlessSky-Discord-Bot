package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.audio.Audio;

public class Loop extends Command {

    private final Audio audio;

    public Loop(Audio audio) {
        name = "loop";
        help = "Controls whether or not to loo the current track.\nNo argument returns whether or not looping is currently on.\n`-loop on` will turn looping on if it isn't already.\n`-loop off` will turn looping off if it isn't already.";
        category = James.audio;
        this.aliases = new String[]{"repeat"};
        this.audio = audio;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (audio.getVoiceChannel() != null
                && event.getMember().getVoiceState().getChannel().equals(audio.getVoiceChannel())
                && audio.getPlayingTrack() != null) {
            String args = event.getArgs();
            if (args.length() == 0) {
                audio.getLoop(event);
            }
            else {
                String arg = args.split(" ")[0];
                if (arg.equals("on"))
                    audio.setLoop(event, true);
                else if (arg.equals("off"))
                    audio.setLoop(event, false);
            }
        }
    }
}
