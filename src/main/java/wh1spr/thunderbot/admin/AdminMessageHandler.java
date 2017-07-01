package wh1spr.thunderbot.admin;

import java.io.IOException;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wh1spr.thunderbot.ThunderBot;

public class AdminMessageHandler extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getChannel().getId().equals("328876368483844096")) {
			if (event.getAuthor().getName().equals("GitHub")) {
				update();
			}
		}
	}
	
	public static void update() {
		ProcessBuilder pb = new ProcessBuilder("/home/pi/DiscordBots/ThunderBot/update.sh");
		try {
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ThunderBot.jda.shutdown();
		System.exit(0);
	}
}
