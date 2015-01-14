package de.ese.beatit.mp3;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import de.ese.beatit.beatanalyzer.TrackDatabaseListener;

public class MP3Player extends TrackDatabaseListener {

	private Timer fadeOutTimer;
	
	private boolean paused = false;
	
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
	private final int fadeTimeSeconds = 4;
	
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
	
	/**
	 * sets the bpm value. changes the track if playing. 
	 */
	public void setBpm(double bpm){
		
		if(bpm == this.bpm){
			return;
		}
		
		this.bpm = bpm;
		
		if(isPlaying()){
			Track track = getDatabase().getTrack(bpm, skippedTracks);
			play(track);
		}
	}
	
	/**
	 * plays the given track.
	 */
	private void play(Track track){
		
		if(track == null){
			return;
		}
		
		paused = false;
		
		if(!initialized || currentTrack == track){
			return;
		}
		
		// fading
		final boolean isFading = currentTrack != null && mPlayer != null;
		
		// fade out
		if(isFading){
			
			Log.e("beatit", "stop track "+currentTrack.getPath());
			
			final MediaPlayer fadeOut = mPlayer;
			
			fadeOutTimer = new Timer();
			fadeOutTimer.scheduleAtFixedRate(new TimerTask() {

				// ms of timer
				int ms = 0;
				
				float volume = 1.0f;
				
	    		@Override
	    		public void run() {
	    			
	    			if((double)ms / 1000 > fadeTimeSeconds){
	    				fadeOut.stop();
	    				
	    				cancel();
	    				return;
	    			}
	    			
	    			// set volume
	    			volume = 1.0f - ((float)ms / 1000) / fadeTimeSeconds; 
	    			fadeOut.setVolume(volume,  volume);
	    			
	    			// increase time
	    			ms += 10;
	    		}
	    		
	    	}, 0, 10);
		}
		
		Log.e("beatit", "play track "+track.getPath());
		
		currentTrack = track;
		
		mPlayer = MediaPlayer.create(context, Uri.fromFile(new File(currentTrack.getPath())));
		mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			Timer timer;
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				
				final MediaPlayer toPlay = mp;
				
				// report track change
				for(MP3PlayerListener listener : mp3PlayerListeners){
					listener.onTrackChanged(currentTrack);
				}				
				
				// install timer to update position
		    	timer = new Timer();
		    	timer.scheduleAtFixedRate(new TimerTask() {

		    		float volume = 0f;
		    		
		    		@Override
		    		public void run() {
		    			
		    			if(paused){
		    				return;
		    			}
		    			
	    				if(toPlay.isPlaying()){
	    				
	    					int pos = toPlay.getCurrentPosition();
	    					
	    					if(volume != 1.0f){
		    					// get volume
		    					if(isFading && (float)pos / 1000 <= fadeTimeSeconds){
		    		    			volume = ((float)pos / 1000) / fadeTimeSeconds; 
		    					} else {
		    						volume = 1.0f;
		    					}
		    					toPlay.setVolume(volume,  volume);	    						
	    					}

	    					// if close to end, play next song
	    					if(pos / 1000 >= currentTrack.getDuration() - (fadeTimeSeconds + 1)){
	    						next();
	    						cancel();
	    					}
	    					
	    					// report position
	    					if(toPlay == mPlayer){
		    					for(MP3PlayerListener listener : mp3PlayerListeners){
		    						listener.onPlaybackTimeChanged(((double)pos / 1000));
		    					}
	    					}
	    			
	    				} else {
	    					cancel();
	    				}
	    			}
		    	}, 0, 10); 
			}
		});
		
		mPlayer.start();
		
		if(bpmListener != null){
			bpmListener.onBPMChanged(track.getBeatDescription().getBpm());
		}
	}
	
	
	/** skips the current track **/
	public void skip(){
		
		// report skip
		skippedTracks.add(currentTrack);
		onSkippedTrackListUpdated();
		
		next();
	}
	
	/** next song **/
	public void next(){
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
	
	public boolean isPlaying(){
		if(mPlayer == null){
			return false;
		}
		return mPlayer.isPlaying();
	}

	public void pause() {
		if(mPlayer != null){
			paused = true;
			mPlayer.pause();
		}
	}
	
	public void play(){
		if((mPlayer != null) && !mPlayer.isPlaying()){
			mPlayer.start();
			paused = false;
		} else if(mPlayer == null){
			next();
		}
	}
}
