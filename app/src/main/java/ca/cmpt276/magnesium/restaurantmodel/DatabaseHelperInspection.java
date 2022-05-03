package ca.cmpt276.magnesium.restaurantmodel;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class DatabaseHelperInspection extends SQLiteOpenHelper {

    public static final String DATABASE_INSP_NAME = "inspection.db";
    public static final String TABLE_INSP_NAME = "inspection_table";

    // Inspection database column names
    public static final String COL_1="TrackingNumber";
    public static final String COL_2 = "InspectionDate";
    public static final String COL_3 = "InspType";
    public static final String COL_4 = "NumCritical";
    public static final String COL_5 = "NumNonCritical";
    public static final String COL_6 = "HazardRating";
    public static final String COL_7 = "ViolLump";

    public Context contextActivity;

    public DatabaseHelperInspection(Context context) {
        super(context, DATABASE_INSP_NAME, null, 1);
        contextActivity = context;
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        ensureInspectionDBCreation(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_INSP_NAME);
        onCreate(sqLiteDatabase);
    }

    public void insertData(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Ensure DB is starting from scratch with the csv as our base:
        String dropDB = "DROP TABLE IF EXISTS " + TABLE_INSP_NAME;
        db.execSQL(dropDB);
        ensureInspectionDBCreation(db);

        // Now add back values:
        ReadingCSVInspection inspection = new ReadingCSVInspection(contextActivity);
        Log.d("DatabaseHelperInspsection", "beginning insert");
        DateTime startTime = DateTime.now();

        // Create statement string and compile it:
        String insertString = String.format(
                "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                TABLE_INSP_NAME, COL_1,  COL_2, COL_3, COL_4, COL_5, COL_6, COL_7);
        SQLiteStatement insertStatement = db.compileStatement(insertString);
        // Break the transaction into chunks:
        db.beginTransaction();
        int innerCount = 0;
        for (int i = 0; i < inspection.getInspectionArrayList().size(); i++){
            // Bind values to our statement:
            insertStatement.bindString(1, inspection.getInspectionReportAtPos(i).getTrackingNumber());
            insertStatement.bindString(2, inspection.getInspectionReportAtPos(i).getInspectionDate().toString("yyyyMMdd"));
            insertStatement.bindString(3, inspection.getInspectionReportAtPos(i).getInspectionType().toString());
            insertStatement.bindLong(4, inspection.getInspectionReportAtPos(i).getNumCritical());
            insertStatement.bindLong(5, inspection.getInspectionReportAtPos(i).getNumNonCritical());
            insertStatement.bindString(6, inspection.getInspectionReportAtPos(i).getHazardRating().toString());
            insertStatement.bindString(7, inspection.getInspectionReportAtPos(i).getViolationStatement());
            long entryID = insertStatement.executeInsert();

            if(entryID == -1){
                Log.e("TABLE INSERTION ERROR","Error inserting data at i = " + i + "\n" +
                        inspection.getInspectionReportAtPos(i).toString()); //It had an error inserting data
            }
            if (innerCount >= 1000) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.beginTransaction();
                innerCount = 0;
            }
            insertStatement.clearBindings();
            innerCount++;
        }
        // End final transaction:
        db.setTransactionSuccessful();
        db.endTransaction();

        DateTime endTime = DateTime.now();
        Log.d("DatabaseHelperInspsection", "Insert complete");
        Seconds difference = Seconds.secondsBetween(startTime, endTime);
        db.close();
    }

    public void ensureInspectionDBCreation(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_INSP_NAME + " (" +
                COL_1 + " TEXT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT, " +
                COL_4 + " INTEGER, " +
                COL_5 + " INTEGER, " +
                COL_6 + " TEXT, " +
                COL_7 + " TEXT, " +
                "FOREIGN KEY ("+COL_1+") REFERENCES "+
                DatabaseHelperFacility.TABLE_FACILITY_NAME+"("+DatabaseHelperFacility.COL_1+"));"
        );
    }
}
