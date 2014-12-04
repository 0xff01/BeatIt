package de.ese.beatit.mp3;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;
import de.ese.beatit.beatanalyzer.BeatDescription;
import de.ese.beatit.beatanalyzer.TrackDatabaseListener;

public class MP3Player extends TrackDatabaseListener {

	/** skipped tracks is a list of skippedTracksCountMax tracks the user skipped **/
	private ArrayList<Track> skippedTracks = new ArrayList<Track>();
	private int skippedTracksCountMax;

	/** current track **/
	Track currentTrack = null;
	
	/** initialized or not **/
	private boolean initialized = false;
	
	/** context **/
	private Context context = null;
	
	/** current bpm **/
	private double bpm = 0;
	
	/** bpm listener **/
	private BeatChangeListener bpmListener = null;
	
	/** mp3 listeners **/
	private ArrayList<MP3PlayerListener> mp3PlayerListeners = new ArrayList<MP3PlayerListener>();
	
	/** player **/
	private MediaPlayer mPlayer;
	
	public MP3Player(Context c){
		context = c;
		Log.e("beatit", "Created MP3Player");
	}
	
	@Override
	public void onTrackCountChanged(int newCount) {
		
		skippedTracksCountMax = newCount / 10;
		onSkippedTrackListUpdated();
		
		Log.e("beatit", "database count changed");
	}

	@Override
	public void onDatabaseInitialized() {
		initialized = true;
		Log.e("beatit", "Initialized Database / MP3Player");
	}
	
	public void onSkippedTrackListUpdated(){
		while(skippedTracks.size() > skippedTracksCountMax){
			skippedTracks.remove(0);
		}
	}
	
	/** plays a song with a close bpm **/
	public void setBpm(double bpm){
		
		this.bpm = bpm;
		
		Track track = getDatabase().getTrack(bpm, skippedTracks);
		play(track);
	}
	
	private void play(Track track){
		if(initialized && currentTrack != track){
			
			if(currentTrack != null && mPlayer != null){
				Log.e("beatit", "stop track "+currentTrack.getPath());
				mPlayer.stop();
			}
			
			
			Log.e("beatit", "play track "+track.getPath());
			
			currentTrack = track;
			
			mPlayer = MediaPlayer.create(context, Uri.fromFile(new File(currentTrack.getPath())));
			mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				
				Timer timer = null;
						
				@Override
				public void onPrepared(MediaPlayer mp) {
					
					for(MP3PlayerListener listener : mp3PlayerListeners){
		
						// report track change
						listener.onTrackChanged(currentTrack);
					}
					
					// install timer to update position
			    	timer = new Timer();
			    	final MediaPlayer player = mPlayer;
			    	
			    	timer.scheduleAtFixedRate(new TimerTask() {

			    		@Override
			    		public void run() {
			    			
			    			if(mPlayer == player){
			    				
			    				if(mPlayer.isPlaying()){
			    					int pos = mPlayer.getCurrentPosition();
			    					
			    					for(MP3PlayerListener listener : mp3PlayerListeners){
			    						
			    						// report position
			    						listener.onPlaybackTimeChanged(((double)pos / 1000));
			    					}
			    				}
			    				
			    			} else {
			    				timer.cancel();
			    				
			    				if(mPlayer == null){
			    					
			    					// stopped
			    					for(MP3PlayerListener listener : mp3PlayerListeners){
			    						
			    						// report track change
			    						listener.onTrackChanged(null);
			    					}
			    				}
			    			}
			    			
			    		}
			    		
			    	}, 0, 10); 
				}
				
			});
			
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					
					// play next song. TODO exclude current Track
					Track track = getDatabase().getTrack(bpm, skippedTracks);
					play(track);				
				}
			});
			
			mPlayer.start();
			
			if(bpmListener != null){
				bpmListener.onBPMChanged(track.getBeatDescription().getBpm());
			}
		}
	}
	
	/** skips the current track **/
	public void skip(){
		
		// report skip
		skippedTracks.add(currentTrack);
		onSkippedTrackListUpdated();
		
		// play next song
		Track track = getDatabase().getTrack(bpm, skippedTracks);
		play(track);
	}
	
	public BeatChangeListener getListener() {
		return bpmListener;
	}

	public void setListener(BeatChangeListener listener) {
		this.bpmListener = listener;
	}
	
	public void addMp3PlayerListener(MP3PlayerListener listener){
		mp3PlayerListeners.add(listener);
	}
}
