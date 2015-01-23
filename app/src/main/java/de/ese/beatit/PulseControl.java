package de.ese.beatit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

import android.os.Environment;
import de.ese.beatit.mp3.BeatChangeListener;
import de.ese.beatit.mp3.MP3Player;
import de.ese.beatit.pulsereader.BluetoothService;

//public class PulseControl implements Runnable
public class PulseControl extends TimerTask implements BeatChangeListener
{
	private MP3Player player = null;
	
	// testarray
	public int[] testPulse = new int[40];
	public double[] testMode = new double[40];
	
	private int refPulse = 155;
	private int actPulse = 0;
	private int bpm = 60;
    private int bpmPlayed;
    private int lastChangeTime = 0;
	private int testCntr = 0;
    private static long timeDiff = 0;
	
	// runtime counter
	int runCnt = 0;
	
	// filter stuff
    private static int FILTER_TYPE = 0;

	public static final int MEAN_SIZE = 20;
	private int meanFilterCnt = 0;
	public static final int MODE_SIZE = 70;
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

    // filter state stuff
    private final int MAX_STATE = 4;
    private final int INIT_STATE = MAX_STATE;
    private static int state = 0;
    private static int BPM_Tick = 0;
    private final int BPM_UPDATE_WAIT = 20;
	
	private final int NEG_HYSTERESIS = 3;
	private final int POS_HYSTERESIS = 3;

    private final int BPM_REFRESH_WAIT = 40;

    private final int BPM_CHANGE = 10;
	
	private File logFile = null;
	private FileWriter logWriter = null;
	private String logName = null;
	
	private Boolean LOG_ENABLED = true;
	
	
	@Override public void run()
	{
		//Date now = new Date();
        Long now1 = (long) (System.currentTimeMillis());
		// get new act pulse
		//actPulse = testPulse[testCntr];
        Boolean bpmChange = false;
		actPulse = BluetoothService.getCurrentPulseRate();

        // check for filter type
        if(FILTER_TYPE == 1)
        {
            // use mean filter
            meanFIFO.addFirst(actPulse);
            if (meanFilterCnt >= MEAN_SIZE) {
                sum += actPulse;
                sum -= meanFIFO.removeLast();
                mean = sum / MEAN_SIZE;
            } else {
                sum += actPulse;
                meanFilterCnt++;
                mean = sum / meanFilterCnt;
            }
            //mean = sum/MEAN_SIZE;

            // use mode filter
            modeFIFO.addFirst(mean);
            if (modeMap.containsKey(mean)) {
                modeMap.put(mean, modeMap.get(mean) + 1);
            } else {
                modeMap.put(mean, 1);
            }
            if (modeFilterCnt > MODE_SIZE) {
                int tempKey = modeFIFO.removeLast();
                int tempVal = modeMap.get(tempKey);
                if (tempVal > 1) {
                    modeMap.put(tempKey, tempVal - 1);
                } else {
                    modeMap.remove(tempKey);
                }
            } else {
                modeFilterCnt++;
            }

            // search map
            Iterator<Integer> modeFilterIt = modeFIFO.iterator();
            int tempKey = 0;
            int tempVal = 0;
            int tempMaxKey = 0;
            int tempMaxVal = 0;
            while (modeFilterIt.hasNext()) {
                tempKey = modeFilterIt.next();
                tempVal = modeMap.get(tempKey);
                if (tempVal > tempMaxVal) {
                    tempMaxKey = tempKey;
                    tempMaxVal = tempVal;
                }
            }
            modeKey = tempMaxKey;
            modeVal = tempMaxVal;

            //testMode[testCntr] = tempMaxKey;

            // compare to refPulse
            if ((modeKey < refPulse - NEG_HYSTERESIS) && (runCnt - BPM_REFRESH_WAIT > lastChangeTime)) {
                this.bpm -= this.BPM_CHANGE;
                this.lastChangeTime = runCnt;
                bpmChange = true;
            } else if ((modeKey > refPulse + POS_HYSTERESIS) && (runCnt - BPM_REFRESH_WAIT > lastChangeTime)) {
                this.bpm += this.BPM_CHANGE;
                this.lastChangeTime = runCnt;
                bpmChange = true;
            }
        }
        else if (FILTER_TYPE == 2) {
            // use mean filter
            meanFIFO.addFirst(actPulse);
            if (meanFilterCnt >= MEAN_SIZE) {
                sum += actPulse;
                sum -= meanFIFO.removeLast();
                mean = sum / MEAN_SIZE;
            } else {
                sum += actPulse;
                meanFilterCnt++;
                mean = sum / meanFilterCnt;
            }

            if(runCnt >= BPM_Tick + state*BPM_UPDATE_WAIT)
            {
                if (mean < refPulse - NEG_HYSTERESIS)
                {
                    bpm += BPM_CHANGE;
                    if (state > 1)
                    {
                        state--;
                    }
                    BPM_Tick = runCnt;
                    bpmChange = true;
                }
                else if(mean > refPulse + POS_HYSTERESIS)
                {
                    bpm -= BPM_CHANGE;
                    if (state > 1)
                    {
                        state--;
                    }
                    BPM_Tick = runCnt;
                    bpmChange = true;
                }
                else
                {
                    if (state < MAX_STATE) {
                        state = state + 1;
                    }
                    BPM_Tick = runCnt;
                }
            }
        }


		//return bpm;
        // TODO only if changes and not too often
        if(player != null){
            if (bpmChange) {
                player.setBpm(bpm);
            }
        }
        
		if(LOG_ENABLED)
		{
			try {
				logWriter.append(this.runCnt + ", " + this.refPulse + ", " + this.actPulse + ", " + modeKey + ", " + this.bpm + ", " + this.bpmPlayed + "\n");
				//logWriter.append("test1");
				logWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//testCntr++;
		runCnt++;
        Long now2 = (long) (System.currentTimeMillis());
        this.timeDiff = now2 - now1;
	}
	
	public PulseControl()
	{
		this.refPulse = 160;
		this.actPulse = 0;
		this.bpm = 80;
        this.state = this.INIT_STATE;
        this.FILTER_TYPE = 2;
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

	public void setPlayer(MP3Player mp3Player) {
		mp3Player.setListener(this);
		this.player = mp3Player;
	}

	@Override
	public void onBPMChanged(int bpm) {
		this.bpmPlayed = bpm;
		// TODO react
	}
}
