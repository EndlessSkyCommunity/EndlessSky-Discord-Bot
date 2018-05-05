package bot;

import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildController;

public class ModeratorCommands 
implements CommandExecutor{
	ESBot bot;

	public ModeratorCommands(ESBot bot){
		super();
		this.bot = bot;
	}

	@Command(aliases = {"-purge"}, description = "Deletes the last X messages in this channel.\nRange: 2 - 100.\n\nRequires the \"manage messages\" permission", usage = "-purge X", privateMessages = false)
	public void onPurgeCommand(Guild guild, Message msg, TextChannel channel, String[] args, Member mod) {
		if(mod.getUser().isBot()) return;
		String[] parsed = Helper.getWords(args);
		// Always remove the requesting message, and delete many after it.
		msg.delete().queue( y -> {
			if(!Helper.CanModerate(channel, mod))
				channel.sendMessage(Helper.GetRandomDeniedMessage()).queue();
			else if(parsed.length == 1 && Helper.IsIntegerInRange(parsed[0], 2, 100)){
				int amount = Integer.valueOf(parsed[0]);
				channel.getHistory().retrievePast(amount).queue( m -> {
					channel.deleteMessages(m).queue( x -> {
						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle("Moderation:");
						eb.setColor(guild.getMember(bot.getSelf()).getColor());
						eb.setDescription("Spaced " + m.size() + " messages! Who's next?!");
						eb.setThumbnail(bot.HOST_RAW_URL + "/thumbnails/cross.png");
						channel.sendMessage(eb.build()).queue();
						// Log the command usage.
						logCommand(guild, "Purged " + amount + " messages in " + channel.getAsMention() + ", ordered by `" + mod.getEffectiveName() + "`.");
					});
				});
			}
		});
	}



	@Command(aliases = {"-move", "-wormhole"}, description = "Moves the last X messages in this channel to the linked channel. Can also send participants to the-corner.\nX: Message count to move\nRange: 2 - 100.\n\nY: Total time in the-corner\nRange: 0 - 86400, optional.\n\nRequires moderation abilities.", usage = "-move X #room-name Y", privateMessages = false)
	public void onMoveCommand(Guild guild, Message msg, TextChannel channel, String[] args, Member mod) {
		if(mod.getUser().isBot()) return;
		String[] parsed = Helper.getWords(args);
		final String moveHeader = "Incoming wormhole content:\n```";
		final String moveFooter = "```";
		if(!Helper.CanModerate(channel, mod))
			msg.delete().queue();
		else{
			if(parsed.length < 2
					|| !Helper.IsIntegerInRange(parsed[0], 2, 100)
					|| msg.getMentionedChannels().size() != 1
					|| !Helper.IsDiffAndWritable(channel, msg.getMentionedChannels().get(0))){
				msg.delete().queue();
			}
			// Act on the valid channel.
			else{
				int n = Integer.valueOf(parsed[0]);
				// Allow custom banlengths, including 0-length.
				int banLength = (parsed.length > 2
						&& Helper.IsIntegerInRange(parsed[2], 0, 86400))
								? Integer.valueOf(parsed[2]) : 0;
				TextChannel dest = msg.getMentionedChannels().get(0);
				// Always delete the requesting message.
				msg.delete().complete();
				// Use a lambda to asynchronously perform this request:
				channel.getHistory().retrievePast(n).queue( toDelete -> {
					if(toDelete.isEmpty())
						return;
					HashSet<Member> toTempBan = new HashSet<Member>();
					LinkedList<String> toMove = new LinkedList<String>();
					for(Message m : toDelete){
						Member chatter = guild.getMember(m.getAuthor());
						if(Helper.isBannable(channel, chatter))
							toTempBan.add(chatter);
						String what = m.getContentStripped().trim();
						if(what.isEmpty())
							continue;
						toMove.addFirst(m.getCreationTime()
								.format(DateTimeFormatter.ISO_INSTANT).substring(11, 19)
								+ "Z " + chatter.getEffectiveName() + ": " + what +"\n"
						);
					}
					// Remove the messages from the original channel and log the move.
					channel.deleteMessages(toDelete).queue( x -> {
						EmbedBuilder log = new EmbedBuilder();
						log.setDescription(dest.getAsMention());
						log.setThumbnail("https://cdn.discordapp.com/emojis/344684586904584202.png");
						log.appendDescription("\n(" + toMove.size() + " messages await)");
						if(toDelete.size() - toMove.size() > 0)
							log.appendDescription("\n(Some embeds were eaten)");
						channel.sendMessage(log.build()).queue();
					});

					// Transport the message content to the new channel.
					if(!toMove.isEmpty())
						Helper.writeChunks(dest, toMove, moveHeader, moveFooter);

					// Place the temporary bans.
					if(!toTempBan.isEmpty() && banLength > 0){
						Helper.EnsureRole(guild, Helper.ROLE_NAUGHTY);
						List<Role> naughty = guild.getRolesByName(Helper.ROLE_NAUGHTY, true);
						GuildController gc = guild.getController();
						for(Member b : toTempBan)
							giveTimeOut(gc, b, naughty, banLength);
					}
					
					// Log the move in mod-log.
					String report = "Moved " + toMove.size() +
							" messages from " + channel.getAsMention() +
							" to " + dest.getAsMention() + ", ordered by `" +
							mod.getEffectiveName() + "`.";
					if(!toTempBan.isEmpty() && banLength > 0)
						report += "\nAlso gave " + toTempBan.size() + " participants a time-out for " + banLength + " seconds.";
					logCommand(guild, report);
				});
			}
		}
	}



	@Command(aliases = {"-timeout"}, description = "Sends the mentioned member to #the-corner.\nX: Time until the member gets released, in seconds.\nRange: 1 - 86400\n\nRequires moderation and role change abilities.", usage = "-timeout @member X", privateMessages = false)
	public void onTimeoutCommand(Guild guild, TextChannel channel, Message msg, String[] args, Member mod){
		if(mod.getUser().isBot()) return;
		String[] parsed = Helper.getWords(args);
		if(!Helper.CanModAndRoleChange(channel, mod))
			channel.sendMessage(Helper.GetRandomDeniedMessage()).queue();
		else if(parsed.length >= 2 && Helper.IsIntegerInRange(parsed[1], 1, 86400)){
			// Put the mentioned user in the-corner unless they have the same perms (or are a bot).
			Member toTimeout = guild.getMember(msg.getMentionedUsers().get(0));
			if(Helper.CanModAndRoleChange(channel, toTimeout) || toTimeout.getUser().isBot())
				return;
			int banLength = Math.max(1, Integer.valueOf(parsed[1]));
			Helper.EnsureRole(guild, Helper.ROLE_NAUGHTY);
			giveTimeOut(guild, toTimeout, Helper.ROLE_NAUGHTY, banLength);
			
			// Log the timeout in mod-log.
			String message = "Put `" + toTimeout.getEffectiveName() + "` in a time-out for " + banLength + " seconds.\nReason: `" + mod.getEffectiveName() + "` said ";
			if(parsed.length > 2){
				message += "'`";
				for(int i = 2; i < parsed.length; ++i)
					message += " " + parsed[i].trim();
				message += "`'";
			}
			else
				message += "(no reason given).";
			try{
				TextChannel corner = guild.getTextChannelsByName("the-corner", false).get(0);
				corner.sendMessage(message).queue();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			logCommand(guild, message);
		}
		else
			channel.sendMessage("You forgot some arguments: `@member seconds reason`").queue();
	}



	@Command(aliases = {"-update"}, description = "Reloads the memes and content of known GitHub files.", usage = "-update", privateMessages = false)
	public void onUpdateCommand(Message msg, TextChannel channel, Member mod) {
		if(mod.getUser().isBot()) return;
		if(!Helper.CanModerate(channel, mod))
			channel.sendMessage(Helper.GetRandomDeniedMessage()).queue();
		else{
			channel.sendMessage("Updating...").queue();
			bot.update();
			channel.sendMessage("Update finished").queue();
		}
	}



	private void logCommand(Guild guild, String report){
		try{
			TextChannel modlog = guild.getTextChannelsByName("mod-log", false).get(0);
			modlog.sendMessage(report).queue();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}



	/**
	 * Utility functions to send the given member to a timeout room by means of
	 * changing the assigned roles.
	 * TODO: The callback does not persist if the bot is destroyed before the
	 * duration is reached, so some more permanent storage is needed, and should
	 * be checked upon startup.
	 * @param Guild   guild        The server with a corner and a naughty member.
	 * @param Member  member       The member who needs a time-out.
	 * @param String  newRoleName  The result of a name-search for the role.
	 * @param int     duration     The length of the time-out.
	 */
	private void giveTimeOut(Guild guild, Member member, String newRoleName, int duration){
		List<Role> newRole = guild.getRolesByName(newRoleName, true);
		List<Role> oldRoles = member.getRoles();
		GuildController gc = guild.getController();
		temporaryRoleSwapAfterDuration(gc, member, newRole, oldRoles, duration);
	}

	private void giveTimeOut(GuildController gc, Member member, List<Role> newRoles, int duration){
		List<Role> oldRoles = member.getRoles();
		temporaryRoleSwapAfterDuration(gc, member, newRoles, oldRoles, duration);
	}

	// TODO: Include a "restored role" log entry in mod-log when a more robust unbanner is implemented.
	private void temporaryRoleSwapAfterDuration(GuildController gc, Member m, List<Role> newRoles, List<Role> oldRoles, int duration){
		gc.modifyMemberRoles(m, newRoles, oldRoles).queue(x -> {
			gc.removeSingleRoleFromMember(m, newRoles.get(0)).queueAfter(duration, TimeUnit.SECONDS);
			for (Role r : oldRoles)
				gc.addSingleRoleToMember(m, r).queueAfter(duration, TimeUnit.SECONDS);
			}
		);
	}
}
