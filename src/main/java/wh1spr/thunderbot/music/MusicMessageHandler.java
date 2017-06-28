package wh1spr.thunderbot.music;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.requests.restaction.pagination.MessagePaginationAction;
import wh1spr.thunderbot.HelpCommand;
import wh1spr.thunderbot.ThunderBot;

public class MusicMessageHandler extends ListenerAdapter{

	public MusicMessageHandler() {
		this.playerManager = new DefaultAudioPlayerManager();
		playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        this.mngs = new HashMap<String, GuildMusicManager>();
	}
	
	private final AudioPlayerManager playerManager;
	private final HashMap<String, GuildMusicManager> mngs; //id of the guild, manager for that guild
	private final HelpCommand help = new HelpCommand();
	private final HashSet<User> deniedSet = new HashSet<>();
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		
			
		if (!event.getMessage().getRawContent().startsWith("!") && 
				!event.getMessage().getRawContent().startsWith("&&") &&
				!event.getMessage().getRawContent().startsWith("`1") &&
				!event.getMessage().getRawContent().startsWith("§1")) {
			return;
		}
		
		if (deniedSet.contains(event.getAuthor())) {
			event.getAuthor().openPrivateChannel().complete().sendMessage("You have been denied to use this bot.").queue();
			event.getChannel().deleteMessageById(event.getMessageId());
			return;
		}
		
		String[] command = event.getMessage().getContent().split(" ");
		Guild guild = event.getGuild();
		
		GuildMusicManager mng = mngs.get(guild.getId());
		if (mng == null) {
			mng = new GuildMusicManager(playerManager, guild);
			mngs.put(guild.getId(), mng);
		}
		
	    AudioPlayer player = mng.player;
	    AudioScheduler scheduler = mng.scheduler;
	    
	    String a = command[0].toLowerCase();
	    
	    switch (a) {
	    	case "&&deny":
	    		if (command.length < 2 || event.getMessage().getMentionedUsers().get(0) == null) return;
	    		if (ThunderBot.isAdmin(event.getAuthor())) {
	    			deniedSet.add(event.getMessage().getMentionedUsers().get(0));
	    			ThunderBot.deny(event.getMessage().getMentionedUsers());
	    			String msg = "";
	    			for (User e : event.getMessage().getMentionedUsers()) {
	    				msg += e.getAsMention() + "\n";
	    			}
	    			event.getChannel().sendMessage(new EmbedBuilder().setColor(Color.red).addField(new Field("Denied following users", msg, false)).build()).queue();
	    		}
	    		break;
	    	case "&&allow":
	    		if (command.length < 2 || event.getMessage().getMentionedUsers().get(0) == null) return;
	    		if (ThunderBot.isAdmin(event.getAuthor())) {
	    			deniedSet.remove(event.getMessage().getMentionedUsers().get(0));
	    			event.getChannel().sendMessage("Allowed " + event.getMessage().getMentionedUsers().get(0).getAsMention()).queue();
	    			
	    		}
	    		break;
			case "&&shutdown":
				event.getChannel().deleteMessageById(event.getMessageIdLong()).complete();
				if (ThunderBot.admins.contains(event.getAuthor().getId())) {
					guild.getAudioManager().closeAudioConnection();
					event.getChannel().sendMessage("Goodbye.").complete();
					ThunderBot.jda.shutdown();
					System.exit(0);
				}
				break;
			
			case "!help":
				help.evaluate(event, mng);
				break;
			case "!join":
				VoiceChannel channel;
				
				if (command.length < 2) {
					channel = guild.getVoiceChannels().get(0);
				} else {
					channel = guild.getVoiceChannelsByName(command[1], true).get(0);
				}
				if (channel == null) {
					event.getChannel().sendMessage("There is no channel with name ```" + command[1] + "```").queue();
				}
		        guild.getAudioManager().setSendingHandler(mng.getSendHandler());
		        guild.getAudioManager().openAudioConnection(channel); 
		        break;
				
			case "!leave":
				guild.getAudioManager().closeAudioConnection();
				break;
			case "!play":
				if (command.length == 1) { //It is only the command to start playback (probably after pause)
	                if (player.isPaused()) {
	                    player.setPaused(false);
	                    event.getChannel().sendMessage("Playback as been resumed.").queue();
	                } else if (player.getPlayingTrack() != null) {
	                    event.getChannel().sendMessage("Player is already playing.").queue();
	                } else if (scheduler.queue.isEmpty()) {
	                    event.getChannel().sendMessage("The current audio queue is empty! Add something to the queue first!").queue();
	                }
	            } else {
	                loadAndPlay(mng, event.getChannel(), command[1], false);
	            }
				break;
			
			case "!skip":
				if (player.getPlayingTrack() == null) {
					event.getChannel().sendMessage("I'm not playing anything.").queue();
				} else {
					
					event.getChannel().sendMessage("Skipped " + player.getPlayingTrack().getInfo().title).queue();
					scheduler.nextTrack();
				}
				break;
			
			case "!volume":
				if (command.length == 1) {
					event.getChannel().sendMessage(String.format("The current volume is %d/100", player.getVolume())).queue();
				} else {
					int vol = 35;
					try {
						vol = Integer.valueOf(command[1]);
					} catch (Exception e) {
						vol = 35;
						//usage
					}
					player.setVolume(vol);
				}
				break;
			case "!p":
			case "!pause":
				if (player.getPlayingTrack() == null)player.setPaused(true);
				player.setPaused(!player.isPaused()); break;
				
			case "!now":
			case "!np":
			case "!nowplaying":
				AudioTrack currentTrack = player.getPlayingTrack();
	            if (currentTrack != null)
	            {
	                String title = currentTrack.getInfo().title;
	                String position = getTimestamp(currentTrack.getPosition());
	                String duration = getTimestamp(currentTrack.getDuration());

	                String nowplaying = String.format("**Playing:** %s\n**Time:** [%s / %s]",
	                        title, position, duration);

	                event.getChannel().sendMessage(nowplaying).queue();
	            } else {
	            	event.getChannel().sendMessage("The player is not currently playing anything!").queue();
	            }
	            break;
			case "&&changename":
			case "&&cn":
				if (command.length < 2) {
					//usage
					ThunderBot.jda.getSelfUser().getManager().setName("ThunderBot").submit();
					return;
				} else if (ThunderBot.admins.contains(event.getAuthor().getId())){
//					this.jda.getPresence().setGame(Game.of(command[1]));	
					ThunderBot.jda.getSelfUser().getManager().setName(event.getMessage().getRawContent().replaceFirst(command[0], "")).submit();
				}
				break;
			
			case "&&cg":
			case "&&changegame":
				if (command.length < 2) {
					//usage
					return;
				} else if (ThunderBot.admins.contains(event.getAuthor().getId())){
					ThunderBot.jda.getPresence().setGame(Game.of(event.getMessage().getRawContent().replaceFirst(command[0], "")));	
				}
				break;
			
			case "!stop":
			case "§1":
			case "`1":
				if ((command[0].equals("§1") || command[0].equals("`1"))&& ThunderBot.admins.contains(event.getAuthor().getId())) {
					event.getChannel().deleteMessageById(event.getMessageId()).queue();
					player.stopTrack();
		            player.setPaused(false);
				} else if (command[0].equals("!stop")) {
					player.stopTrack();
		            player.setPaused(false);
				}
				break;
				
			case "&&clean":
				try {
						
				
					if (ThunderBot.admins.contains(event.getAuthor().getId())) {
						final List<Message> msgs = new ArrayList<>();
						if (command.length < 2) {
							event.getChannel().getIterableHistory().complete().iterator().forEachRemaining(e->{if (!e.isPinned()) {msgs.add(e);}});
						} else {
							event.getChannel().getHistory().retrievePast(Integer.valueOf(command[1]) + 1).complete().iterator().forEachRemaining(e->{if (!e.isPinned()) {msgs.add(e);}});
						}
						event.getChannel().deleteMessages(msgs).queue();
					}
					break;
				} catch (Exception e) {
					event.getChannel().sendMessage("ERROR: Something went wrong while executing this command.");
				}
			default:
				break;
		}
	}
	
	private void loadAndPlay(GuildMusicManager mng, final MessageChannel channel, String url, final boolean addPlaylist)
    {
        final String trackUrl;

        //Strip <>'s that prevent discord from embedding link resources
        if (url.startsWith("<") && url.endsWith(">"))
            trackUrl = url.substring(1, url.length() - 1);
        else
            trackUrl = url;

        playerManager.loadItemOrdered(mng, trackUrl, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                String msg = "Adding to queue: " + track.getInfo().title;

                mng.scheduler.queue(track);
                channel.sendMessage(msg).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                List<AudioTrack> tracks = playlist.getTracks();


                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                if (addPlaylist)
                {
                    channel.sendMessage("Adding **" + playlist.getTracks().size() +"** tracks to queue from playlist: " + playlist.getName()).queue();
                    tracks.forEach(mng.scheduler::queue);
                }
                else
                {
                    channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
                    mng.scheduler.queue(firstTrack);
                }
            }

            @Override
            public void noMatches()
            {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }
    private static String getTimestamp(long milliseconds)
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours   = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }
}
