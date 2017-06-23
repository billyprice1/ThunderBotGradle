package wh1spr.thunderbot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;



public class ThunderBot extends ListenerAdapter implements EventListener{
	
	private static final String TOKEN = "MzI2NDY1MTUyNDE1MzAxNjMy.DCnN9w.Gxmpr6eYaQG_bBPW5ctK6_gQt-U";
	private static JDA jda = null;
	
	public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
		jda = new JDABuilder(AccountType.BOT)
	            .setToken(TOKEN).addEventListener(new ThunderBot(), new MusicMessageHandler())
	            .buildBlocking();
		
		System.out.println("Logging bot in...");
			
		jda.getSelfUser().getManager().setName("The Best Bot").submit();
	}
}
