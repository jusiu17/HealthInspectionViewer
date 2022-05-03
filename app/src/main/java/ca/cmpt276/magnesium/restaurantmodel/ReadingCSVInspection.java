package ca.cmpt276.magnesium.restaurantmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import ca.cmpt276.magnesium.healthinspectionviewer.R;

public class ReadingCSVInspection {
    private ArrayList<InspectionReport> inspection;
//    private static String filePath;
    private static Context context;

    public ReadingCSVInspection(Context cont) {
        context = cont;
        setInspectionList(getInspectionFromTextFile());
    }

    public static ArrayList<InspectionReport> getInspectionFromTextFile(){

        InputStream is = null;
        BufferedReader bufferedReader = null;
        ArrayList<InspectionReport> inspectionArrayList = new ArrayList<InspectionReport>();
        SharedPreferences prefs = context.getSharedPreferences(DataUpdater.prefsFile, 0);

        try{
            // Try to use the new file, but fallback to the default if necessary.
            String fileLocation = prefs.getString(DataUpdater.downloadedInspectionFilenameKey, null);
            if (fileLocation != null) {
                File downloadedInspections = new File(fileLocation);
                if (downloadedInspections.exists()) {
                    is = new FileInputStream(downloadedInspections);
                } else {
                    is =  context.getResources().openRawResource(R.raw.inspectionreports_current);
                }
            } else {
                is =  context.getResources().openRawResource(R.raw.inspectionreports_current);
            }
            bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            // Save line we get from csv file
            String line = null;

            //Save InspectionReport
            String[] strInspection = null;

            bufferedReader.readLine(); // Skip first line since tha is column name

            while (true){
                //read line
                line = bufferedReader.readLine();

                if (line != null){
                    strInspection = line.replace("\"", "").trim().split(",");
                    // Disable logging for performance benefits
                    // Log.d("STRING_INPECTION_LENGTH", "" + strInspection.length);

                    //InspectionReport(String track, String date, String inspecType, int critical,
                    //                            int nonCritical, String hazardRate, String statement)
                    if (strInspection.length > 4) {
                        String hazardRating;
                        int violStartIndex = 5;
                        int numCritical = Integer.parseInt(strInspection[3]);
                        int numNonCritical = Integer.parseInt(strInspection[4]);

                        String violation;
                        if (strInspection.length <= 6){
                            violation = " ";
                            hazardRating = "Low";
                        } else {
                            StringBuilder builder = new StringBuilder();
                            for (int i = violStartIndex; i < strInspection.length; i++) {
                                builder.append(strInspection[i]);
                                builder.append(",");
                            }
                            violation = builder.toString();

                            if ((strInspection[5].equalsIgnoreCase("Low")) ||
                                    (strInspection[5].equalsIgnoreCase("Moderate")) ||
                                    (strInspection[5].equalsIgnoreCase("High"))) {
                                violStartIndex = 6;
                                hazardRating = strInspection[5];
                            } else {
                                if ((numCritical + numNonCritical) == 0) {
                                    hazardRating = "Low";
                                } else if ((numCritical < 3) && ((numCritical + numNonCritical) < 6)) {
                                    hazardRating = "Moderate";
                                } else {
                                    hazardRating = "High";
                                }
                            }
                        }

                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
                        String inspectionDate = strInspection[1].replace("-", "");
                        LocalDate inspectionLocalDate = formatter.parseLocalDate(inspectionDate);

                        inspectionArrayList.add(new InspectionReport(strInspection[0], inspectionLocalDate, strInspection[2],
                                numCritical, numNonCritical,
                                hazardRating, violation));
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e){
            Log.d("DEBUG FILE ERROR", "Error Reading File");
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return inspectionArrayList;
    }

    public ArrayList<InspectionReport> getInspectionArrayList() {
        return inspection;
    }

    public InspectionReport getInspectionReportAtPos(int i){
        return inspection.get(i);
    }

    public void setInspectionList(ArrayList<InspectionReport> inspRep) {
        this.inspection = inspRep;
    }
}
