package de.ese.beatit.mp3;

import java.util.ArrayList;

public class PCMData {

	private int sampleRate;
	private ArrayList<Short> pcmSignal;
	private boolean valid;
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	public ArrayList<Short> getPcmSignal() {
		return pcmSignal;
	}
	
	public void setPcmSignal(ArrayList<Short> pcmSignal) {
		this.pcmSignal = pcmSignal;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
