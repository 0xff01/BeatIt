package de.ese.beatit.mp3;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;
import de.ese.beatit.beatanalyzer.BeatDescription;
import de.ese.beatit.beatanalyzer.TrackDatabaseListener;

public class MP3Player extends TrackDatabaseListener {

	/** skipped tracks is a list of skippedTracksCountMax tracks the user skipped **/
	private ArrayList<String> skippedTracks = new ArrayList<String>();
	private int skippedTracksCountMax;

	/** current track **/
	String currentTrack = "";
	
	/** initialized or not **/
	private boolean initialized = false;
	
	/** context **/
	private Context context = null;
	
	/** current bpm **/
	private double bpm = 0;
	
	/** bpm listener **/
	private BeatChangeListener listener = null;
	
	public MP3Player(Context c){
		context = c;
	}
	
	@Override
	public void onTrackCountChanged(int newCount) {
		skippedTracksCountMax = newCount / 10;
		onSkippedTrackListUpdated();
		
		// TODO temp
		if(newCount > 2){
			setBpm(60.0);
		}
	}

	@Override
	public void onDatabaseInitialized() {
		initialized = true;
	}
	
	public void onSkippedTrackListUpdated(){
		while(skippedTracks.size() > skippedTracksCountMax){
			skippedTracks.remove(0);
		}
	}
	
	/** plays a song with a close bpm **/
	public void setBpm(double bpm){
		
		this.bpm = bpm;
		
		Pair<String, BeatDescription> track = getDatabase().getTrack(bpm, skippedTracks);
		play(track);	
	}
	
	private void play(Pair<String, BeatDescription> track){
		if(initialized && currentTrack != track.first){
			
			Log.e("beatit", track.first);
			
			currentTrack = track.first;
			
			MediaPlayer mPlayer = MediaPlayer.create(context, Uri.fromFile(new File(currentTrack)));
			mPlayer.start();
			
			if(listener != null){
				listener.onBPMChanged(track.second.getBpm());
			}
		}
	}
	
	/** skips the current track **/
	public void skip(){
		
		// report skip
		skippedTracks.add(currentTrack);
		onSkippedTrackListUpdated();
		
		// play next song
		Pair<String, BeatDescription> track = getDatabase().getTrack(bpm, skippedTracks);
		play(track);
	}

	public BeatChangeListener getListener() {
		return listener;
	}

	public void setListener(BeatChangeListener listener) {
		this.listener = listener;
	}
}
