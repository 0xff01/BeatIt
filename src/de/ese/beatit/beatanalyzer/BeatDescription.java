package de.ese.beatit.beatanalyzer;

public class BeatDescription {

	private int bpm;
	private double firstBeatPosition;
	
	public int getBpm() {
		return bpm;
	}
	
	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public double getFirstBeatPosition() {
		return firstBeatPosition;
	}

	public void setFirstBeatPosition(double firstBeatPosition) {
		this.firstBeatPosition = firstBeatPosition;
	}
}
