package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;

import de.ese.beatit.mp3.PCMData;

public class BeatAnalyzer {

	/** rms values per second **/
	private final int rmsRate = 500;
	
	public BeatDescription analyzeData(PCMData pcm){
		
		// get rms signal
		double[] rms = rms(pcm.getPcmSignal(), pcm.getSampleRate(), rmsRate);
		
		return null;
	}
	
	
	private double[] rms(ArrayList<Short> pcm, int sampleFrequency, int rmsRate){
		
		// get duration of pcm data
		double duration = (double)(pcm.size())/(double)sampleFrequency;
		
		// compute array size
		int size = (int)(duration * rmsRate);
		
		// num pcm values per rms value
		int pcmPerRms = sampleFrequency / rmsRate;
		
		if(size * pcmPerRms >= pcm.size()){
			size--;
		}
		
		double[] d = new double[size];
		
		for(int i=0; i<size; i++){
				
			// squared mean
			double msq = 0;
			for(int j=0; j<pcmPerRms; j++){
				msq += pcm.get(i*pcmPerRms+j)*pcm.get(i*pcmPerRms+j);
			}
			msq /= pcmPerRms;
			
			// root
			d[i] = Math.sqrt(msq);
		}
		
		return d;
	}
}
