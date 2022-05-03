package ca.cmpt276.magnesium.healthinspectionviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;

import java.util.ArrayList;

import ca.cmpt276.magnesium.restaurantmodel.DatabaseReader;
import ca.cmpt276.magnesium.restaurantmodel.Facility;
import ca.cmpt276.magnesium.restaurantmodel.HazardRating;
import ca.cmpt276.magnesium.restaurantmodel.InspectionReport;
import ca.cmpt276.magnesium.restaurantmodel.Violation;

public class InspectionActivity extends AppCompatActivity {

    private final static String EXTRA_INSPECT_ID = "InspectionActivity_InspectIDExtra";
    private final static String EXTRA_FACILITY_ID = "InspectionActivity_FacilityIDExtra";

    private InspectionReport inspection;
    private BaseAdapter adapter;

    public static Intent makeInspectionIntent(Context context, int inspectionID, int facilityID) {
        Intent intent = new Intent(context, InspectionActivity.class);
        intent.putExtra(EXTRA_INSPECT_ID, inspectionID);
        intent.putExtra(EXTRA_FACILITY_ID, facilityID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        setupToolbar();

        DatabaseReader reader = new DatabaseReader(getApplicationContext());

        ArrayList<Facility> facilities = reader.getAllFacilities();
        int facilityIndex = getIntent().getIntExtra(EXTRA_FACILITY_ID, 0);
        Facility currentFacility = facilities.get(facilityIndex);

        String trackingNo = currentFacility.getTrackingNumber();
        ArrayList<InspectionReport> reports = reader.getAllAssociatedInspections(trackingNo);
        int inspectionIndex = getIntent().getIntExtra(EXTRA_INSPECT_ID, 0);
        inspection = reports.get(inspectionIndex);

        TextView name = findViewById(R.id.inspection_restaurant_name);
        name.setText(currentFacility.getName());

        TextView type = findViewById(R.id.inspection_type);
        type.setText(inspection.getInspectionType().toString());

        TextView hazardLevel = findViewById(R.id.inspection_hazard_lv);
        hazardLevel.setText(inspection.getHazardRating().toString());

        ImageView hazardIcon = findViewById(R.id.inspection_hazard_color);
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

        TextView numCrit = findViewById(R.id.inspection_crit);
        numCrit.setText(Integer.valueOf(inspection.getNumCritical()).toString());

        TextView numNonCrit = findViewById(R.id.inspection_noncrit);
        numNonCrit.setText(Integer.valueOf(inspection.getNumNonCritical()).toString());

        LocalDate inspectionDate = inspection.getInspectionDate();
        String dateString = inspectionDate.toString("MMMM d, yyyy");
        TextView date = findViewById(R.id.inspection_date);
        date.setText(dateString);

        TextView empty = findViewById(R.id.inspection_violation_empty);
        ListView list = findViewById(R.id.inspection_violation_listView);
        if(inspection.getViolations().isEmpty()){
            empty.setVisibility(View.VISIBLE);
            list.setVisibility(View.INVISIBLE);
        }else {
            populateListView();
            empty.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.inspection_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateListView() {
        ListView lv = findViewById(R.id.inspection_violation_listView);
        ArrayList<Violation> violationList = inspection.getViolations();
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return violationList.size();
            }

            @Override
            public Violation getItem(int position) {
                return violationList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.violation_list_view, parent, false);
                }
                Violation currentViolation = violationList.get(position);

                TextView criticality = (TextView) convertView.findViewById(
                                                R.id.violationArrayList_issue_lv);
                criticality.setText(currentViolation.getCriticality());

                ImageView criticalityIcon = convertView.findViewById(R.id.violationArrayList_vio_type);
                if (currentViolation.getCriticality().toLowerCase().equals("critical")) {
                    criticalityIcon.setImageDrawable(getDrawable(R.drawable.critical_violation));
                } else if (currentViolation.getCriticality().toLowerCase().equals("non-critical")) {
                    criticalityIcon.setImageDrawable(getDrawable(R.drawable.non_critical_violation));
                }

                TextView description = (TextView) convertView.findViewById(R.id.violationArrayList_issue_description);
                description.setText(currentViolation.toString());

                TextView nature = (TextView) convertView.findViewById(
                                                R.id.violationArrayList_vio_lv);
                String vioNature = currentViolation.getViolationNature();
                ImageView natureIcon = convertView.findViewById(R.id.violationArrayList_nature_icon);
                natureIcon.setImageDrawable(getDrawable(getNatureIcon(vioNature)));
                nature.setText(vioNature);

                // Set this view's onClick activity:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Show the full violation text via toast.
                        Toast toast = Toast.makeText(
                                getApplicationContext(),
                                currentViolation.getViolDescription(),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });


                return convertView;
            }
        };

        lv.setAdapter(adapter);
    }

    private int getNatureIcon(String vioNature) {
        if (vioNature == "Regulatory") {
            return R.drawable.regulatory;
        } else if (vioNature == "Food") {
            return R.drawable.food;
        } else if (vioNature == "Sanitization") {
            return R.drawable.sanitization;
        } else if (vioNature == "Pests") {
            return R.drawable.pests;
        } else if (vioNature == "Facility") {
            return R.drawable.facility;
        } else if (vioNature == "Employee") {
            return R.drawable.employee;
        } else if (vioNature == "Operator") {
            return R.drawable.operator;
        }
        return R.drawable.regulatory;
    }


}
