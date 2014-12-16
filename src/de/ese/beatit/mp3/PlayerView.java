package de.ese.beatit.mp3;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.ese.beatit.R;

public class PlayerView implements MP3PlayerListener{

	// seek slider
	private SeekBar bpmSlider;
	
	// bpm view
	private TextView bpmView;
	
	// player
	private MP3Player player;
	
	// MP3View
	private MP3View mp3View;
	
	public PlayerView(View view, MP3Player player){
		
		this.player = player;
		
		// init components
		bpmView = (TextView)(view.findViewById(R.id.bpm_display));
		
		bpmSlider = (SeekBar)(view.findViewById(R.id.bpm_slider));
		setBPM(60);
		bpmSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setBPM(seekBar.getProgress()+30);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		
		mp3View = (MP3View)(view.findViewById(R.id.mp3_view));
		
		// init as listeners
		player.addMp3PlayerListener(this);
		player.addMp3PlayerListener(mp3View);
	}
	
	private void setBPM(int bpm){
		bpmSlider.setProgress(bpm-30);
		bpmView.setText(String.valueOf(bpm));
		player.setBpm(bpm);
	}

	@Override
	public void onTrackChanged(Track track) {
		
	}

	@Override
	public void onPlaybackTimeChanged(double time) {
		
	}
}
