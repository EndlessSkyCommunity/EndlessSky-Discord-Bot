package me.mcofficer.james.commands.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.mcofficer.james.James;
import me.mcofficer.james.JamesSlashCommand;
import me.mcofficer.james.Util;
import me.mcofficer.james.audio.Audio;
import me.mcofficer.james.audio.Playlists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Playlist extends AudioCommand {

    private Playlists playlists;

    public Playlist(Audio audio, Playlists playlists) {
        super(audio);
        name = "playlist";
        help = "Saves URLs as playlists, to be quickly accessible. Each playlist is associated with a key.\n" +
                "`-playlist X` plays a playlist X.\n" +
                "`-playlist list` shows all available playlists.\n" +
                "`-playlist save X U` saves the URL U under the key X.\n" +
                "`-playlist delete X` deletes the playlist X, if you are the owner of X.\n" +
                "`-playlist edit X U` updates the playlist X with the URL U.\n" +
                "`-playlist info X` Shows the URL and Owner of the playlist X.\n";
        arguments = "[list|save|delete|edit|info] [X [U]]";
        this.playlists = playlists;

        this.children = new SlashCommand[]{
                new PlayPlaylist(audio, playlists),
                new ListPlaylist(audio, playlists),
                new SavePlaylist(audio, playlists),
                new DeletePlaylist(audio, playlists),
                new EditPlaylist(audio, playlists),
                new InfoPlaylist(audio, playlists)
        };
    }


    //Slash command code, the new hotness

    private static abstract class PlaylistCommand extends AudioCommand {
        protected Playlists playlists;
        public PlaylistCommand(Audio audio, Playlists playlists) {
            super(audio);
            this.playlists = playlists;
        }


    }
    @Override
    protected void execute(SlashCommandEvent event) {}

    private static class PlayPlaylist extends PlaylistCommand {
        private static final String optionName = "playlist";
        public PlayPlaylist(Audio audio, Playlists playlists) {
            super(audio, playlists);
            this.name = "play";
            this.help = "Plays the specified saved playlist.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.STRING, optionName, "The playlist to play.", true));
            this.options = data;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            audio.connect(event.getMember().getVoiceState().getChannel());
            String playList = event.getOption(optionName).getAsString();
            audio.loadItem(playlists.getPlaylistUrl(playList), event);
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }

    private static class ListPlaylist extends PlaylistCommand {
        public ListPlaylist(Audio audio, Playlists playlists) {
            super(audio, playlists);
            this.name = "list";
            this.help = "Shows all available playlists.";
        }
        @Override
        protected void execute(SlashCommandEvent event) {
            List<String> keys;
            try {
                keys = playlists.getKeys();
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();

            for (String key : keys)
                stringBuilder.append(String.format("\n\u2022 `%s`\n", key));

            EmbedBuilder embedBuider = new EmbedBuilder()
                    .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setDescription(String.format("Playlists: %s\n%s", keys.size(), stringBuilder.toString()));
            event.replyEmbeds(embedBuider.build()).queue();
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }

    private static class SavePlaylist extends PlaylistCommand {
        private static final String optionNameKey = "key";
        private static final String optionNameUrl = "url";
        public SavePlaylist(Audio audio, Playlists playlists) {
            super(audio, playlists);
            this.name = "save";
            this.help = "Saves the URL U under the key X.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.STRING, optionNameKey, "The key to save the playlist under.", true));
            data.add(new OptionData(OptionType.STRING, optionNameUrl, "The URL to save as a playlist.", true));
            this.options = data;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                String key = event.getOption(optionNameKey).getAsString();
                String url = event.getOption(optionNameUrl).getAsString();
                if (playlists.keyExists(key))
                    event.reply("A playlist with key `" + key + "` already exists!");
                else {
                    playlists.addPlaylist(key, url, event.getMember().getId());
                    event.reply("Saved playlist as `" + key + "`");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }

    private static class DeletePlaylist extends PlaylistCommand {
        private static final String optionName = "key";

        public DeletePlaylist(Audio audio, Playlists playlists) {
            super(audio, playlists);
            this.name = "delete";
            this.help = "Deletes the specified playlist, if you are the owner.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.STRING, optionName, "The key of the playlist to delete.", true));
            this.options = data;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String key = event.getOption(optionName).getAsString();
            try {
                if (!playlists.isOwner(key, event.getMember().getId()))
                    event.reply("You're not the Owner of this playlist!");
                else {
                    playlists.removePlaylist(key);
                    event.reply("Playlist `" + key + "` has been removed.");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }

    private static class EditPlaylist extends PlaylistCommand {
        private static final String optionNameKey = "key";
        private static final String optionNameUrl = "url";

        public EditPlaylist(Audio audio, Playlists playlists) {
            super(audio, playlists);
            this.name = "edit";
            this.help = "Updates the specified playlist with the given URL.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.STRING, optionNameKey, "The key of the playlist to update.", true));
            data.add(new OptionData(OptionType.STRING, optionNameUrl, "The URL to update the playlist with.", true));
            this.options = data;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String key = event.getOption(optionNameKey).getAsString();
            String url = event.getOption(optionNameUrl).getAsString();
            try {
                if (!playlists.isOwner(key, event.getMember().getId()))
                    event.reply("You're not the Owner of this playlist!");
                else {
                    playlists.changePlaylistUrl(key, url);
                    event.reply("Playlist `" + key + "` has been edited.");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }

    private static class InfoPlaylist extends PlaylistCommand {

        private static final String optionName = "key";

        public InfoPlaylist(Audio audio, Playlists playlists) {
            super(audio, playlists);
            this.name = "info";
            this.help = "Retrieves the URL and owner of the specified playlist.";

            List<OptionData> data = new ArrayList<>();
            data.add(new OptionData(OptionType.STRING, optionName, "The key of the playlist to get info for.", true));
            this.options = data;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String key = event.getOption(optionName).getAsString();
            try {
                Map.Entry<String, String> info = playlists.getPlaylistInfo(key);
                event.getJDA().retrieveUserById(info.getValue()).queue(user -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                            .setColor(event.getGuild().getSelfMember().getColor())
                            .setDescription(String.format("Key: `%s`\nURL: %s\n Owner: %s", key, info.getKey(), user.getAsMention()));
                    event.replyEmbeds(embedBuilder.build()).queue();
                });
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void doCommand(CommandEvent event) {}
        protected void doCommand(SlashCommandEvent event) {}
    }


    //Standard, non-slash command related code
    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().length() == 0)
            return;

        String[] args = event.getArgs().split(" ");
        String arg = args[0];
        String[] revisedArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            if (arg.equals("list"))
                list(event);
            else if (arg.equals("save"))
                save(event, revisedArgs);
            else if (arg.equals("delete"))
                delete(event, revisedArgs);
            else if (arg.equals("edit"))
                edit(event, revisedArgs);
            else if (arg.equals("info"))
                info(event, revisedArgs);
            else
                play(event);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void play(CommandEvent event) {
        audio.connect(event.getMember().getVoiceState().getChannel());
        audio.loadItem(playlists.getPlaylistUrl(event.getArgs()), event);
    }

    private void list(CommandEvent event) throws IOException {
        List<String> keys = playlists.getKeys();
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : keys)
            stringBuilder.append(String.format("\n\u2022 `%s`\n", key));

        EmbedBuilder embedBuider = new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setColor(event.getGuild().getSelfMember().getColor())
                .setDescription(String.format("Playlists: %s\n%s", keys.size(), stringBuilder.toString()));
        event.reply(embedBuider.build());
    }

    private void save(CommandEvent event, String[] args) throws IOException {
        if (playlists.keyExists(args[0]))
            event.reply("A playlist with key `" + args[0] + "` already exists!");
        else {
            playlists.addPlaylist(args[0], args[1], event.getAuthor().getId());
            event.reply("Saved playlist as `" + args[0] + "`");
        }
    }

    private void delete(CommandEvent event, String[] args) throws IOException {
        if (!playlists.isOwner(args[0], event.getAuthor().getId()))
            event.reply("You're not the Owner of this playlist!");
        else {
            playlists.removePlaylist(args[0]);
            event.reply("Playlist `" + args[0] + "` has been removed.");
        }
    }

    private void edit(CommandEvent event, String[] args) throws IOException {
        if (!playlists.isOwner(args[0], event.getAuthor().getId()))
            event.reply("You're not the Owner of this playlist!");
        else {
            playlists.changePlaylistUrl(args[0], args[1]);
            event.reply("Playlist `" + args[0] + "` has been edited.");
        }
    }

    private void info(CommandEvent event, String[] args) throws IOException {
        Map.Entry<String, String> info = playlists.getPlaylistInfo(args[0]);
        event.getJDA().retrieveUserById(info.getValue()).queue(user -> {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setDescription(String.format("Key: `%s`\nURL: %s\n Owner: %s", args[0], info.getKey(), user.getAsMention()));
            event.reply(embedBuilder.build());
        });
    }

    //We don't need these here, well, we don't need the bodies, we still need the declarations
    protected void doCommand(CommandEvent event) {}
    protected void doCommand(SlashCommandEvent event) {}
}
