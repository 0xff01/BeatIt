package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.util.Log;

public class BeatAnalyzerService extends Service {

	private boolean DEBUG = true;
	private String DEBUG_FS_PREFIX = "/storage/emulated/0/ese/";
	
	/** Adapter class to communicate with app **/
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
	private int timerIntervalMinutes = 2;
	private int minDurationSeconds = 20;
	
	private BeatAnalyzerServiceBinder binder = new BeatAnalyzerServiceBinder(this);
	
	private TrackDatabase database = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	@Override
	public void onCreate() {
	
		// load database
		database = new TrackDatabase();
		
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
		ArrayList<String> mp3Files = mp3Files();
		for(String s : mp3Files){
			
			// in debug only analyze files from given directory
			if(DEBUG && !s.startsWith(DEBUG_FS_PREFIX)){
				continue;
			}
			Log.e("beatit", s);
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
		
		// save database
		database.save();
	}
	
	Pair<String, BeatDescription> chooseTrack(int bpm){
		return database.track(bpm);
	}

	ArrayList<String> mp3Files(){
		
		ArrayList<String> paths = new ArrayList<String>();
		
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
					int songDuration = Integer.parseInt(cursor.getString(1));
					
					if(songDuration > minDurationSeconds && (path.endsWith(".mp3") || path.endsWith(".MP3"))){
						paths.add(path);
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
}
