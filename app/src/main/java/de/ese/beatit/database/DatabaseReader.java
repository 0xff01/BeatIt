package de.ese.beatit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.ese.beatit.beatanalyzer.BeatDescription;
import de.ese.beatit.mp3.Track;

/**
 * Created by jan on 1/27/15.
 */
public class DatabaseReader {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private String[] allColumns = {
            DatabaseHelper.COLUMN_FILE_PATH,
            DatabaseHelper.COLUMN_TRACK_NAME,
            DatabaseHelper.COLUMN_ARTIST,
            DatabaseHelper.COLUMN_BPM,
            DatabaseHelper.COLUMN_CERTAINTY,
            DatabaseHelper.COLUMN_IS_CERTAIN,
            DatabaseHelper.COLUMN_DURATION
    };

    public DatabaseReader(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Track createTrackEntry(String dataPath, String track, String artist, double duration, BeatDescription description) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FILE_PATH, dataPath);
        values.put(DatabaseHelper.COLUMN_TRACK_NAME, track);
        values.put(DatabaseHelper.COLUMN_ARTIST, artist);
        values.put(DatabaseHelper.COLUMN_BPM, description.getBpm());
        values.put(DatabaseHelper.COLUMN_CERTAINTY, description.getCertainty());
        values.put(DatabaseHelper.COLUMN_IS_CERTAIN, description.isCertain());
        values.put(DatabaseHelper.COLUMN_DURATION, duration);

        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, allColumns, null, null, null, null, null);
        // Cursor cursor = db.rawQuery("select * from " +DatabaseHelper.TABLE_NAME, null);
        cursor.moveToFirst();
        Track newTrack = cursorToTrack(cursor);
        cursor.close();
        return newTrack;
    };

    private Track cursorToTrack(Cursor cursor) {

        Track singleTrack = new Track();
        singleTrack.setPath(cursor.getString(0));
        singleTrack.setName(cursor.getString(1));
        singleTrack.setArtist(cursor.getString(2));
        BeatDescription desc = new BeatDescription();
        desc.setBpm(cursor.getInt(3));
        desc.setCertainty(cursor.getDouble(4));
        desc.isCertain();
        singleTrack.setBeatDescription(desc);
        singleTrack.setDuration(cursor.getDouble(6));

        return singleTrack;
    }

    public List<Track> getAllTracks() {

        List<Track> tracks = new ArrayList<Track>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, allColumns, null, null, null, null, null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            Track track = cursorToTrack(cursor);
            tracks.add(track);
            cursor.moveToNext();
        }

        cursor.close();

        return tracks;
    }
}
