package de.ese.beatit.mp3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.SampleBuffer;
import android.util.Log;

public class MP3Loader {
	
	// loads pcm signal
	public PCMData loadMp3Begin(String path, int segmentDuration) throws FileNotFoundException {

		javazoom.jl.decoder.Decoder decoder = new javazoom.jl.decoder.Decoder();
		
		// load file
		File file = new File(path);
		
		// prepare buffer
		ArrayList<Short> pcm = new ArrayList<Short>();
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file), 8 * 1024);
		
		int sampleFrequency = 0;
		
		try {
			
            Bitstream bitstream = new Bitstream(inputStream);
            boolean done = false;

            int durationMs = 0;
            
            while (!done) {
                
            	javazoom.jl.decoder.Header frameHeader = bitstream.readFrame();
                if(frameHeader == null) {
                	done = true;
                	break;
                }
                              
                SampleBuffer output = (SampleBuffer)decoder.decodeFrame(frameHeader, bitstream);
                
                short[] next = output.getBuffer();
                for(int i = 0; i<next.length; i+=output.getChannelCount()){
                	short b = 0;
                	for(int c = 0; c<output.getChannelCount(); c++){
                		b+=next[i+c];
                	}
                	pcm.add((short)(b/output.getChannelCount()));
                }
                
                durationMs += frameHeader.ms_per_frame();

                // test duration
                if(durationMs / 1000 >= segmentDuration){
                	done = true;
                }
                
                sampleFrequency = output.getSampleFrequency();
                
                bitstream.closeFrame();
            }
           
            PCMData data = new PCMData();
            data.setSampleRate(sampleFrequency);
            data.setPcmSignal(pcm);
            data.setValid(true);
            
            return data;
            
        }   catch (BitstreamException e) {
        	Log.w("error is:", "Bitstream error", e);
        }   catch (DecoderException e) {
        	Log.w("error is:", "Decoder error", e);
        }
		
        PCMData data = new PCMData();
        data.setValid(false);
        return data;
	}
}
