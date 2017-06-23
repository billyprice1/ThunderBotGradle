package wh1spr.thunderbot;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wh1spr.thunderbot.music.MusicMessageHandler;



public class ThunderBot extends ListenerAdapter implements EventListener{
	
	private static final String TOKEN = "MzI2NDY1MTUyNDE1MzAxNjMy.DCnN9w.Gxmpr6eYaQG_bBPW5ctK6_gQt-U";
	public static JDA jda = null;
	
	public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
		jda = new JDABuilder(AccountType.BOT)
	            .setToken(TOKEN).addEventListener(new ThunderBot(), new MusicMessageHandler())
	            .buildBlocking();
		
		System.out.println("Logging bot in...");
			
		jda.getSelfUser().getManager().setName("Ben Dover").submit();
		jda.getPresence().setGame(Game.of("with your mom"));
		
		admins.add("204529799912226816"); //wh1spr
		admins.add("277140443785854986"); //wraithgamer2
	}
	
	public static final Set<String> admins = new HashSet<String>();
}
