package ca.cmpt276.magnesium.restaurantmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jakewharton.processphoenix.ProcessPhoenix;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import java.io.File;

/**
 *  DataUpdater class
 *  Facilitates reading of updated data from remote server.
 *  Attempts to operate as atomically as possible to allow cancellation.
 *
 *  We use the Google Volley library to help with async web connections.
 *
 *  March 17, 2020
 *  Devon Lutz, SFU ID 301349016
 *  CMPT 276 - Project - Sprint 2
 *
 */


public class DataUpdater {

    // Useful constants
    private static final String surreyRestaurantGetURL =
            "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
    private static final String surreyInspectionsGetURL =
            "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";
    public static final String prefsFile = "healthInspection_dataUpdater_prefs";
    public static final String updateAvailableRestaurantBool = "dataUpdater_updateAvailableRestaurant_bool";
    public static final String updateAvailableInspectionBool = "dataUpdater_updateAvailableInspection_bool";
    public static final String restaurantCheckKey = "dataUpdater_restaurant_lastUpdated";
    public static final String inspectionCheckKey = "dataUpdater_inspection_lastUpdated";
    private static final String resourcesString = "resources";
    private static final String modifiedString = "last_modified";
    private static final String restaurantCsvUrlString = "url";
    private static final String restaurantCsvUrlKey = "dataUpdater_updateAvailableRestaurant_url";
    private static final String inspectionCsvUrlString = "url";
    private static final String inspectionCsvUrlKey = "dataUpdater_updateAvailableInspection_url";
    private static final String updateIgnoredString = "update_dismissed";
    private static final String lastCheckDateString = "dataUpdater_last_checked";

    private static final String inProgressDownloadRestaurantIDKey = "dataUpdater_restaurant_download_id";
    private static final String inProgressDownloadInspectionIDKey = "dataUpdater_inspection_download_id";

    public static final String downloadedRestaurantFilenameKey = "dataUpdater_restaurant_filename";
    public static final String downloadedInspectionFilenameKey = "dataUpdater_inspection_filename";

    private static final String dbUpdateRequiredKey = "dataUpdater_dbRequiredUpdate_key";
    private static final String dbUpdateFirstRun = "dataUpdater_dbUninit";

    private static final String TAG = "DataUpdater";

    private static AlertDialog.Builder progressBarBuilder;
    private static AlertDialog progressDialog;
    private static ProgressBar progress;


    // Use GET request to query City of Surrey website
    // and compare to SharedPrefs saved dates.
    // If any dates differ, set SharedPrefs to
    public static void checkForAvailableUpdates(Context callerContext) {
        resetIgnoreBoolean(callerContext);
        Hours timeSinceLastUpdated = numHoursSinceUpdate(callerContext);
        if (timeSinceLastUpdated.isGreaterThan(Hours.hours(20))) {
            // Do this in an async way, so Android lets us continue!
            RequestQueue queue = Volley.newRequestQueue(callerContext);
            // Check the restaurant json file for updates (i.e. new restaurants):
            StringRequest restaurantRequest = new StringRequest(Request.Method.GET, surreyRestaurantGetURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Use anonymous class method to compare response with the one in sharedPrefs
                    JsonObject obj = new Gson().fromJson(response, JsonObject.class).getAsJsonObject();
                    JsonObject result = obj.getAsJsonObject("result");
                    JsonArray resources = result.getAsJsonArray(resourcesString);
                    JsonObject firstResult = resources.get(0).getAsJsonObject();
                    SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0); // 0 means private mode
                    String restaurantLastUpdated = prefs.getString(restaurantCheckKey, "");
                    String newLastUpdated = firstResult.get(modifiedString).getAsString();
                    // Ensure we use HTTPS as Android disallows cleartext traffic:
                    String newRestaurantURL = firstResult.get(restaurantCsvUrlString).getAsString().replace("http://", "https://");
                    if (restaurantLastUpdated.equals(newLastUpdated)) {
                        // DO nothing
                    } else {
                        // Update sharedPrefs bool to say that an update is available:
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putBoolean(updateAvailableRestaurantBool, true);
                        edit.putString(restaurantCsvUrlKey, newRestaurantURL);
                        edit.apply();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // If an error is caught, cannot determine if an update is available or not.
                    SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0); // 0 means private mode
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(updateAvailableRestaurantBool, false);
                    // Wipe out the old URL:
                    edit.putString(restaurantCsvUrlKey, null);
                    edit.apply();
                }
            });



            // Check the inspection json file for updates (i.e. new inspection reports):
            StringRequest inspectionRequest = new StringRequest(Request.Method.GET, surreyInspectionsGetURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Use anonymous class method to compare response with the one in sharedPrefs
                    JsonObject obj = new Gson().fromJson(response, JsonObject.class).getAsJsonObject();
                    JsonObject result = obj.getAsJsonObject("result");
                    JsonArray resources = result.getAsJsonArray(resourcesString);
                    JsonObject firstResult = resources.get(0).getAsJsonObject();
                    SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0); // 0 means private mode
                    String inspectionLastUpdated = prefs.getString(inspectionCheckKey, "");
                    String newLastUpdated = firstResult.get(modifiedString).getAsString();
                    // Ensure we use HTTPS as Android disallows cleartext traffic
                    String newInspectionURL = firstResult.get(inspectionCsvUrlString).getAsString().replace("http://", "https://");
                    if (inspectionLastUpdated.equals(newLastUpdated)) {
                        // DO nothing
                    } else {
                        // Update sharedPrefs bool to say that an update is available:
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putBoolean(updateAvailableInspectionBool, true);
                        edit.putString(inspectionCsvUrlKey, newInspectionURL);
                        edit.apply();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // If an error is caught, cannot determine if an update is available or not.
                    SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(updateAvailableInspectionBool, false);
                    // Wipe out old URL
                    edit.putString(inspectionCsvUrlKey, null);
                    edit.apply();
                }
            });

            // Add these to the async RequestQueue to be executed in another thread:
            queue.add(restaurantRequest);
            queue.add(inspectionRequest);
            // Save now as the last checked time:
            saveCurrentUpdateTime(callerContext);
        }
    }

    // Helper function for determining the number of hours between last check for updates:
    private static Hours numHoursSinceUpdate(Context callerContext) {
        Hours returnHours;
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
        String lastCheckedString = prefs.getString(lastCheckDateString, null);
        if (lastCheckedString == null) {
            // If there is no stored value, assume we've never checked.
            returnHours = Hours.hours(24);
        } else {
            // Something is stored here.
            Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
            // Deserialize last DateTime with Gson:
            DateTime lastUpdatedDate = gson.fromJson(lastCheckedString, DateTime.class);
            // Also get current time:
            DateTime now = DateTime.now();
            // Find the difference:
            returnHours = Hours.hoursBetween(lastUpdatedDate, now);
        }

        return returnHours;
    }

    // Helper function: After checking for update, set stored DateTime to now.
    private static void saveCurrentUpdateTime(Context callerContext) {
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        DateTime now = DateTime.now();
        String serializedNow = gson.toJson(now);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(lastCheckDateString, serializedNow);
        edit.apply();
    }

    private static void resetIgnoreBoolean(Context callerContext) {
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0); // 0 means private mode
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(updateIgnoredString, false);
        edit.apply();
    }


    // notifyIfUpdateAvailable():
    // Creates an AlertDialog asking if the user wants to update,
    // if updates are available.
    public static void notifyIfUpdateAvailable(Context callerContext) {
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
        boolean ignore = prefs.getBoolean(updateIgnoredString, false);
        if (!ignore) {
            boolean restaurantsNeedUpdate = prefs.getBoolean(updateAvailableRestaurantBool, false);
            boolean inspectionsNeedUpdate = prefs.getBoolean(updateAvailableInspectionBool, false);

            if (restaurantsNeedUpdate || inspectionsNeedUpdate) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(callerContext);
                alertBuilder.setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadFiles(callerContext);
                    }
                });

                alertBuilder.setNegativeButton("Ignore Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set this as "update ignored" so we only call this on the first time:
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putBoolean(updateIgnoredString, true);
                        edit.apply();
                    }
                });
                // TODO: replace these UI strings with resources from strings.xml!
                alertBuilder.setMessage("New content available for restaurant resources - would you like to update now?");
                // Show this alertDialog:
                alertBuilder.create().show();
            }
        }
    }

    // Helper function: downloadFiles()
    // Uses DownloadManager to download the newly-updated files.
    // Saves to local, internal (read: private to this application) directory.
    // Updates sharedPreferences with the filenames of these new files.
    private static void downloadFiles(Context callerContext) {

        PRDownloader.initialize(callerContext);
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
        boolean restaurantsNeedUpdate = prefs.getBoolean(updateAvailableRestaurantBool, false);
        boolean inspectionsNeedUpdate = prefs.getBoolean(updateAvailableInspectionBool, false);

        Integer restaurantDownloadID = null;
        Integer inspectionDownloadID = null;

        if (restaurantsNeedUpdate || inspectionsNeedUpdate) {
            progressDialog = getProgressDialog(callerContext).create();
            progressDialog.show();
        }
        SharedPreferences.Editor edit = prefs.edit();

        if (restaurantsNeedUpdate) {
            String restaurantURL = prefs.getString(restaurantCsvUrlKey, null);
            if (restaurantURL != null) {
                String downloadFilename = "restaurants_" + DateTime.now().toString()
                        .replace(" ", "_")
                        .replace(":", "_")
                        .replace("-", "_")
                        + ".csv";
                File downloadedRestaurant = new File(callerContext.getExternalFilesDirs(null)[0], downloadFilename);
                restaurantDownloadID = PRDownloader
                        .download(restaurantURL, callerContext.getExternalFilesDirs(null)[0].getAbsolutePath(), downloadFilename).build().start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                Log.d(TAG, "onDownloadComplete: successfully downloaded new restaurant CSV.");
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString(downloadedRestaurantFilenameKey, downloadedRestaurant.getAbsolutePath());
                                edit.putBoolean(updateAvailableRestaurantBool, false);
                                edit.putBoolean(dbUpdateRequiredKey, true);
                                edit.apply();
                                dismissDialog(callerContext);
                            }

                            @Override
                            public void onError(Error error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                edit.putInt(inProgressDownloadRestaurantIDKey, restaurantDownloadID);
            }
        }
        edit.apply();
        if (inspectionsNeedUpdate) {
            String inspectionURL = prefs.getString(inspectionCsvUrlKey, null);
            if (inspectionURL != null) {
                String downloadFilename = "inspections_" + DateTime.now().toString()
                        .replace(" ", "_")
                        .replace(":", "_")
                        .replace("-", "_")
                        + ".csv";
                File downloadedInspections = new File(callerContext.getExternalFilesDirs(null)[0], downloadFilename);
                inspectionDownloadID = PRDownloader.download(inspectionURL, callerContext.getExternalFilesDirs(null)[0].getAbsolutePath(), downloadFilename)
                        .build()
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                Log.d(TAG, "onDownloadComplete: successfully downloaded new inspection CSV.");
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString(downloadedInspectionFilenameKey, downloadedInspections.getAbsolutePath());
                                edit.putBoolean(updateAvailableInspectionBool, false);
                                edit.putBoolean(dbUpdateRequiredKey, true);
                                edit.apply();
                                dismissDialog(callerContext);
                            }

                            @Override
                            public void onError(Error error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                edit.putInt(inProgressDownloadInspectionIDKey, inspectionDownloadID);
            }
        }
        edit.apply();
    }

    // Helper function: getProgressDialog().
    // Much of this was inspired by
    // https://stackoverflow.com/questions/10977150/showing-progress-bar-in-alert-dialog
    private static AlertDialog.Builder getProgressDialog(Context callerContext) {
        if (progressBarBuilder == null) {
            progressBarBuilder = new AlertDialog.Builder(callerContext);
            progressBarBuilder.setTitle("Downloading new data...");
            progress = new ProgressBar(callerContext);
            // Create an inline LinearLayout and add this progressBar to it:
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            progress.setLayoutParams(params);
            progressBarBuilder.setView(progress);
            progressBarBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelDownloads(callerContext);
                    progressDialog.dismiss();
                }
            });
        }
        return progressBarBuilder;
    }

    // Helper function: dismissDialog()
    // Ensures that we only close the dialog when
    // both are finished.
    private static void dismissDialog(Context callerContext) {
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
        boolean restaurantsNeedUpdate = prefs.getBoolean(updateAvailableRestaurantBool, false);
        boolean inspectionsNeedUpdate = prefs.getBoolean(updateAvailableInspectionBool, false);
        if (!restaurantsNeedUpdate && !inspectionsNeedUpdate) {
            if (progressDialog != null) {
                // Set the download ID to invalid int so we don't double-cancel:
                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt(inProgressDownloadInspectionIDKey, -1);
                edit.putInt(inProgressDownloadRestaurantIDKey, -1);
                edit.apply();
                progressDialog.dismiss();
                // Create a dialog telling the user to restart the app for new data.
                AlertDialog.Builder builder = new AlertDialog.Builder(callerContext);
                builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ProcessPhoenix.triggerRebirth(callerContext);
                    }
                });
                builder.setMessage("Data successfully updated. Please restart this application to use new data.");
                builder.create().show();
            }
        } else {
            if (!restaurantsNeedUpdate || !inspectionsNeedUpdate) {
                // Set progress to 50%
                if (progress != null) {
                    progress.setProgress(progress.getMax() / 2);
                }
            }
        }
    }

    // Helper function: cancelDownloads();
    // Cancels downloads in progress with given IDs.
    private static void cancelDownloads(Context callerContext) {
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0);
        SharedPreferences.Editor edit = prefs.edit();
        int restaurantDownloadID = prefs.getInt(inProgressDownloadRestaurantIDKey, -1);
        int inspectionDownloadID = prefs.getInt(inProgressDownloadInspectionIDKey, -1);
        if (restaurantDownloadID != -1) {
            PRDownloader.cancel(restaurantDownloadID);
        }
        if (inspectionDownloadID != -1) {
            PRDownloader.cancel(inspectionDownloadID);
        }

        // Set these to -1:
        edit.putInt(inProgressDownloadInspectionIDKey, -1);
        edit.putInt(inProgressDownloadRestaurantIDKey, -1);
        edit.apply();
    }

    // Helper method: updateDatabase()
    // Only does work when new data is available for the db.
    public static void updateDatabase(Context callerContext) {
        SharedPreferences prefs = callerContext.getSharedPreferences(prefsFile, 0); // 0 means private mode
        boolean dbUpdateRequired = prefs.getBoolean(dbUpdateRequiredKey, false);
        boolean firstRun = prefs.getBoolean(dbUpdateFirstRun, true);
        if (firstRun) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(dbUpdateFirstRun, false);
            edit.apply();
            dbUpdateRequired = true;
        }
        if (dbUpdateRequired) {
            DatabaseHelperFacility dbFacility = new DatabaseHelperFacility(callerContext);
            dbFacility.getWritableDatabase();
            dbFacility.insertData();
            dbFacility.close();

            DatabaseHelperInspection dbInspection = new DatabaseHelperInspection(callerContext);
            dbInspection.getWritableDatabase();
            dbInspection.insertData();
            dbInspection.close();

            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(dbUpdateRequiredKey, false);
            edit.apply();
        }
    }
}