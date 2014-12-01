package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import android.support.v4.util.Pair;

public class TrackDatabase {

	/** 
	 * data: indexed by bpm, per bpm there is a list containing paths to
	 * the mp3 file as well as the beat description.
	 */
	private Map<Double, ArrayList<Pair<String, BeatDescription> > > data =
		new HashMap<Double, ArrayList<Pair<String, BeatDescription> > >();
	
	/**
	 * Semaphore to synchronize access.
	 */
	private Semaphore mutex = new Semaphore(1);
	
	/**
	 * Path to the persistent database .xml file.
	 */
	private final String dataPath = "database.xml";
	
	public TrackDatabase(){

		// load database
		
	}
	
	/**
	 * Returns all tracks registered for the given bpm.
	 * Will return tracks with closest bpm if exact bpm has not been found.
	 */
	Pair<String, BeatDescription> track(int bpm){
		
		String path = "";
		BeatDescription bDescr = null;
		
		acquire();
		
		// find closest entry
		double nearestBpm = -1;
		for(double b : data.keySet()){
			if(nearestBpm == -1 || Math.abs(b-bpm) < nearestBpm){
				nearestBpm = Math.abs(b-bpm);
			}
		}
		
		if(nearestBpm != -1){
			
			int num = data.get(nearestBpm).size();
			int i = (int)Math.random()*(num-1);
			if(i<0){
				i = 0;
			}
			
			path = data.get(nearestBpm).get(i).first;
			bDescr = data.get(nearestBpm).get(i).second;
		}
		
		release();
		
		return new Pair<String, BeatDescription>(path, bDescr);
	}
	
	void insert(Pair<String, BeatDescription> entry){
	
		acquire();
		
		if(entry.second != null){
			double bpm = entry.second.getBpm();
			
			if(!data.containsKey(bpm)){
				ArrayList<Pair<String, BeatDescription> > list =
					new ArrayList<Pair<String, BeatDescription> >();
				data.put(bpm, list);
			}
			
			data.get(bpm).add(entry);
		}
		
		release();
	}
	
	private void acquire(){
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			
		}
	}
	
	private void release(){
		mutex.release();
	}
	
	/** saves database to xml **/
	public void save(){
		acquire();
		
		release();
	}

	/** load databse from xml **/
	public void load() {
		acquire();
		
		release();
	}
}
