package de.ese.beatit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jan on 1/27/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "musicDB";
    public static final String COLUMN_FILE_PATH = "path";
    public static final String COLUMN_TRACK_NAME = "track";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_BPM = "bpm";
    public static final String COLUMN_CERTAINTY = "certainty";
    public static final String COLUMN_IS_CERTAIN = "isCertain";
    public static final String COLUMN_DURATION = "duration";


    private static final String DATABASE_NAME = "tracks.db";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "("
            + COLUMN_FILE_PATH + " text not null, "
            + COLUMN_TRACK_NAME + " text not null, "
            + COLUMN_ARTIST + " text not null, "
            + COLUMN_BPM + " integer not null, "
            + COLUMN_CERTAINTY + " REAL not null, "
            + COLUMN_IS_CERTAIN + " integer not null, "
            + COLUMN_DURATION + " REAL not null);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
