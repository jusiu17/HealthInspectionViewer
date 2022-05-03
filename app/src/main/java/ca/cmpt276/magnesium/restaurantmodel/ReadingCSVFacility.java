package ca.cmpt276.magnesium.restaurantmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import ca.cmpt276.magnesium.healthinspectionviewer.R;

public class ReadingCSVFacility {
    private ArrayList<Facility> facilitiesList;
    private static Context context;

    public ReadingCSVFacility(Context cont) {
        context = cont;
        setFacilityRes(getFacilityFromTextFile());
    }

    public static ArrayList<Facility> getFacilityFromTextFile(){
        InputStream is = null;
        BufferedReader bufferedReader = null;
        ArrayList<Facility> facilityArrayList = new ArrayList<Facility>();
        SharedPreferences prefs = context.getSharedPreferences(DataUpdater.prefsFile, 0);
        try{
            // Try to use the new file, but fallback to the default if necessary.
            String fileLocation = prefs.getString(DataUpdater.downloadedRestaurantFilenameKey, null);
            if (fileLocation != null) {
                File downloadedRestaurants = new File(fileLocation);
                if (downloadedRestaurants.exists()) {
                    is = new FileInputStream(downloadedRestaurants);
                } else {
                    is = context.getResources().openRawResource(R.raw.restaurants_current);
                }
            } else {
                is = context.getResources().openRawResource(R.raw.restaurants_current);
            }

            bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            // Save line we get from csv file
            String line = null;

            //Save Facility
            String[] strFacility = null;

            bufferedReader.readLine(); // Skip first line since tha is column name
            while (true){
                //read line
                line = bufferedReader.readLine();

                // Check if line exists
                if (line != null){
                    strFacility = line.replace("\"", "").split(",");
                    int index = 0;
                    String trackingNumber = strFacility[index];
                    index++;
                    String name;
                    if (strFacility.length == 7) {
                        name = strFacility[index];
                        index++;
                    } else {
                        name = strFacility[index] + ", " + strFacility[index + 1];
                        index += 2;
                    }
                    String address = strFacility[index];
                    index++;
                    String city = strFacility[index];
                    index++;
                    String facilityType = strFacility[index];
                    index++;
                    Double latitude = Double.parseDouble(strFacility[index]);;
                    index++;
                    Double longitude = Double.parseDouble(strFacility[index]);
                    facilityArrayList.add(new Facility(trackingNumber, name,
                            address, city,
                            facilityType,
                            latitude,
                            longitude));
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
        return facilityArrayList;
    }

    public ArrayList<Facility> getFacilityArrayList() {
        return facilitiesList;
    }

    public Facility getFacilityAtPos(int i){
        return facilitiesList.get(i);
    }
    public void setFacilityRes(ArrayList<Facility> facilityRes) {
        this.facilitiesList = facilityRes;
    }
}
