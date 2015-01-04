package de.ese.beatit.mp3;

import java.io.File;
import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class MP3View extends View implements MP3PlayerListener {
	
	private Context context;
	private Paint p;
	
	// current track
	private Track currentTrack = null;
	private double currentTime = 0;
	
	public MP3View(Context context) {
		super(context);
		this.context = context;
		p = new Paint(Paint.ANTI_ALIAS_FLAG);
	}
	
	public MP3View (Context c, AttributeSet a) {
	    super(c, a);
	    this.context = c;
	    p = new Paint(Paint.ANTI_ALIAS_FLAG);
	}
	
	public MP3View (Context c, AttributeSet a, int i) {
		super(c, a, i);
		this.context = c;
		p = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@SuppressLint("DrawAllocation")
	protected void onDraw (Canvas canvas){
		
		/*if(currentTrack != null){
			
			// beats
			double phase = 2*Math.PI*(currentTime)*currentTrack.getBeatDescription().getBpm() / 60d;
			double v = (1d+Math.cos(phase)) / 2d;
			if(v>0.6){
				canvas.drawColor(Color.DKGRAY);
			}
			else{
				canvas.drawColor(Color.LTGRAY);
			}
			
			
		} else { */
			
		//}
		
		canvas.drawColor(Color.rgb(0xdd, 0xdd, 0xdd));
		
		p.setTextSize(30);
		
		// border around progress
		float processHeight = 60;
		float graypadding = 20;
		
		p.setColor(Color.rgb(0x33, 0xb5, 0xe5));
		RectF timeRect = new RectF(
			graypadding,
			getHeight()-graypadding-processHeight,
			getWidth()-graypadding,
			getHeight()-graypadding);
		
		canvas.drawRect(timeRect, p);

		RectF inner = new RectF(
			graypadding+8,
			getHeight()-graypadding-processHeight+8,
			graypadding+8 + getWidth()-2*graypadding-16,
			getHeight()-graypadding-8);
		
		p.setColor(Color.rgb(0xaa, 0xaa, 0xaa));
		canvas.drawRect(inner, p);
		
		// track name
		if(currentTrack != null){
			
			// progress
			float process = (float)(currentTime / currentTrack.getDuration());
			timeRect = new RectF(
				graypadding+8,
				getHeight()-graypadding-processHeight+8,
				graypadding+8 + process*(getWidth()-2*graypadding-16),
				getHeight()-graypadding-8);
			
			p.setColor(getResources().getColor(android.R.color.holo_orange_dark));
			canvas.drawRect(timeRect, p);
		}
	}

	@Override
	public void onTrackChanged(Track track) {
		currentTrack = track;
		currentTime = 0;
		postInvalidate();
	}

	@Override
	public void onPlaybackTimeChanged(double time) {
		currentTime = time;
		postInvalidate();
	}
}
