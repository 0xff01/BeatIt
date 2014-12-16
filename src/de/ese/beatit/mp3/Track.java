package de.ese.beatit.mp3;

import de.ese.beatit.beatanalyzer.BeatDescription;

public class Track {

	/** tracks path **/
	private String path, name;
	
	/** bpm and first beat position **/
	private BeatDescription beatDescription = null;
	
	/** duration in seconds **/
	private double duration;
	
	public BeatDescription getBeatDescription() {
		return beatDescription;
	}
	
	public void setBeatDescription(BeatDescription beatDescription) {
		this.beatDescription = beatDescription;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
		int sli = path.lastIndexOf('/');
		if(sli != -1){
			name = path.substring(sli+1);
			int pi = name.lastIndexOf(".");
			if(pi > -1){
				name = name.substring(0, pi);
			}
		} else {
			name = path;
		}
	}
	
	public void setDuration(double songDuration) {
		duration = songDuration;		
	}
	
	public double getDuration(){
		return duration;
	}
	
	public String getName(){
		return name;
	}
}
