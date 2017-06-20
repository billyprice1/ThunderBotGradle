package wh1spr.thunderbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final AudioEventHandler scheduler;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager)
    {
        player = manager.createPlayer();
        scheduler = new AudioEventHandler(player);
        player.addListener(scheduler);
        
        player.setVolume(35);
        System.out.println(player.getVolume());
        System.out.println(player.isPaused());
    }
    
    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioScheduler getSendHandler() {
    	return new AudioScheduler(player);
    }
}