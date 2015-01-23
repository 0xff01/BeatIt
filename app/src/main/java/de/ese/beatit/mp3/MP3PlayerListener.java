package de.ese.beatit.mp3;

public interface MP3PlayerListener {

	public void onTrackChanged(Track track);
	public void onPlaybackTimeChanged(double time);

    public void onPlay();
    public void onPause();
}
