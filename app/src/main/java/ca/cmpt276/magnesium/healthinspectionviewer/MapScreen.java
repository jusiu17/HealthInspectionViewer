package ca.cmpt276.magnesium.healthinspectionviewer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.magnesium.restaurantmodel.ClusterRenderer;
import ca.cmpt276.magnesium.restaurantmodel.ClustorMarker;
import ca.cmpt276.magnesium.restaurantmodel.DatabaseReader;
import ca.cmpt276.magnesium.restaurantmodel.Facility;
import ca.cmpt276.magnesium.restaurantmodel.HazardRating;
import ca.cmpt276.magnesium.restaurantmodel.InspectionReport;

import static ca.cmpt276.magnesium.restaurantmodel.HazardRating.High;
import static ca.cmpt276.magnesium.restaurantmodel.HazardRating.Moderate;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback{

    GoogleMap map;
    Button restListBtn;

    private static final int ACTIVITY_REST_LIST_MAP_BUTTON = 100;
    private static final int ACTIVITY_REST_WINDOW = 200;

    private static final String TAG = "MapScreen";
    private static final float DEFAULT_ZOOM = 12f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_CODE = 1234;

    private List<Facility> listFacility;
    private List<InspectionReport> inspectionList;

    private boolean locationPermissionGranted = false;
    private ClusterManager mClusterManager;
    private ClusterRenderer mClusterRenderer;
    private List<ClustorMarker> mClusterMarkers;
    private List<Marker> mMarkers;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;

    public static Intent makeMapScreenIntent(Context context){
        return new Intent(context, MapScreen.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        getLocationPermission();
        listFacility = new ArrayList<>();
        inspectionList = new ArrayList<>();
        mClusterMarkers = new ArrayList<>();
        mMarkers = new ArrayList<>();
        restListBtn = (Button) findViewById(R.id.restaurantList);
        setupRestListButton();
    }


    private void setupRestListButton() {
        restListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = RestaurantsListActivity.makeRestaurantsListIntent(MapScreen.this);
                startActivityForResult(intent, ACTIVITY_REST_LIST_MAP_BUTTON);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready");
        map = googleMap;

        if(locationPermissionGranted){
            getDeviceLocation();
            map.setMyLocationEnabled(true);
        }

        getInspection();
        addResMarkers();
//        checkInfoWindowClicked();
    }

    private void checkInfoWindowClicked() {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Facility markerFacility = (Facility) marker.getTag();
                int pos = listFacility.indexOf(markerFacility);
                Intent i = RestaurantActivity.makeRestaurantIntent(MapScreen.this, pos);
                startActivityForResult(i, ACTIVITY_REST_WINDOW);
            }
        });

    }

    private void getDeviceLocation(){
        //Getting device Location
        Log.d(TAG, "getDeviceLocation: getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(locationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found Location");
                            currentLocation = (Location) task.getResult();
                            LatLng locationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(locationLatLng, DEFAULT_ZOOM);
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapScreen.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }

    private void addResMarkers(){
        if(map != null){
            //Setting up needed objects
            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClustorMarker>(getApplicationContext(), map);
            }
            if(mClusterRenderer == null){
                mClusterRenderer = new ClusterRenderer(getApplicationContext(), map, mClusterManager);
            }
            mClusterManager.setRenderer(mClusterRenderer);

            map.setInfoWindowAdapter(new CustomInfoAdapter(MapScreen.this));

            for(int i =0; i < listFacility.size(); i++){
                Log.d(TAG, "Inside For loop to add icons" );

                int height = 50;
                int width = 50;

                int iconToDisplay = R.drawable.green_circle;
                HazardRating currentHazardLevel = inspectionList.get(i).getHazardRating();
                Facility currentFacility = listFacility.get(i);
                if(currentHazardLevel == High){
                    iconToDisplay = R.drawable.red_triangle;
                }else if (currentHazardLevel == Moderate){
                    iconToDisplay = R.drawable.yellow_diamond;
                }

                ClustorMarker newClustorMarker = new ClustorMarker(
                        new LatLng(currentFacility.getLatitude(), currentFacility.getLongitude()),
                        currentFacility.getName(),
                        currentHazardLevel.toString(),
                        currentFacility.getAddress(),
                        iconToDisplay
                );

                BitmapDrawable bitmapdraw = (BitmapDrawable)getDrawable(iconToDisplay);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(currentFacility.getLatitude(), currentFacility.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                        .title(currentFacility.getName())
                        .snippet("Address: " + currentFacility.getAddress() + "\nHazard Level: " + currentHazardLevel);

                Marker newMarker = map.addMarker(markerOptions);
                newMarker.setTag(currentFacility);
                mMarkers.add(newMarker);

                Log.d(TAG, "Adding cluster markers to manager and list of markers" );
                mClusterManager.addItem(newClustorMarker);
                mClusterMarkers.add(newClustorMarker);

            }
//            mClusterManager.cluster();
            Log.d(TAG, "ClusterManager.cluster called!!" );

        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        map.setInfoWindowAdapter(new CustomInfoAdapter(MapScreen.this));
        Log.d(TAG, "moveCamera: moving the map camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        checkInfoWindowClicked();
    }

    private void getInspection(){
        addRestaurants();
        DatabaseReader dr = new DatabaseReader(getApplicationContext());
        for(int i =0; i < listFacility.size(); i++){
            List<InspectionReport> facilityInspectionList = dr.getAllAssociatedInspections(listFacility.get(i).getTrackingNumber());
            if(facilityInspectionList.size() == 0){
                inspectionList.add(new InspectionReport(listFacility.get(i).getTrackingNumber(), null,"None",-1,-1,"low", "No Inspection yet"));
                continue;
            }
            InspectionReport latestInspection = facilityInspectionList.get(0);
            for(int j = 1; j < facilityInspectionList.size(); j++){
                if(facilityInspectionList.get(j).getInspectionDate().compareTo(latestInspection.getInspectionDate()) > 0){
                    //Change latestInspection
                    latestInspection = facilityInspectionList.get(j);
                }
            }
            inspectionList.add(latestInspection);
            Log.d(TAG, "Inspection Added" );

        }
    }

    private void addRestaurants() {
        DatabaseReader reader = new DatabaseReader(getApplicationContext());
        listFacility = reader.getAllFacilities();
    }

    private void initializeMap(){
        Log.d(TAG, "initializeMap: Initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);

        mapFragment.getMapAsync(MapScreen.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permission");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
                initializeMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions,LOCATION_PERMISSION_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions,LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: called.");
        locationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_CODE:{
                if(grantResults.length > 0){
                    for(int i =0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionResult: permission failed");
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionResult: permission granted");
                    //Initialize Map
                    initializeMap();

                }
            }
        }
    }

    private void showRestInfo(String trackNum){
        for (Facility f : listFacility) {
            if (trackNum.equals(f.getTrackingNumber())) {
                for (ClustorMarker marker : mClusterMarkers) {
                    if (marker.getTitle().equals(f.getName())) {
                        moveCamera(marker.getPosition(), DEFAULT_ZOOM);
                    }
                }
                for (Marker marker : mMarkers){
                    if (marker.getTitle().equals(f.getName())) {
                        marker.showInfoWindow();
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_REST_LIST_MAP_BUTTON:
                if (data==null)
                    finish();
                else
                    if (data.hasExtra("restTrackNum"))
                        showRestInfo(data.getStringExtra("restTrackNum"));
                break;
            case ACTIVITY_REST_WINDOW:
                if (data != null)
                    if (data.hasExtra("restTrackNum"))
                        showRestInfo(data.getStringExtra("restTrackNum"));
                break;
        }
    }
}
