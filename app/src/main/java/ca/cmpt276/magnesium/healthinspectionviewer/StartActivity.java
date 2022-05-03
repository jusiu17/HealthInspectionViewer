package ca.cmpt276.magnesium.healthinspectionviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ca.cmpt276.magnesium.restaurantmodel.DataUpdater;
import ca.cmpt276.magnesium.restaurantmodel.DatabaseHelperFacility;
import ca.cmpt276.magnesium.restaurantmodel.DatabaseHelperInspection;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        DataUpdater.updateDatabase(this);

        // Begin async task to check for updates from the City of Surrey API:
        // Will not download any data, but check the "last modified" dates
        // and store a boolean in SharedPrefs if something has changed.
        DataUpdater.checkForAvailableUpdates(getApplicationContext());

        autoSwitchRestaurantList();
    }

    private void autoSwitchRestaurantList() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = MapScreen.makeMapScreenIntent(StartActivity.this);
                startActivity(intent);
                finish();
            }
        },2000);
    }

}
