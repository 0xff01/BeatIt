package de.ese.beatit.beatanalyzer;

import java.util.ArrayList;

import android.util.Log;
import de.ese.beatit.mp3.PCMData;

public class BeatAnalyzer {

	/** rms values per second **/
	private final int rmsRate = 500;
	
	private final double bpmMin = 50;
	private final double bpmMax = 200;
	
	/**
	 * Analyzes the pcm signal. If bpm or firstBeatPosition are not known, pass -1.
	 * @param pcm
	 * @param bpm
	 * @param firstBeatPosition
	 * @return
	 */
	public BeatDescription analyzeData(PCMData pcm, double bpm, double firstBeatPosition){
		
		// get rms signal
		double[] rms = rms(pcm.getPcmSignal(), pcm.getSampleRate(), rmsRate);
		
		// bpm
		double certainty = 0;
		
		if(bpm == -1){
						
			// dt per rmsSample
			double dt = 1d / rmsRate;
			
			final double tmin = 60d / bpmMax;
			final double tmax = 60d / bpmMin;
			final int numShifts = (int)Math.ceil((tmax-tmin) / dt);
			final int maxShift =  (int)Math.ceil(tmax / dt);
			
			final int correlationSteps = rms.length - maxShift;
			
			double[] correlation = new double[numShifts];
			
			int maxIndex = -1;
			
			for(int i = 0; i<numShifts; i++){
				
				double ct = 0;
				
				// time to shift
				final double t = i * (tmax - tmin) / numShifts + tmin;
				
				final int shift = (int)(t / dt);
				
				for(int j = 0; j<correlationSteps; j++){
					ct += rms[j]*rms[j+shift];
				}
				
				correlation[i] = ct;
				
				// save maximum index	
				if(maxIndex == -1 || correlation[maxIndex] < ct){
					maxIndex = i;
				}
			}
			
			// bpm
			double tmaxcorr = maxIndex * (tmax - tmin) / numShifts + tmin;
			bpm = 60d / tmaxcorr;
			
			Log.e("beatit", "bpm="+String.valueOf(bpm));
			
			certainty = variance(correlation);
			Log.e("beatit", "certainty="+String.valueOf(certainty));
			
		} else {
			certainty = 1;
		}
		
		// first beat
		if(firstBeatPosition == -1){
			
			final double fbeat = bpm / 60d;
			final int numShifts = (int)Math.ceil(rmsRate / fbeat);

			double[] correlation = new double[numShifts];
			
			int maxIndex = -1;
			
			for(int i = 0; i<numShifts; i++){
				
				double c = 0;
				int n = 0;
				
				int ii = i;				
				while(ii < rms.length){
					
					c += rms[ii];
					n++;
					
					ii = ii+numShifts;
				}
				
				if(n!=0){
					c /= n;
				}
				
				correlation[i] = c;
				
				// save maximum index	
				if(maxIndex == -1 || correlation[maxIndex] < c){
					maxIndex = i;
				}
			}
			
			firstBeatPosition = (double)(maxIndex) / (double)rmsRate;
			
			Log.e("beatit", "firstbeat[s]="+String.valueOf(firstBeatPosition));
		}
		
		BeatDescription description = new BeatDescription();
		description.setBpm(bpm);
		description.setFirstBeatPosition(firstBeatPosition);
		description.setCertainty(certainty);
		
		Log.e("beatit", "analyzed");
		
		return description;
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
	
	private double variance(double[] d){
		
		// normalize
		double sum = 0;
		for(int i = 0; i<d.length; i++){
			sum += d[i];
		}
		double mean = 0;
		double[] norm = new double[d.length];
		for(int i = 0; i<d.length; i++){
			norm[i] = d[i] / sum;
			mean += norm[i];
		}
		mean /= norm.length;
		
		// variance
		double var = 0;
		for(int i = 0; i<norm.length; i++){
			var += (norm[i] - mean) * (norm[i] - mean);
		}
				
		return Math.sqrt(var / norm.length);
	}
}
