package wh1spr.thunderbot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import wh1spr.thunderbot.music.GuildMusicManager;

public class HelpCommand {
		
	public HelpCommand() {
		try {
			this.ini = new Wini(getClass().getResource("commands.ini"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Wini ini = null;
	
	public void evaluate(GuildMessageReceivedEvent event, GuildMusicManager mng) {
		//this only is called when the help command is issued,right now it is placed in MusicMessageHandler
		String[] command = event.getMessage().getContent().split(" ", 2);
		
		if (command.length < 2 ) {
			displayModules(event);
		} else {
			displayModule(event);
		}
	}
	
	private void displayModule(GuildMessageReceivedEvent event) {
		TextChannel channel = event.getChannel();
		String[] commandevent = event.getMessage().getContent().split(" ", 3);
		if (Tools.startsWith(commandevent[1], "!", "&&", "/")) commandevent[1] = "command"+commandevent[1];
		
		Set<Entry<String, Section>> sections = ini.entrySet();
		for (Entry<String, Section> e : sections) {
			Section section = e.getValue();
			
			if (section.getName().toUpperCase().equals(commandevent[1].toUpperCase())) {
				String msg = String.format("```%15s|| %-55s\n", section.getName().replaceAll("command", ""), "Description");
				String[] usages = new String[5];
				String[] aliases = new String[5];
				for (int i = 0; i < 15; i++) {
					msg += "=";
				}
				msg += "||";
				for (int i = 0; i < 51; i++) {
					msg += "=";
				}
				
				for (Entry<String, String> command : section.entrySet()) {
					if (command.getKey().startsWith("Usage")) {
						usages[Integer.valueOf(command.getKey().substring(command.getKey().length()-1))] = command.getValue();
					} else if (command.getKey().startsWith("Usage")) {
						aliases[Integer.valueOf(command.getKey().substring(command.getKey().length()-1))] = command.getValue();
					} else {
						msg +="\n" + String.format("%14s || %-50s", command.getKey(), command.getValue());	
					}
					
				}
				for (int i = 0; i < usages.length; i++) {
					if (usages[i] != null) {
						msg += "\n" + String.format("%14s || %-55s", "Usage " + i, usages[i]);
					}
				}
				for (int i = 0; i < aliases.length; i++) {
					if (aliases[i] != null) {
						msg += "\n" + String.format("%14s || %-55s", "Alias " + i, aliases[i]);
					}
				}
				msg += "```";
				channel.sendMessage(msg).queue();
			}
		}
	}
	
	private void displayModules(GuildMessageReceivedEvent event) {
		TextChannel channel = event.getChannel();
		
		Set<Entry<String, Section>> sections = ini.entrySet();
		for (Entry<String, Section> e : sections) {
			Section section = e.getValue();
			
			if (section.getName().equals("Modules")) {
				String msg = String.format("```%15s|| %-50s\n", "Modules ", "Description");
				for (int i = 0; i < 15; i++) {
					msg += "=";
				}
				msg += "||";
				for (int i = 0; i < 51; i++) {
					msg += "=";
				}
				for (Entry<String, String> module : section.entrySet()) {
					if (module.getKey().startsWith("command")) continue;
					String val = module.getValue();
					int i = 0;
					String[] valu = val.split(" ");
					ArrayList<String> lines = new ArrayList<String>();
					double nrOfLines = Math.ceil(val.length() / 50.0);

					while (val.length() > 50 || lines.size() < nrOfLines) {
						//select words until 50 length
						String next = "";
						
						int len = 0;
						while (len < 50 && len <= val.length() && i < valu.length ) {
							next += valu[i] + " ";
							len += valu[i].length() + 1;
							i++;
							
						}
						if (len > 50) {
							i--;
							next = next.substring(0, next.length() - (valu[i].length() + 1));
							len -= valu[i].length();
						}
						lines.add(next);
						try {
							val = val.substring(len, val.length());
						} catch (IndexOutOfBoundsException exc) {
							val = "";
						}
						
						
						
					}
					Iterator<String> lineIterator = lines.iterator();
					//line 1 of module:
					msg+="\n" + String.format("%14s || %-50s", module.getKey(), lineIterator.next());
					//line 2+ of module:
					while (lineIterator.hasNext())
						msg+= String.format("\n%14s || %-50s", " ", lineIterator.next()); 
				}
				msg += "```";
				channel.sendMessage(msg).queue();
			}
		}
	}
}
