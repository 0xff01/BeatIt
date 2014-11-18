package de.ese.beatit;

import java.util.ArrayList;
import java.util.Collections;
import java.math.*;

public class PulseControl extends Thread
{
	
	private int refPulse = 0;
	private int actPulse = 0;
	private ArrayList<Integer> actPulseHist;
	private boolean histFilled = false;
	private int histCnt = 0;
	
	public static final int HIST_SIZE = 300;
	
	public static final int MEAN_SIZE = 20;
	public static final int MODE_SIZE = 90;
	
	public final int FREQ_HZ = 1;
	
	
	@Override public void run()
	{
		// mean stuff
		ArrayList<Integer> meanFilterList = new ArrayList<Integer>();
		meanFilterList.ensureCapacity(MEAN_SIZE);
		int sum = 0;
		int mean = 0;
		int mode = 0;
		
		// mode stuff
		ArrayList<Integer> modeFilterList = new ArrayList<Integer>();
		modeFilterList.ensureCapacity(MODE_SIZE);
		
		while(true)
		{
			// get new act pulse
			actPulse = 60;
			// sort act pulse in hist array
			if (histFilled)
			{
				// shift array and add new act pulse
				Collections.rotate(actPulseHist, -1);
				actPulseHist.set(HIST_SIZE-1, actPulse);
			}
			else
			{
				// fill array with first act pulse
				for (int i = 0; i < HIST_SIZE; i++)
				{
					actPulseHist.add(actPulse);
				}
				
				for (int i = 0; i < MODE_SIZE; i++)
				{
					modeFilterList.add(actPulse);
				}
				histFilled = true;
				
				// fill array with new act pulse
				/*
				actPulseHist.add(actPulse);
				histCnt++;
				if(histCnt == HIST_SIZE)
				{
					histFilled = true;
				}
				*/
			}
			
			// use mean filter
			meanFilterList = (ArrayList<Integer>) actPulseHist.subList(HIST_SIZE-1-MEAN_SIZE, HIST_SIZE-1);
			for(int i = 0; i <= MEAN_SIZE; i++)
			{
				sum += meanFilterList.get(i);
			}
			mean = sum/MEAN_SIZE;
			
			// use mode filter
			Collections.rotate(modeFilterList, -1);
			modeFilterList.set(MODE_SIZE-1, mean);
			
			
			// compare to refPulse
			
			// wait for 1000/FREQ_HZ ms
			try {
				Thread.sleep(1000/FREQ_HZ);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	public PulseControl()
	{
		this.refPulse = 60;
		this.actPulse = 0;
		this.actPulseHist = new ArrayList<Integer>();
		this.actPulseHist.ensureCapacity(HIST_SIZE);
		this.histFilled = false;
		this.histCnt = 0;
		start();
	}
	
	public void setRefPulse( int refPulse)
	{
		this.refPulse = refPulse;
	}
	
	//private class Control implements Runnable
	//{
		
	//}
}
