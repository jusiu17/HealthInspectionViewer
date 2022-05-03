package ca.cmpt276.magnesium.restaurantmodel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import ca.cmpt276.magnesium.healthinspectionviewer.R;

/**
 * SFU CMPT 276
 * Term Project - Team Magnesium
 *
 * Restaurant Model: Database Reader Class
 * Reads from the SQLite database and creates objects
 * for use elsewhere in the restaurant model.
 * Mostly contains static factory methods.
 *
 * @author dlutz
 *
 */

public class DatabaseReader {

    private Context activityContext; // Needed for calling DBHelperFacility

    public DatabaseReader(Context activityContext) {
        this.activityContext = activityContext;
    }

    /**
     * Create a Facility object from records in the database.
     * @param trackingNumber the tracking number of the restaurant
     * @return Facility if found in db, null if not in db
     */
    public static Facility facilityFactory(String trackingNumber) {
        // TODO - implement this stub
        Facility returnFacility = null;

        return returnFacility;
    }

    /**
     * Create an InspectionReport object from records in the database.
     * @param trackingNumber the tracking number of the report
     * @return InspectionReport if found in db, null if not in db
     */
    public static InspectionReport inspectionReportFactory(String trackingNumber) {
        // TODO - implement this stub
        InspectionReport returnReport = null;

        return returnReport;
    }

    public ArrayList<String> getAllTrackingNums() {
        ArrayList<String> returnArray = new ArrayList<>();

        // Get the dbHelper instance:
        DatabaseHelperFacility dbHelper = new DatabaseHelperFacility(activityContext);
        // Make sure the table has been created properly:
        dbHelper.ensureFacilityDBCreation(dbHelper.getWritableDatabase());
        // Get database to read from:
        SQLiteDatabase facilityDB = dbHelper.getReadableDatabase();

        String allTrackingNumQuery = "SELECT " + DatabaseHelperFacility.COL_1 + " FROM " +
                DatabaseHelperFacility.TABLE_FACILITY_NAME;

        // Read all values from the SQLite database!
        // No "where" clause here:
        Cursor queryResults = facilityDB.rawQuery(allTrackingNumQuery, null);

        // Now, go through the Cursor and add all resulting values to the ArrayList:
        // But only do it if the result is not empty.
        if (queryResults.moveToFirst()) {
            while (!queryResults.isAfterLast()) {
                String trackingNum = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_1)
                );
                returnArray.add(trackingNum);
                // Advance the Cursor:
                queryResults.moveToNext();
            }
        }

        // All values should have been added.
        return returnArray;
    }

    public ArrayList<Facility> getAllFacilities() {
        ArrayList<Facility> returnArray = new ArrayList<>();

        // Get the dbHelper instance:
        DatabaseHelperFacility dbHelper = new DatabaseHelperFacility(activityContext);
        // Make sure the table has been created properly:
        dbHelper.ensureFacilityDBCreation(dbHelper.getWritableDatabase());
        // Get database to read from:
        SQLiteDatabase facilityDB = dbHelper.getReadableDatabase();

        String allTrackingNumQuery = "SELECT * FROM " +
                DatabaseHelperFacility.TABLE_FACILITY_NAME
                + " ORDER BY " + DatabaseHelperFacility.COL_2 + " ASC";

        // Cursor to peruse all results:
        Cursor queryResults = facilityDB.rawQuery(allTrackingNumQuery, null);

        // Now, go through the Cursor and add all resulting values to the ArrayList:
        // But only do it if the result is not empty.
        if (queryResults.moveToFirst()) {
            while (!queryResults.isAfterLast()) {
                String trackingNum = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_1)
                );
                String name = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_2)
                );
                String physicalAddr = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_3)
                );
                String city = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_4)
                );
                String facilityTypeStr = queryResults.getString(
                            queryResults.getColumnIndex(
                                    DatabaseHelperFacility.COL_5));
                Double latitude = Double.parseDouble(queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_6))
                );
                Double longitude = Double.parseDouble(queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperFacility.COL_7))
                );

                // Attempt to convert facilityType string to enum:
                FacilityType facilityType;
                try {
                    facilityType = FacilityType.valueOf(facilityTypeStr);
                } catch (IllegalArgumentException e) {
                    // Failed to convert, let it be null.
                    facilityType = null;
                }

                // Set the restaurant ID based on a few context clues:
                Integer iconID;
                if(name.toLowerCase().contains("freshslice")
                        && name.toLowerCase().contains("pizza")) {
                    iconID = R.drawable.freshslice;
                } else if(name.toLowerCase().contains("burger")
                        && name.toLowerCase().contains("king")){
                    iconID = R.drawable.burgerking;
                } else if (name.toLowerCase().contains("pizza")) {
                    iconID = R.drawable.pizza;
                } else if (name.toLowerCase().contains("seafood")
                        || name.toLowerCase().contains("sushi")) {
                    iconID = R.drawable.fish;
                } else if (name.toLowerCase().contains("burger")) {
                    iconID = R.drawable.burger;
                } else if(name.toLowerCase().contains("a&w")){
                    iconID = R.drawable.aandw;
                }  else if(name.toLowerCase().contains("donalds")){
                    iconID = R.drawable.mcdonald;
                } else if(name.toLowerCase().contains("hortons")){
                    iconID = R.drawable.timhortons;
                } else if(name.toLowerCase().contains("eleven")){
                    iconID = R.drawable.seveneleven;
                } else if(name.toLowerCase().contains("kfc")){
                    iconID = R.drawable.kfc;
                } else if(name.toLowerCase().contains("subway")){
                    iconID = R.drawable.subway;
                } else if(name.toLowerCase().contains("starbucks")){
                    iconID = R.drawable.starbucks;
                } else if(name.toLowerCase().contains("wendy's")){
                    iconID = R.drawable.wendys;
                }
                else {
                    // Default "generic" icon: kitchen
                    iconID = R.drawable.kitchen;
                }



                String inspectionString =
                        getInspectionString(
                                getAllAssociatedInspections(trackingNum)
                        );

                Facility currentFacility = new Facility( trackingNum, name, physicalAddr,
                        city, facilityType, latitude, longitude, iconID, inspectionString);

                returnArray.add(currentFacility);
                // Advance the Cursor:
                queryResults.moveToNext();
            }
        }

        // All values should have been added.
        dbHelper.close();
        return returnArray;
    }

    private String getInspectionString(ArrayList<InspectionReport> inspections) {
        String returnString;

        if (inspections.size() > 0) {
            returnString = inspections.get(0).getInspectionDateString();
        } else {
            returnString = "N/A"; // Case where there are no associated inspections
        }

        return returnString;
    }

    public ArrayList<InspectionReport> getAllAssociatedInspections(String trackingNo) {
        ArrayList<InspectionReport> returnArray = new ArrayList<>();

        // Get the dbHelper instance:
        DatabaseHelperInspection dbHelper = new DatabaseHelperInspection(activityContext);
        // Make sure the table has been created properly:
        dbHelper.ensureInspectionDBCreation(dbHelper.getWritableDatabase());
        // Get database to read from:
        SQLiteDatabase inspectionDB = dbHelper.getReadableDatabase();

        String allRelatedInspectionQuery = "SELECT * FROM " + DatabaseHelperInspection.TABLE_INSP_NAME
                + " WHERE " + DatabaseHelperInspection.COL_1 + " == ?"
                + " ORDER BY " + DatabaseHelperInspection.COL_2 + " DESC";

        // Cursor to peruse all results:
        Cursor queryResults = inspectionDB.rawQuery(allRelatedInspectionQuery,
                                                    new String[] {trackingNo});

        // Now, go through the Cursor and add all resulting values to the ArrayList:
        // But only do it if the result is not empty.
        if (queryResults.moveToFirst()) {
            while (!queryResults.isAfterLast()) {
                String inspectionTrackingNum = queryResults.getString(
                                queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_1));
                String inspectionDate = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_2));
                String inspectionType = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_3));
                String inspectionNumCritical = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_4));
                String inspectionNumNonCritical = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_5));
                String inspectionHazardRating = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_6));
                String inspectionViolations = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_7));

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
                inspectionDate = inspectionDate.replace("-", "");
                LocalDate inspectionLocalDate = formatter.parseLocalDate(inspectionDate);

                InspectionReport report = new InspectionReport(inspectionTrackingNum,
                                                        inspectionLocalDate,
                                                        inspectionType,
                                                        Integer.valueOf(inspectionNumCritical),
                                                        Integer.valueOf(inspectionNumNonCritical),
                                                        inspectionHazardRating,
                                                        inspectionViolations);

                returnArray.add(report);

                // Advance the Cursor:
                queryResults.moveToNext();
            }
        }

        // All reports should have been added.
        dbHelper.close();
        queryResults.close();
        return returnArray;
    }

    public InspectionReport getFirstAssociatedInspection(String trackingNo) {
        InspectionReport returnInspection = null;
        // Get the dbHelper instance:
        DatabaseHelperInspection dbHelper = new DatabaseHelperInspection(activityContext);
        // Make sure the table has been created properly:
        dbHelper.ensureInspectionDBCreation(dbHelper.getWritableDatabase());
        // Get database to read from:
        SQLiteDatabase inspectionDB = dbHelper.getReadableDatabase();

        String allRelatedInspectionString = "SELECT * FROM " + DatabaseHelperInspection.TABLE_INSP_NAME
                + " WHERE " + DatabaseHelperInspection.COL_1 + " == ?"
                + " ORDER BY " + DatabaseHelperInspection.COL_2 + " DESC";

        // Cursor to peruse all results:
        Cursor queryResults = inspectionDB.rawQuery(allRelatedInspectionString,
                new String[] {trackingNo});

        // Now, go through the Cursor and add all resulting values to the ArrayList:
        // But only do it if the result is not empty.
        if (queryResults.moveToFirst()) {
                String inspectionTrackingNum = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_1));
                String inspectionDate = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_2));
                String inspectionType = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_3));
                String inspectionNumCritical = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_4));
                String inspectionNumNonCritical = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_5));
                String inspectionHazardRating = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_6));
                String inspectionViolations = queryResults.getString(
                        queryResults.getColumnIndex(
                                DatabaseHelperInspection.COL_7));

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
                inspectionDate = inspectionDate.replace("-", "");
                LocalDate inspectionLocalDate = formatter.parseLocalDate(inspectionDate);

                InspectionReport report = new InspectionReport(inspectionTrackingNum,
                        inspectionLocalDate,
                        inspectionType,
                        Integer.valueOf(inspectionNumCritical),
                        Integer.valueOf(inspectionNumNonCritical),
                        inspectionHazardRating,
                        inspectionViolations);

                returnInspection = report;
        }

        // All reports should have been added.
        dbHelper.close();
        queryResults.close();
        return returnInspection;
    }


}
