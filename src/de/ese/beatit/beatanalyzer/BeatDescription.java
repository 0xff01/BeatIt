package de.ese.beatit.beatanalyzer;

public class BeatDescription {

	private double bpm;
	private double firstBeatPosition;
	
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
}
