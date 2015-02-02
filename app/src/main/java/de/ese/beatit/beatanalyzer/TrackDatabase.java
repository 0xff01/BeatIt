package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import de.ese.beatit.MainActivity;
import de.ese.beatit.database.DatabaseReader;
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

    /** Database Reader/Writer **/
    DatabaseReader dbreader = new DatabaseReader(MainActivity.getInstance());

    /** List of available bpms **/
    List<Integer> availBPMList = new ArrayList<Integer>();
    ArrayList<Track> allTheTracks = new ArrayList<Track>();
	
	public TrackDatabase(){
		
		for(int i = 0; i<numCells; i++){
			ArrayList<Track> list = new ArrayList<Track>();
			data.put(i, list);
		}

        dbreader.open();

        if (dbreader.getAllTracks().size() > 0) {

            /** Add tracks from database to path array **/
            for (int i = 0; i < dbreader.getAllTracks().size(); i++) {
                Track currentTrack = dbreader.getAllTracks().get(i);
                paths.add(i, currentTrack.getPath());
                insert(currentTrack);
                int index = cellIndex(currentTrack.getBeatDescription().getBpm());
                data.get(index).add(currentTrack);
                count++;

                allTheTracks.add(currentTrack);

                if (!(availBPMList.contains(currentTrack.getBeatDescription().getBpm())))
                    availBPMList.add(currentTrack.getBeatDescription().getBpm());
            }
        }
        for (Integer entry : availBPMList) Log.d("JanDebug", String.valueOf(entry));
	}
	
	/**
	 * Returns all tracks registered for the given bpm.
	 * Will return tracks with closest bpm if exact bpm has not been found.
	 * @param skippedTracks 
	 */
	public Track getTrack(double bpm, ArrayList<Track> skippedTracks){

        Track closestEntry = null;

        ArrayList<Track> possibleTracks = new ArrayList<Track>();
        ArrayList<Track> cleanedTracks;

        if (availBPMList.contains(bpm)) {

            Log.d("fetchSound", "BPM found");

            for (Track currentTrack : allTheTracks) {
                if (currentTrack.getBeatDescription().getBpm() == bpm) {
                    possibleTracks.add(currentTrack);
                }
            }

            cleanedTracks = possibleTracks;

            cleanedTracks.removeAll(skippedTracks);

            Log.d("fetchSound", String.valueOf(possibleTracks.size()));
            Log.d("fetchSound", String.valueOf(cleanedTracks.size()));

            if (cleanedTracks.size() > 0) {
                closestEntry = cleanedTracks.get(0);
            }
            else {
                closestEntry = possibleTracks.get(0);
            }
        }
        else {

            Log.d("fetchSound", "BPM not found");

            for (Track currentTrack : allTheTracks) {

                if (currentTrack.getBeatDescription().getBpm() <= (bpm + 10) && currentTrack.getBeatDescription().getBpm() >= (bpm - 10)) {
                    possibleTracks.add(currentTrack);
                }
            }

            cleanedTracks = possibleTracks;

            cleanedTracks.removeAll(skippedTracks);

            Log.d("fetchSound", String.valueOf(possibleTracks.size()));
            Log.d("fetchSound", String.valueOf(cleanedTracks.size()));

            if (cleanedTracks.size() > 0) {

                closestEntry = cleanedTracks.get(0);

                for (Track currTrack : cleanedTracks) {

                    if (Math.abs(currTrack.getBeatDescription().getBpm() - bpm) < Math.abs(closestEntry.getBeatDescription().getBpm() - bpm)) {
                        closestEntry = currTrack;
                    }
                }
            }
            else {
                closestEntry = possibleTracks.get(0);

                for (Track currTrack : possibleTracks) {

                    if (Math.abs(currTrack.getBeatDescription().getBpm() - bpm) < Math.abs(closestEntry.getBeatDescription().getBpm() - bpm)) {
                        closestEntry = currTrack;
                    }
                }
            }
        }

		return closestEntry;
	}
	
	void insert(Track entry){
		
		boolean added = false;

		acquire();

		if(!(paths.contains(entry.getPath())) && entry.getBeatDescription()!= null){
			
			int index = cellIndex(entry.getBeatDescription().getBpm());
			data.get(index).add(entry);
			paths.add(entry.getPath());

            dbreader.createTrackEntry(entry.getPath(), entry.getName(), entry.getArtist(), entry.getDuration(), entry.getBeatDescription());
			
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
            this.listener.onTrackCountChanged(count);
		}
	}

	public int getCount() {
		return count;
	}

	public void reportUncertainTrack(String path) {
		
		acquire();
		
		// just add to pths and not to tracks
		paths.add(path);

		release();
	}
}
