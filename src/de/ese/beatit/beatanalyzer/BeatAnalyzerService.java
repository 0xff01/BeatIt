package de.ese.beatit.beatanalyzer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.util.Pair;
import android.util.Log;

public class BeatAnalyzerService extends Service {

	/** Adapter class to communicate with service **/
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

}
