package de.ese.beatit;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.ese.beatit.pulsereader.BluetoothService;

import android.os.Environment;

public class PulseControl implements Runnable
{
	// testarray
	public int[] testPulse = new int[40];
	public double[] testMode = new double[40];
	
	private int refPulse = 0;
	private int actPulse = 0;
	private int bpm = 80;
	private int testCntr = 0;
	
	// runtime counter
	int runCnt = 0;
	
	// filter stuff
	public static final int MEAN_SIZE = 2;
	private int meanFilterCnt = 0;
	public static final int MODE_SIZE = 5;
	private int modeFilterCnt = 0;
	// mean stuff
	ArrayDeque<Integer> meanFIFO = new ArrayDeque<Integer>();
	int sum = 0;
	int mean = 0;
	// mode stuff
	ArrayDeque<Integer> modeFIFO = new ArrayDeque<Integer>();
	Map<Integer, Integer> modeMap = new HashMap<Integer, Integer>();
	double modeKey = 0;
	int modeVal = 0;
	
	private final int NEG_HYSTERESIS = 3;
	private final int POS_HYSTERESIS = 3;
	
	private File logFile = null;
	private FileWriter logWriter = null;
	private String logName = null;
	
	private Boolean LOG_ENABLED = true;
	
	
	@Override public void run()
	{
		
		// get new act pulse
		//actPulse = testPulse[testCntr];
		actPulse = BluetoothService.getCurrentPulseRate();
		
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
			int tempKey = modeFIFO.removeLast();
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
		Iterator<Integer> modeFilterIt = modeFIFO.iterator();
		int tempKey = 0;
		int tempVal = 0;
		int tempMaxKey = 0;
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
		
		//testMode[testCntr] = tempMaxKey;
		
		// compare to refPulse
		if (modeKey < refPulse - NEG_HYSTERESIS)
		{
			// change bpm here
		}
		else if (modeKey > refPulse + POS_HYSTERESIS)
		{
			// change bpm here
		}
		
		//return bpm;
		if(LOG_ENABLED)
		{
			try {
				logWriter.append(this.testCntr + ", " + this.refPulse + ", " + this.actPulse + ", " + modeKey + "\n");
				//logWriter.append("test1");
				logWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//testCntr++;
		runCnt++;
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
			this.testPulse[i] = 90;
		}
		for (int i = 30; i < 40; i++)
		{
			this.testPulse[i] = 120-i;
		}
		this.testCntr = 0;
		this.runCnt = 0;
		if(LOG_ENABLED)
		{
			Date now = new Date();
			this.logName = "BeatIt_log_" + now.getTime() + ".txt";
			if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				this.logFile = new File("/storage/emulated/0/BeatIt", logName);
				logFile.getParentFile().mkdirs();
				
				if (!logFile.exists()) {
					try {
						logFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					this.logWriter = new FileWriter(logFile.getAbsoluteFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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
