package bot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.URL;

import net.dv8tion.jda.core.entities.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.script.ScriptEngineManager;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;

public class MiscCommands
implements CommandExecutor{

	private final CommandHandler commandHandler;
	private ESBot bot;
	//Free-To-Join Roles have to be lowercase
	private List<String> OPTINROLES = Arrays.asList("weeb", "notes", "traveler");

	public MiscCommands(CommandHandler commandHandler, ESBot bot) {
		this.commandHandler = commandHandler;
		this.bot = bot;
	}



	@Command(aliases = {"-template"}, description = "Sends the template for X. Possible args: outfit, ship, thumbnail or plugin.", usage = "-template X")
	public void onTemplatesCommand(Guild guild, MessageChannel channel, String[] args, User author)	{
		if(author.isBot()) return;
		String[] parsed = Helper.getWords(args);
		if(parsed.length == 0)
			channel.sendMessage("Which template would you like? I have three flavours available: 'outfit', 'ship', 'thumbnail' and 'plugin'.").queue();
		else
			for(String str : parsed){
				String name = "";
				if(str.equals("plugin"))
					name = "exampleplugin.zip";
				else if(str.equals("outfit"))
					name = "outfittemplate.blend";
				else if(str.equals("ship"))
					name = "shiptemplate.blend";
				else if(str.equals("thumbnail"))
					name = "thumbnail.blend";
				else
					channel.sendMessage("Sorry, I only have templates for 'outfit', 'ship', 'thumbnail' and 'plugin'.").queue();
				// Link to the desired template.
				if(name.length() > 0){
					String url = bot.HOST_RAW_URL + "/data/templates/" + name;
					EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle(name, url);
					eb.setColor(guild.getMember(bot.getSelf()).getColor());
					eb.setDescription("Here's your " + str + " template, served hot and crunchy :)");
					channel.sendMessage(eb.build()).queue();
				}
			}
	}



	@Command(aliases = {"-cat"}, description = "Posts a random cat picture from random.cat", usage = "-cat")
	public void onCatCommand(MessageChannel channel, User author) {
		if(author.isBot()) return;
		try{
			URL url = new URL("https://aws.random.cat/meow");
			channel.sendMessage(getJson(url).getString("file")).queue();
		}
		catch(IOException e){
			e.printStackTrace(System.out);
		}
	}



	@Command(aliases = {"-dog"}, description = "Posts a random dog picture from random.dog", usage = "-dog")
	public void onDogCommand(MessageChannel channel, User author) {
		if(author.isBot()) return;
		try{
			URL url = new URL("https://random.dog/woof.json");
			channel.sendMessage(getJson(url).getString("url")).queue();
		}
		catch(IOException e){
			e.printStackTrace(System.out);
		}
	}



	@Command(aliases = {"-apod"}, description = "posts a random APOD (NASA's Astronomy Picture of the Day).", usage = "-apod")
	public void onApodCommand(Guild guild, MessageChannel channel, User author) {
		if(author.isBot()) return;
		JSONObject json = null;
		try{
			javax.script.ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
			ScriptObjectMirror result = (ScriptObjectMirror)engine.eval(new java.io.FileReader("scripts/random_apod.js"));
			String sresult = result.values().toString();
			sresult = sresult.substring(sresult.indexOf("[")+1, sresult.indexOf("]"));
			URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=" + bot.getKey("NASA")+ "&date=" + sresult);
			json = getJson(url);
		}
		catch (ScriptException|IOException e){
			e.printStackTrace(System.out);
		}
		EmbedBuilder eb = new EmbedBuilder();
		if(json.getString("media_type").equals("image")){
			eb.setImage(json.getString("hdurl"));
			eb.setTitle(json.getString("title"));
			eb.setDescription(json.getString("explanation"));
			eb.setColor(guild.getMember(bot.getSelf()).getColor());
			channel.sendMessage(eb.build()).queue();
		}
		else if(json.getString("media_type").equals("video"))
			channel.sendMessage(json.getString("url").replace("embed/", "watch?v=")).queue();
		else
			channel.sendMessage("Incompatible Media Type, please try again").queue();
	}



	@Command(aliases = {"-optin"}, description = "Grants access to hidden rooms by assigning one or more Roles X [Y, Z, ...]. Available Roles: \n- Weeb\n- NotES", privateMessages = false, usage = "-optin X [Y, Z, ...]")
	public void onOptinCommand(Guild guild, MessageChannel channel, Message msg, String[] args, User author) {
		if (author.isBot()) return;
		ArrayList<Role> toAssign = new ArrayList<>();
		for (String arg : args) {
			try {
				if (OPTINROLES.contains(arg.toLowerCase()))
					toAssign.add(guild.getRolesByName(arg, true).get(0));
			}
			catch (IndexOutOfBoundsException e) {
				System.out.println("Failed to find a Role for '" + arg + "'");
			}
		}
		guild.getController().addRolesToMember(guild.getMember(author), toAssign).queue(success -> {
			msg.addReaction("\uD83D\uDC4C").queue();
			msg.delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}



	@Command(aliases = {"-optout"}, description = "Revokes access to hidden rooms by removing one or more Roles X [Y, Z, ...]. Available Roles: \n- Weeb\n- NotES", privateMessages = false, usage = "-optout X [Y, Z, ...]")
	public void onOptoutCommand(Guild guild, MessageChannel channel, Message msg, String[] args, User author) {
		if (author.isBot()) return;
		ArrayList<Role> toRemove = new ArrayList<>();
		for (String arg : args) {
			try {
				if (OPTINROLES.contains(arg.toLowerCase()))
					toRemove.add(guild.getRolesByName(arg, true).get(0));
			}
			catch (IndexOutOfBoundsException e) {
				System.out.println("Failed to find a Role for '" + arg + "'");
			}
		}
		guild.getController().removeRolesFromMember(guild.getMember(author), toRemove).queue(success -> {
			msg.addReaction("\uD83D\uDC4C").queue();
			msg.delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}




	@Command(aliases = {"-wav"}, description = "Converts an Audio file to a wav file suitable for ES.", usage = "-wav [attached file]")
	public void onWavCommand(Guild guild, MessageChannel channel, Message message, User author) {
		if(author.isBot() || message.getAttachments().isEmpty())
			return;
		try{
			OkHttpClient client = new OkHttpClient();
			String url = message.getAttachments().get(0).getUrl();
			String filename = message.getAttachments().get(0).getFileName();

			// Send POST to start up the conversion.
			MediaType mediaType = MediaType.parse("application/json");
			String payload = String.format("{\"input\":[{\"type\":\"remote\",\"source\":\"%s\", \"filename\": \"%s\"}]" +
							",\"conversion\":[{\"category\":\"audio\",\"target\":\"wav\", \"options\":" +
							"{\"frequency\": 44100,\"channels\": \"mono\",\"normalize\": false,\"pcm_format\": \"pcm_s16le\"}}]}",
							url,
							filename);
			RequestBody body = RequestBody.create(mediaType, payload);
			String botKey = bot.getKey("ONLINECONVERT");
			Request request = new Request.Builder()
					.url("http://api2.online-convert.com/jobs")
					.post(body)
					.addHeader("x-oc-api-key", botKey)
					.addHeader("cache-control", "no-cache")
					.build();

			// Get job data.
			Response response = client.newCall(request).execute();
			JSONObject json = new JSONObject(response.body().string());
			String conversion_id = json.getJSONArray("conversion").getJSONObject(0).getString("id");
			String input_id = json.getJSONArray("input").getJSONObject(0).getString("id");
			String token = json.getString("token");
			String job_id = json.getString("id");

			// TODO: Rewrite the status check as an async lambda to avoid threadlocking.
			channel.sendMessage("Conversion queued, this may take up a few minutes...").queue();
			// Prepare a request for the status history.
			request = new Request.Builder()
					.url("https://api2.online-convert.com/jobs/" + job_id + "/history")
					.get()
					.addHeader("x-oc-api-key", botKey)
					.addHeader("x-oc-token", token)
					.addHeader("cache-control", "no-cache")
					.build();

			// Check the status every 5 seconds, until it is either failed, done, or took longer than ~50 seconds.
			boolean failed = true;
			int counter = 0;
		checkProgress:
			while(counter++ < 10 && failed){
				// Sleep for 5 seconds before the next check.
				try{
					TimeUnit.SECONDS.sleep(5);
				}
				catch(InterruptedException e){
					e.printStackTrace();
				}
				// Check the conversion status.
				response = client.newCall(request).execute();
				JSONArray jsonarray = new JSONArray(response.body().string());
				for(Object object : jsonarray){
					String status = ((JSONObject) object).getString("status");
					if(status.equals("completed")){
						failed = false;
						break;
					}
					else if(status.equals("failed"))
						break checkProgress;
				}
			}
			if(!failed){
				// Get File ID.
				request = new Request.Builder()
						.url("https://api2.online-convert.com/jobs/" + job_id)
						.get()
						.addHeader("x-oc-api-key", botKey)
						.addHeader("x-oc-token", token)
						.addHeader("cache-control", "no-cache")
						.build();
				response = client.newCall(request).execute();
				json = new JSONObject(response.body().string());
				String file_id = json.getJSONArray("output").getJSONObject(0).getString("id");

				// Get download link.
				request = new Request.Builder()
						.url("https://api2.online-convert.com/jobs/" + job_id + "/output/" + file_id)
						.get()
						.addHeader("x-oc-api-key", botKey)
						.addHeader("x-oc-token", token)
						.addHeader("cache-control", "no-cache")
						.build();
				response = client.newCall(request).execute();
				json = new JSONObject(response.body().string());
				String download = json.getString("uri");

				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle(filename.split("\\.(?=[^\\.]+$)")[0] + ".wav", download);
				eb.setDescription("Here's your converted file, thank the guys at online-convert.com :)");
				eb.setColor(guild.getMember(bot.getSelf()).getColor());
				channel.sendMessage(eb.build()).queue();
			}
			else
				channel.sendMessage("Conversion failed. Maybe try again?").queue();
		}
		catch(IOException e){
			e.printStackTrace(System.out);
		}
	}



	@Command(aliases = {"-wikia"}, description = "Posts either a wikia article, a link to that article or search results for X.", usage = "-wikia X\n-wikia search X\n-wikia show X")
	public void onWikiaCommand(Guild guild, MessageChannel channel, String[] args, User author)	{
		String[] parsed = Helper.getWords(args);
		String baseUrl = "http://endless-sky.wikia.com/api/v1/";
		boolean search = false;
		boolean show = false;
		if(parsed[0].toLowerCase().equals("search"))
			search = true;
		else if(parsed[0].toLowerCase().equals("show"))
			show = true;

		StringBuilder builder = new StringBuilder();
		if(search)
			for(int i = 1; i < parsed.length; ++i)
				builder.append(" " + parsed[i]);
		else
			for(String s : parsed)
				builder.append(" " + s);

		// Make the querystring.
		String query = "";
		try{
			query = java.net.URLEncoder.encode(builder.toString().trim(), "UTF-8");
		}
		catch(IOException e){
			e.printStackTrace();
		}

		// Send the query to wikia.
		try{
			JSONObject json = getJson(new URL(baseUrl + "Search/List?query=" + query));
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(guild.getMember(bot.getSelf()).getColor());

			if(search){
				eb.setTitle("Results");
				eb.setDescription("Found the following results for '" + query + "':\n");
				for(Object o : json.getJSONArray("items")){
					JSONObject r = (JSONObject) o;
					eb.appendDescription("\n\n" + r.getString("title") + ":\n" + r.getString("url"));
				}
				channel.sendMessage(eb.build()).queue();
			}
			else if(show){
				String url = json.getJSONArray("items").getJSONObject(0).getString("url");
				int id = json.getJSONArray("items").getJSONObject(0).getInt("id");
				json = getJson(new URL(baseUrl + "Articles/AsSimpleJson?id=" + id));
				eb.setTitle(json.getJSONArray("sections").getJSONObject(0).getString("title"), url);
				StringBuilder sb = new StringBuilder();
				for(Object section : json.getJSONArray("sections"))
					for(Object o : ((JSONObject) section).getJSONArray("content")){
						JSONObject content = (JSONObject) o;
						if(content.getString("type").equals("paragraph")){
							try{
								sb.append(content.getString("text") + "\n");
							}
							catch(JSONException e) {}
						}
						else if(content.getString("type").equals("list"))
							for(Object element : content.getJSONArray("elements")){
								try{
									sb.append("- " + ((JSONObject) element).getString("text") + "\n");
								}
								catch(JSONException e) {}
							}
					}
				String text = sb.toString();
				if(sb.length() > MessageEmbed.TEXT_MAX_LENGTH)
					text = text.substring(0, MessageEmbed.TEXT_MAX_LENGTH - 3) + "...";
				eb.setDescription(text);
				// The API doesn't provide the image used in the Infobox, so we have to get that ourselves.
				try{
					String page = Helper.getPlainHtml(url);
					String thumbnailUrl = page.substring(page.indexOf("https://vignette.wikia.nocookie.net"), page.indexOf("class=\"image image-thumbnail")).replace("\"", "");
					eb.setThumbnail(thumbnailUrl);
				}
				catch(IndexOutOfBoundsException e){
					eb.setThumbnail(bot.HOST_RAW_URL + "/thumbnails/info.png");
				}
				channel.sendMessage(eb.build()).queue();
			}

			else{
				channel.sendMessage(json.getJSONArray("items").getJSONObject(0).getString("url")).queue();
			}
		}
		catch(IOException e){
			if(e instanceof FileNotFoundException) {
				e.printStackTrace();
				channel.sendMessage("Nothing found.").queue();
			}
			else
				e.printStackTrace();
		}
	}


	@Command(aliases = {"-translate"}, description = "Translates a query from a language 'source' to a language 'target'. Both 'source' and 'target' are optional ('source' can be auto-detected, 'target' defaults to english). Use the `list` parameter to get all supported languages.", usage = "-translate [source] [target] <query>\n-translate list")
	public void onTranslateCommand(Guild guild, TextChannel channel, String[] args, User author) {
		if(author.isBot()) return;
		String[] parsed = Helper.getWords(args);
		if(parsed.length > 0 && parsed[0].equalsIgnoreCase("list")){
			if(channel.getTopic().contains("spam") || channel.getName().contains("spam")) {
				StringBuilder sb = new StringBuilder("**Languages Supported by the Yandex Translation API:**");
				for(String[] pair : yandexGetLangs())
					sb.append("\n`" + pair[0] + "` (" + pair[1] + ")");
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle("EndlessSky-Discord-Bot", bot.HOST_PUBLIC_URL);
				eb.setColor(guild.getMember(bot.getSelf()).getColor());
				eb.setDescription(sb.toString());
				channel.sendMessage(eb.build()).queue();
			}
			else
				channel.sendMessage("This is a long list... please only use this command in a channel dedicated to spam").queue();
		}
		else if(parsed.length > 1){
			String result;
			boolean hasFirst = yandexIsSupportedLang(parsed[0]);
			boolean hasSecond = yandexIsSupportedLang(parsed[1]);
			if(hasFirst && hasSecond)
				// Converting between 'source' and 'target', both given.
				result = yandexTranslate(parsed[0], parsed[1], String.join(" ", Arrays.asList(parsed).subList(2, parsed.length)));
			else if(hasFirst)
				// Only a target language was given.
				result = yandexTranslate(null, parsed[0], String.join(" ", Arrays.asList(parsed).subList(1, parsed.length)));
			else
				// Translate the given language into English.
				result = yandexTranslate(null, "en", String.join(" ", parsed));
			channel.sendMessage(result).queue();
		}
	}


	/**
	 * Gets all supported languages of the Yandex Translation API.
	 * @return   ArrayList<>      possibly empty ArrayList containing key-Language pair Arrays such as ["en", "English"].
	 */
	private ArrayList<String[]> yandexGetLangs() {
		ArrayList<String[]> results = new ArrayList<>();
		try{
			JSONObject json = getJson(new URL("https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=" + bot.getKey("YANDEXTRANSLATE") + "&ui=en"));
			Iterator<?> keys = json.getJSONObject("langs").keys();
			while(keys.hasNext()){
				String key = (String) keys.next();
				String[] pair = { key, json.getJSONObject("langs").getString(key) };
				results.add(pair);
			}
			// Sort alphabetically by keys
			Collections.sort(results, new Comparator<String[]>() {
				@Override
				public int compare(String[] pair1, String[] pair2) {
					return pair1[0].compareTo(pair2[0]);
				}
			});
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return results;
	}


	/**
	 * Checks if a language is supported by the Yandex Translation API.
	 * @Param    String    language    A String representing a language code (e.g. "en").
	 * @return   boolean               True if the language is supported.
	 */
	private boolean yandexIsSupportedLang(String language) {
		for(String pair[] : yandexGetLangs())
			if(pair[0].equalsIgnoreCase(language))
				return true;
		return false;
	}



	/**
	 * Detects the language of a text.
	 * @Param    String    text    A String representing a language code (e.g. "en").
	 * @return   String            possibly null language code that has been returned by the API.
	 */
	private String yandexDetectLang(String text) {
		String result = null;
		try{
			text = URLEncoder.encode(text, "UTF-8");
			JSONObject json = getJson(new URL("https://translate.yandex.net/api/v1.5/tr.json/detect?key=" + bot.getKey("YANDEXTRANSLATE") + "&text=" + text));
			return json.getString("lang");
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}



	/**
	 * Translates a text using the Yandex Translation API.
	 * @Param    String    source    the language code (e.g. "en") of the source language, may ne null (will be auto-detected).
	 * @Param    String    target    the language code (e.g. "en") of the target language, may ne null (defaults to english).
	 * @return   String              the translated text, possibly null.
	 */
	private String yandexTranslate(@Nullable String source, String target, String text){
		try{
			text = URLEncoder.encode(text, "UTF-8");
			if(source == null)
				source = yandexDetectLang(text);
			String baseUrl= "https://translate.yandex.net/api/v1.5/tr.json/translate";
			URL url = new URL(baseUrl + "?key=" + bot.getKey("YANDEXTRANSLATE") + "&text=" + text + "&lang=" + source + "-" + target);
			return getJson(url).getJSONArray("text").getString(0);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}



	private static JSONObject getJson(URL url) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sb = new StringBuilder();
		int cp;
		while((cp = br.read()) != -1)
			sb.append((char) cp);
	 JSONObject json = new JSONObject(sb.toString());
	 return json;
 }

}
