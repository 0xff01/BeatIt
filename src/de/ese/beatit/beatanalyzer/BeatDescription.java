package de.ese.beatit.beatanalyzer;

public class BeatDescription {

	private double bpm;
	private double firstBeatPosition;
	private double certainty;
	
	/** certainty threshold **/
	private final double certaintyThreshold = 1.0E-5d;
	
	public double getBpm() {
		return bpm;
	}
	
	public void setBpm(double bpm2) {
		this.bpm = bpm2;
	}

	public double getFirstBeatPosition() {
		return firstBeatPosition;
	}

	public void setFirstBeatPosition(double firstBeatPosition) {
		this.firstBeatPosition = firstBeatPosition;
	}

	public double getCertainty() {
		return certainty;
	}

	public void setCertainty(double certainty) {
		this.certainty = certainty;
	}
	
	public boolean isCertain(){
		return certainty >= certaintyThreshold;
	}
}
