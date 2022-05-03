package ca.cmpt276.magnesium.healthinspectionviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.magnesium.restaurantmodel.DatabaseReader;
import ca.cmpt276.magnesium.restaurantmodel.Facility;
import ca.cmpt276.magnesium.restaurantmodel.HazardRating;
import ca.cmpt276.magnesium.restaurantmodel.InspectionReport;

public class RestaurantActivity extends AppCompatActivity {

    private final static String EXTRA_REST_ID = "RestaurantActivity_restaurantID";
    private static final int ACTIVITY_REST_MAP_WINDOW = 200;

    private List<InspectionReport> inspections = new ArrayList<InspectionReport>();
    private BaseAdapter adapter;
    private Facility currentRestaurant;
    private int restaurantID;

    public static Intent makeRestaurantIntent(Context context, int restaurantID){
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(EXTRA_REST_ID, restaurantID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_restaurant);
        setupToolbar();

        DatabaseReader reader = new DatabaseReader(getApplicationContext());
        ArrayList<Facility> facilities = reader.getAllFacilities();
        restaurantID = getIntent().getIntExtra(EXTRA_REST_ID, 0);

        currentRestaurant = facilities.get(restaurantID);
        setupTextFields();

        addTestInspection();

        TextView empty = findViewById(R.id.res_inspection_empty);
        ListView list = findViewById(R.id.res_inspection_listView);
        if(inspections.isEmpty()){
            empty.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
        }else {
            populateListView();
            empty.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }

        setupGPSToMap();

    }

    private void setupGPSToMap() {
        View gps = findViewById(R.id.res_coords_layout);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent().putExtra("restTrackNum", currentRestaurant.getTrackingNumber());
                setResult(ACTIVITY_REST_MAP_WINDOW, intent);
                finish();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.res_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupTextFields() {
        // Setup all text fields and icon based on currentFacility
        TextView address, name, latitude, longitude;
        address = findViewById(R.id.res_restaurant_name);
        address.setText(currentRestaurant.getAddress());

        name = findViewById(R.id.res_restaurant_name);
        name.setText(currentRestaurant.getName());

        latitude = findViewById(R.id.res_restaurant_lat);
        latitude.setText(Double.valueOf(currentRestaurant.getLatitude()).toString());

        longitude = findViewById(R.id.res_restaurant_long);
        longitude.setText(Double.valueOf(currentRestaurant.getLongitude()).toString());

        ImageView icon = findViewById(R.id.res_icon);
        icon.setImageDrawable(getDrawable(currentRestaurant.getIconID()));
    }

    private void populateListView() {
        ListView lv = findViewById(R.id.res_inspection_listView);
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return inspections.size();
            }

            @Override
            public InspectionReport getItem(int position) {
                return inspections.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.inspection_list_view, parent, false);
                }

                InspectionReport inspection = getItem(position);
                ImageView hazardIcon = convertView.findViewById(R.id.inspectionArrayList_inspection_hazard_color);
                HazardRating hazardRating = inspection.getHazardRating();
                switch (hazardRating) {
                    case High: {
                        hazardIcon.setImageResource(R.drawable.high_hazard_level);
                        break;
                    }
                    case Moderate: {
                        hazardIcon.setImageResource(R.drawable.moderate_hazard_level);
                        break;
                    }
                    case Low: {
                        hazardIcon.setImageResource(R.drawable.low_hazard_level);
                        break;
                    }
                }

                ((TextView) convertView.findViewById(R.id.inspectionArrayList_inspection_hazard_lv)).setText(inspection.getHazardRating().toString());
                ((TextView) convertView.findViewById(R.id.inspectionArrayList_inspection_noncrit)).setText(inspection.getNumNonCritical() + "");
                ((TextView) convertView.findViewById(R.id.inspectionArrayList_inspection_crit)).setText(inspection.getNumCritical() + "");
                ((TextView) convertView.findViewById(R.id.inspectionArrayList_inspection_date)).setText(inspection.getInspectionDateString());

                return convertView;
            }
        };
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Intent intent = InspectionActivity.makeInspectionIntent(
                            RestaurantActivity.this,
                                    position,
                                    restaurantID);
                    startActivity(intent);
                });
    }

    private void addTestInspection() {
        DatabaseReader reader = new DatabaseReader(getApplicationContext());
        inspections = reader.getAllAssociatedInspections(currentRestaurant.getTrackingNumber());
    }

}
