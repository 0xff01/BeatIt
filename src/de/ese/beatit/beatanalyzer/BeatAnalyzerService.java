package de.ese.beatit.beatanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import de.ese.beatit.mp3.MP3Loader;
import de.ese.beatit.mp3.PCMData;
import de.ese.beatit.mp3.Track;

public class BeatAnalyzerService extends Service {

	private boolean DEBUG = true;
	private String DEBUG_FS_PREFIX = "/storage/emulated/0/ese/dance";
	
	/** beat analyzer **/
	private MP3Loader mp3Loader = new MP3Loader();
	private int segmentDurationSeconds = 20;
	private BeatAnalyzer beatAnalyzer = new BeatAnalyzer();
	
	/** Adapter class to communicate with Application **/
	public class BeatAnalyzerServiceBinder extends Binder {
		
		private BeatAnalyzerService service;

		public BeatAnalyzerServiceBinder(BeatAnalyzerService s){
			this.service = s;
		}
		
		public BeatAnalyzerService getService() {
			return service;
		}

		public void setService(BeatAnalyzerService service) {
			this.service = service;
		}
	}
	
	private Timer timer = null;
	private int timerIntervalMinutes = 20;
	private int minDurationSeconds = 20;
	
	private BeatAnalyzerServiceBinder binder = new BeatAnalyzerServiceBinder(this);
	
	private final TrackDatabase database = new TrackDatabase();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	@Override
	public void onCreate() {
	
		// load database
		database.load();
		
		// start task which periodically searches for MP3 Files
		// which are not analyzed yet
    	timer = new Timer();
    	timer.scheduleAtFixedRate(new TimerTask() {

    		@Override
    		public void run() {
    			analyzeFileSystem();
    		}
    		
    	}, 0, timerIntervalMinutes * 1000 * 60);  
	}
	
	public void analyzeFileSystem(){
		
		// get all mp3 files from system
		ArrayList<Track> mp3Files = mp3Files();
		for(Track track : mp3Files){
			
			// in debug only analyze files from given directory
			if(DEBUG && !track.getPath().startsWith(DEBUG_FS_PREFIX)){
				continue;
			}
			Log.e("beatit", track.getPath());
			
			// check whether database already knows mp3 file
			if(database.contains(track.getPath())){
				continue;
			}
			
			float bpm = -1;
			
			// read tags
			AudioFile tagFile = null;
			Tag tag = null;
			try {
				tagFile = AudioFileIO.read(new File(track.getPath()));
				tag = tagFile.getTag();
				if(tag.hasField(FieldKey.BPM)){
					String bpmStr = tag.getFirst(FieldKey.BPM);
					float tbpm = Float.parseFloat(bpmStr);
					if(tbpm != 0){
						bpm = tbpm;
					} else {
						Log.e("beatit", "Could not parse BPM from ID3 tags!");
					}
				} else {
					Log.e("beatit", "BPM field does not exist.");
				}
			} catch (CannotReadException e1) {
				e1.printStackTrace();
				tagFile = null;
				tag = null;
				Log.e("beatit", "Could not read ID3 Tags!");
			} catch (IOException e1) {
				e1.printStackTrace();
				tagFile = null;
				tag = null;
				Log.e("beatit", "Could not read ID3 Tags!");
			} catch (TagException e1) {
				e1.printStackTrace();
				tagFile = null;
				tag = null;
				Log.e("beatit", "Could not read ID3 Tags!");
			} catch (ReadOnlyFileException e1) {
				e1.printStackTrace();				
				tagFile = null;
				tag = null;
				Log.e("beatit", "Could not read ID3 Tags!");
			} catch (InvalidAudioFrameException e1) {
				e1.printStackTrace();
				tagFile = null;
				tag = null;
				Log.e("beatit", "Could not read ID3 Tags!");
			}

			BeatDescription beatDescription = null;
			
			if(bpm == -1){
				
				// bpm not known.
				try {
					
					// get pcm
					Log.e("beatit", "get pcm");
					PCMData pcm = mp3Loader.loadMp3Begin(track.getPath(), segmentDurationSeconds);
					
					// check pcm signal validity
					if(!pcm.isValid()){
						continue;
					}
					
					// analyze
					Log.e("beatit", "analyze");
					beatDescription = beatAnalyzer.analyzeData(pcm);
					
					// check certainty of analysis
					if(!beatDescription.isCertain()){
						database.reportUncertainTrack(track.getPath());
						continue;
					}
					
					// save bpm as id3 tag
					if(tag != null && tagFile != null && !tag.hasField(FieldKey.BPM)){
						try {
							tag.setField(FieldKey.BPM, String.valueOf(beatDescription.getBpm()));
							AudioFileIO.write(tagFile);
						} catch (KeyNotFoundException e) {
							Log.e("beatit", "Could not write ID3 BPM Tag!");
							e.printStackTrace();
						} catch (FieldDataInvalidException e) {
							Log.e("beatit", "Could not write ID3 BPM Tag!");
							e.printStackTrace();
						} catch (CannotWriteException e) {
							Log.e("beatit", "Could not write ID3 Tags to File!");
							e.printStackTrace();
						}
					}
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			} else {
				
				// create beat description
				beatDescription = new BeatDescription();
				beatDescription.setCertainty(1);
				beatDescription.setBpm(bpm);
			}
			
			// save to database
			track.setBeatDescription(beatDescription);
			database.insert(track);
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		
		// stop analyzer task
		timer.cancel();
		
		// save database
		database.save();
	}

	ArrayList<Track> mp3Files(){
		
		ArrayList<Track> paths = new ArrayList<Track>();
		
		String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
		
		String[] projection = {
			MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.DURATION
		};
		final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
		
		Cursor cursor = null;
		try {
			
			Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
			if( cursor != null){
				cursor.moveToFirst();
				while( !cursor.isAfterLast() ){
					
					String path = cursor.getString(0);
					double songDuration = (double)Integer.parseInt(cursor.getString(1)) / 1000;
					
					if(songDuration > minDurationSeconds && (path.endsWith(".mp3") || path.endsWith(".MP3"))){
						Track track = new Track();
						track.setPath(path);
						track.setDuration(songDuration);
						paths.add(track);
					}
					cursor.moveToNext();
				}
			}
			
		} catch (Exception e) {
			Log.e("beatit", e.toString());
		} finally{
			if( cursor != null){
				cursor.close();
			}
		}
		
		return paths;
	}
	
	public TrackDatabase database(){
		return database;
	}
}
