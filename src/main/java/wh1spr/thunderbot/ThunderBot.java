package wh1spr.thunderbot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
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
		
		admins.add("204529799912226816"); //wh1spr
		admins.add("277140443785854986"); //wraithgamer2
	}
	
	public static final Set<String> admins = new HashSet<String>();
	private static final Set<String> deniedSet = new HashSet<String>();
	
	/**
	 * Return wether or not the given user is denied to use the bot's commands
	 * @param user The user to check.
	 * @return true if the user can not use the bot's commands.
	 * @return false if the user can use the bot's commands.
	 */
	public static final boolean isDenied(User user) {
		return deniedSet.contains(user.getId());
	}
	
	/**
	 * Denies a user to use any of the bot's commands. This also removes the user from 
	 * the admin group if the user was part of that group.
	 * @param user The user to deny.
	 * @return true if the user has been denied.
	 * @return false if the user was already denied.
	 */
	public static final boolean deny(User user) {
		admins.remove(user.getId());
		return deniedSet.add(user.getId());
	}
	public static final void deny(List<User> users) {
		for (User e : users) {
			deny(e);
		}
	}
	
	/**
	 * Allows a user to use the bot's commands.
	 * @param user The user to allow.
	 * @return true if the user was denied access before and has been regranted permission.
	 * @return false if the user wasn't denied.
	 */
	public static final boolean allow(User user) {
		return deniedSet.remove(user.getId());
	}
	public static final void allow(List<User> users) {
		for (User e : users) {
			allow(e);
		}
	}
	
	/**
	 * Return wether or not the given user is an admin.
	 * @param user The user to check.
	 * @return true if the user is an admin.
	 */
	public static final boolean isAdmin(User user) {
		return admins.contains(user.getId());
	}
	
	/**
	 * Adds a user to the admin group.
	 * @param user The user to make admin.
	 * @return true if the user has been made admin.
	 * @return false if the user already was admin.
	 */
	public static final boolean makeAdmin(User user) {
		return admins.add(user.getId());
	}
	
	/**
	 * Removes the user from the admin group.
	 * @param user The user to remove.
	 * @return true if the user was an admin and has been removed.
	 * @return false if the user was not an admin.
	 */
	public static final boolean removeAdmin(User user) {
		return admins.remove(user.getId());
	}
}
