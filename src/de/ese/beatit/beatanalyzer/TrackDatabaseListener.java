package de.ese.beatit.beatanalyzer;

public abstract class TrackDatabaseListener {

	public abstract void onTrackCountChanged(int newCount);
	public abstract void onDatabaseInitialized();
	
	public TrackDatabase getDatabase() {
		return database;
	}
	public void setDatabase(TrackDatabase database) {
		this.database = database;
	}

	private TrackDatabase database;
	
}
