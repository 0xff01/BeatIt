package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import android.support.v4.util.Pair;

public class TrackDatabase {

	private final int bpmMin = 50;
	private final int bpmMax = 100;
	
	private final int numCells = 10;
	
	private int count = 0;
	
	/** 
	 * data: indexed by bpm, per bpm there is a list containing paths to
	 * the mp3 file as well as the beat description. bpms are givven in ranges.
	 */
	private Map<Integer, ArrayList<Pair<String, BeatDescription> > > data =
		new HashMap<Integer, ArrayList<Pair<String, BeatDescription> > >();
	
	/** all paths which are in the databse **/
	private ArrayList<String> paths = new ArrayList<String>();
	
	/** Semaphore to synchronize access. */
	private Semaphore mutex = new Semaphore(1);
	
	/** Path to the persistent database .xml file. */
	private final String dataPath = "database.xml";
	
	/** adapter **/
	private TrackDatabaseListener listener = null;
	
	public TrackDatabase(){
		
		for(int i = 0; i<numCells; i++){
			ArrayList<Pair<String, BeatDescription> > list =
				new ArrayList<Pair<String, BeatDescription> >();
			data.put(i, list);
		}
	}
	
	/**
	 * Returns all tracks registered for the given bpm.
	 * Will return tracks with closest bpm if exact bpm has not been found.
	 * @param skippedTracks 
	 */
	public Pair<String, BeatDescription> getTrack(double bpm, ArrayList<String> skippedTracks){
		
		String path = "";
		BeatDescription bDescr = null;
		
		acquire();
		
		// get tracks
		ArrayList<Pair<String, BeatDescription> > tracks = null;
		
		// find cell index
		int index = cellIndex(bpm);
		if(data.get(index).size() == 0){
			
			tracks = new ArrayList<Pair<String, BeatDescription> >(); 
			
			for(int i = 0; i<numCells; i++){
				
				int i1 = index-i;
				int i2 = index+i;
				
				boolean foundData = false;
				
				if(i1 >= 0){
					if(data.get(i1).size() != 0){
						tracks.addAll(data.get(i1));
						foundData = true;
					}
				}
				
				if(i2 < numCells){
					if(data.get(i2).size() != 0){
						tracks.addAll(data.get(i2));
						foundData = true;
					}
				}
				
				if(foundData){
					break;
				}
			}
			
		} else {
			tracks = data.get(index);
		}
		
		// find closest entry
		Pair<String, BeatDescription> closestEntry = null;
		
		for(Pair<String, BeatDescription> entry : tracks){
			
			if(closestEntry == null || Math.abs(entry.second.getBpm()-bpm) < Math.abs(closestEntry.second.getBpm()-bpm)){
				closestEntry = entry;
			}
		}
		
		release();
		
		return closestEntry;
	}
	
	void insert(Pair<String, BeatDescription> entry){
	
		acquire();
	
		if(!paths.contains(entry.first) && entry.second != null){
			
			int index = cellIndex(entry.second.getBpm());
			data.get(index).add(entry);
			paths.add(entry.first);
			
			count++;
			
			if(listener != null){
				listener.onTrackCountChanged(count);
			}
		}
		release();
	}
	
	public boolean contains(String path){
		return paths.contains(path);
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
		
		if(listener != null){
			listener.onDatabaseInitialized();
		}
	}
	
	/** returns the cell index for the given bpm **/
	public int cellIndex(double bpm){
		while(bpm < bpmMin){
			bpm *= 2;
		}
		while(bpm > bpmMax){
			bpm /= 2;
		}
		return (int)(bpm - bpmMin) * numCells / (bpmMax - bpmMin);
	}

	public TrackDatabaseListener getListener() {
		return listener;
	}

	public void setListener(TrackDatabaseListener listener) {
		this.listener = listener;
		listener.setDatabase(this);
	}

	public int getCount() {
		return count;
	}
}
