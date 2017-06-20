package wh1spr.thunderbot;

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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.requests.restaction.pagination.MessagePaginationAction;

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
        this.mng = new GuildMusicManager(playerManager);
	}
	
	
	private final AudioPlayerManager playerManager;
	private final GuildMusicManager mng;
	
	@Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) 
    {
		
		
		
		if (event.getMessage().getRawContent().startsWith("&&shutdown")) {
			event.getChannel().deleteMessageById(event.getMessageIdLong()).complete();
//			System.out.println(event.getAuthor().getId());
			if (event.getAuthor().getId().equals("204529799912226816") ||
					event.getAuthor().getId().equals("277140443785854986"))
				System.exit(0);
		}
		
		String[] command = event.getMessage().getContent().split(" ", 2);
		Guild guild = event.getGuild();
	    AudioPlayer player = mng.player;
	    AudioEventHandler scheduler = mng.scheduler;
		if (event.getAuthor().getId().equals("225244113161682944")) {
			if (command[0].equals("!volume")) {
				event.getChannel().sendMessage("Can't do that boiiiii");
			}
			if (command[0].equals("!skip")) {
				event.getChannel().sendMessage("Replay enabled.");
			}
			if (command[0].equals("!join")) {
				event.getChannel().sendMessage("Not joining channel *#" + command[1] + "*" );
			}
			if (command[0].equals("!pause")) {
				event.getChannel().sendMessage("lol nope lmao :D");
			}
			return;
		}
		if (command[0].equals("!join")) {
        
	        if (event.getAuthor().isBot()) return;
	       
	        VoiceChannel channel = guild.getVoiceChannelsByName("general", true).get(0);
	
	        // MySendHandler should be your AudioSendHandler implementation
	        guild.getAudioManager().setSendingHandler(mng.getSendHandler());
	        // Here we finally connect to the target voice channel 
	        // and it will automatically start pulling the audio from the MySendHandler instance
	        guild.getAudioManager().openAudioConnection(channel); 
	        
	        
	    } else if ("!play".equals(command[0])) {
            if (command.length == 1) //It is only the command to start playback (probably after pause)
            {
                if (player.isPaused())
                {
                    player.setPaused(false);
                    event.getChannel().sendMessage("Playback as been resumed.").queue();
                }
                else if (player.getPlayingTrack() != null)
                {
                    event.getChannel().sendMessage("Player is already playing!").queue();
                }
                else if (scheduler.queue.isEmpty())
                {
                    event.getChannel().sendMessage("The current audio queue is empty! Add something to the queue first!").queue();
                }
            } else {
                loadAndPlay(mng, event.getChannel(), command[1], false);
            }
        } else if (command[0].equals("!pause")) {
	    	player.setPaused(!player.isPaused());
	    	if (player.isPaused()) {
	    		event.getChannel().sendMessage("Paused.").queue();;
	    	} else event.getChannel().sendMessage("Resumed.").queue();;
	    } else if (command[0].equals("!stop")) {
	    	player.stopTrack();
            player.setPaused(false);
	    } else if (command[0].equals("!removeMSG")) {
	    	event.getChannel().deleteMessageById(command[1].trim()).queue();
	    	event.getChannel().deleteMessageById(event.getMessageId()).queue();
	    } else if (command[0].equals("!removeLatest")) {
	    	int i = Integer.valueOf(command[1]);
	    	TextChannel channel = event.getChannel();
	    	List<Message> messages = channel.getIterableHistory().complete();
	    	while (i >= 0) {
	    		channel.deleteMessageById(messages.get(i).getId()).queue();
	    		i--;
	    	}
	    } else if ("!nowplaying".equals(command[0]) || "!np".equals(command[0])) {
            AudioTrack currentTrack = player.getPlayingTrack();
            if (currentTrack != null)
            {
                String title = currentTrack.getInfo().title;
                String position = getTimestamp(currentTrack.getPosition());
                String duration = getTimestamp(currentTrack.getDuration());

                String nowplaying = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                        title, position, duration);

                event.getChannel().sendMessage(nowplaying).queue();
            }
            else
                event.getChannel().sendMessage("The player is not currently playing anything!").queue();
        } else if ("!skip".equals(command[0])) {
        	scheduler.nextTrack();
        } else if ("!volume".equals(command[0])) {
        	player.setVolume(Integer.valueOf(command[1]));
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
                if (mng.player.getPlayingTrack() == null)
                    msg += "\nand the Player has started playing;";

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
