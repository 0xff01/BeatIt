package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import de.ese.beatit.mp3.Track;

public class TrackDatabase {

	private final int bpmMin = 50;
	private final int bpmMax = 100;
	
	private final int numCells = 10;
	
	private int count = 0;
	
	private boolean initialized = false;
	
	/** 
	 * data: indexed by bpm, per bpm there is a list containing paths to
	 * the mp3 file as well as the beat description. bpms are givven in ranges.
	 */
	private SparseArray<ArrayList<Track> > data =
		new SparseArray<ArrayList<Track> >();
	
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
			ArrayList<Track> list = new ArrayList<Track>();
			data.put(i, list);
		}
	}
	
	/**
	 * Returns all tracks registered for the given bpm.
	 * Will return tracks with closest bpm if exact bpm has not been found.
	 * @param skippedTracks 
	 */
	public Track getTrack(double bpm, ArrayList<Track> skippedTracks){
		
		acquire();
		
		// get tracks
		ArrayList<Track> tracks = null;
		
		// find cell index
		int index = cellIndex(bpm);
		if(data.get(index).size() == 0){
			
			tracks = new ArrayList<Track>(); 
			
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
		Track closestEntry = null;
		
		for(Track entry : tracks){
			
			if(skippedTracks.contains(entry)){
				continue;
			}
			if(closestEntry == null || Math.abs(entry.getBeatDescription().getBpm()-bpm) < Math.abs(closestEntry.getBeatDescription().getBpm()-bpm)){
				closestEntry = entry;
			}
		}
		
		release();
		
		return closestEntry;
	}
	
	void insert(Track entry){
	
		Log.e("beatit", "insert");
		
		boolean added = false;
		
		acquire();
	
		if(!(paths.contains(entry.getPath())) && entry.getBeatDescription()!= null){
			
			Log.e("beatit", "do insert");
			
			int index = cellIndex(entry.getBeatDescription().getBpm());
			data.get(index).add(entry);
			paths.add(entry.getPath());
			
			added = true;
			
			count++;
		}
		release();
		
		if(added && listener != null){
			listener.onTrackCountChanged(count);
		}
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
		
		// TODO
		
		initialized = true;
		
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
		while(bpm >= bpmMax){
			bpm /= 2;
		}
		return (int)(bpm - bpmMin) * numCells / (bpmMax - bpmMin);
	}

	public TrackDatabaseListener getListener() {
		return listener;
	}

	public void setListener(TrackDatabaseListener listener) {
		this.listener = listener;
		this.listener.setDatabase(this);
		if(initialized){
			this.listener.onDatabaseInitialized();
		}
	}

	public int getCount() {
		return count;
	}

	public void reportUncertainTrack(String path) {
		
		acquire();
		
		// just add to pths and not to tracks
		paths.add(path);
		
		Log.e("beatit", "Added uncertain track "+path);
		release();
	}
}
