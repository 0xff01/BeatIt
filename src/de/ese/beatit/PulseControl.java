package de.ese.beatit;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PulseControl implements Runnable
{
	// testarray
	public int[] testPulse = new int[40];
	public double[] testMode = new double[40];
	
	private int refPulse = 0;
	private int actPulse = 0;
	private int bpm = 80;
	private int testCntr = 0;
	
	
	// filter stuff
	public static final int MEAN_SIZE = 2;
	private int meanFilterCnt = 0;
	public static final int MODE_SIZE = 20;
	private int modeFilterCnt = 0;
	// mean stuff
	ArrayDeque<Integer> meanFIFO = new ArrayDeque<Integer>();
	int sum = 0;
	double mean = 0;
	// mode stuff
	ArrayDeque<Double> modeFIFO = new ArrayDeque<Double>();
	Map<Double, Integer> modeMap = new HashMap<Double, Integer>();
	double modeKey = 0;
	int modeVal = 0;
	
	
	@Override public void run()
	{
		
		// get new act pulse
		actPulse = testPulse[testCntr];
		
		// use mean filter
		meanFIFO.addFirst(actPulse);
		if (meanFilterCnt >= MEAN_SIZE)
		{
			sum += actPulse;
			sum -= meanFIFO.removeLast();
			mean = sum/MEAN_SIZE;
		}
		else
		{
			sum+= actPulse;
			meanFilterCnt++;
			mean = sum/meanFilterCnt;
		}
		//mean = sum/MEAN_SIZE;
		
		// use mode filter
		modeFIFO.addFirst(mean);
		if (modeMap.containsKey(mean))
		{
			modeMap.put(mean, modeMap.get(mean) + 1);
		}
		else
		{
			modeMap.put(mean, 1);
		}
		if (modeFilterCnt > MODE_SIZE)
		{
			double tempKey = modeFIFO.removeLast();
			int tempVal = modeMap.get(tempKey);
			if (tempVal > 1)
			{
				modeMap.put(tempKey, tempVal -1);
			}
			else
			{
				modeMap.remove(tempKey);
			}
		}
		else
		{
			modeFilterCnt++;
		}
		
		// search map
		Iterator<Double> modeFilterIt = modeFIFO.iterator();
		double tempKey = (float) 0.0;
		int tempVal = 0;
		double tempMaxKey = 0.0;
		int tempMaxVal = 0;
		while (modeFilterIt.hasNext())
		{
			tempKey = modeFilterIt.next();
			tempVal = modeMap.get(tempKey);
			if (tempVal > tempMaxVal)
			{
				tempMaxKey = tempKey;
				tempMaxVal = tempVal;
			}
		}
		modeKey = tempMaxKey;
		modeVal = tempMaxVal;
		
		testMode[testCntr] = tempMaxKey;
		
		// compare to refPulse
		if (modeKey < refPulse)
		{
			// change bpm here
		}
		else if (modeKey > refPulse)
		{
			// change bpm here
		}
		
		//return bpm;
		testCntr++;
	}
	
	public PulseControl()
	{
		this.refPulse = 60;
		this.actPulse = 0;
		this.bpm = 80;
		for (int i = 0; i < 10; i++)
		{
			this.testPulse[i] = 80;
		}
		for (int i = 10; i < 20; i++)
		{
			this.testPulse[i] = 70 + i;
		}
		for (int i = 20; i < 30; i++)
		{
			this.testPulse[i] = 110 - i;
		}
		for (int i = 30; i < 40; i++)
		{
			this.testPulse[i] = 80;
		}
		this.testCntr = 0;
	}
	
	public void setRefPulse( int refPulse)
	{
		this.refPulse = refPulse;
	}
	
	public int getBpm()
	{
		return this.bpm;
	}

	
}
