package com.example.jake1.designproject;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.jake1.designproject.services.AsyncResponse;
import com.example.jake1.designproject.services.UiService;
import com.example.jake1.designproject.services.WebServiceCaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private PinchZoomPan pinchZoomPan;
    private EditText etFrom;
    static EditText etTo;
    private TextView tvTo;
    private Button btnFloor1;
    private Button btnFloor2;
    private Button btnFloor3;
    private Button btnNavigate;
    private Button btnStairs;
    private Button btnBoth;
    private Button btnElevator;
    private Button btnStartNav;
    private ImageButton imBtnBackArrow;
    private ImageView ivUTD;
    private ImageView ivUpDownArrow;
    private PopupMenu pumFrom;
    private PopupMenu pumTo;
    static boolean takeStairs;
    static boolean takeElevator;

    private double[] coordinates;

    //URLs to services
    private String serverURL = "https://ecsclark18.utdallas.edu/";
    private String cmxApiUrl = "http://cmxproxy01.utdallas.edu/api";
    private String arcGisApiUrl = "10.0.2.2";
    private String arcGisApiRoute = "index.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        setSupportActionBar(toolbar);
        pinchZoomPan.loadImageOnCanvas(0);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        loadPrefs();
        pumFrom.getMenuInflater().inflate( R.menu.start_location_menu, pumFrom.getMenu());
        pumTo.getMenuInflater().inflate( R.menu.destination_menu, pumTo.getMenu());

        if (getIntent().hasExtra("coordinateArray")) {
            displayPath(getIntent().getExtras().getDoubleArray("coordinateArray"));
        }

    }

    private void init() {
        setVariables();
        setListeners();
    }

    private void setVariables() {

        toolbar = findViewById(R.id.toolbar);
        pinchZoomPan = findViewById(R.id.pinchZoomPan);
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        tvTo = findViewById(R.id.tvTo);
        btnFloor1 = findViewById(R.id.btnFloor1);
        btnFloor2 = findViewById(R.id.btnFloor2);
        btnFloor3 = findViewById(R.id.btnFloor3);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnStairs = findViewById(R.id.btnStairs);
        btnBoth = findViewById(R.id.btnBoth);
        btnElevator = findViewById(R.id.btnElevator);
        btnStartNav = findViewById(R.id.btnStartNav);
        ivUTD = findViewById(R.id.ivUTD);
        ivUpDownArrow = findViewById(R.id.ivUpDownArrow);
        imBtnBackArrow = findViewById(R.id.imBtnBackArrow);
        pumFrom = new PopupMenu(this, etFrom);
        pumTo = new PopupMenu(this, etTo);


    }

    private void setListeners() {

        etFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pumFrom.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        etFrom.setText(menuItem.toString());
                        return true;

                    }
                });
                pumFrom.show();
            }
        });

        etTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pumTo.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        etTo.setText(menuItem.toString());
                        return true;

                    }
                });
                pumTo.show();
            }
        });

        btnStairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectStairs();
                savePrefs(1);
            }
        });

        btnBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBoth();
                savePrefs(0);
            }
        });

        btnElevator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectElevator();
                savePrefs(2);
            }
        });

        btnFloor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFloor1();
                pinchZoomPan.setFloorPath(0);
                pinchZoomPan.loadImageOnCanvas(0);
            }
        });

        btnFloor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFloor2();
                pinchZoomPan.setFloorPath(1);
                pinchZoomPan.loadImageOnCanvas(1);
            }
        });

        btnFloor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFloor3();
                pinchZoomPan.setFloorPath(2);
                pinchZoomPan.loadImageOnCanvas(2);
            }
        });

        imBtnBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnNavigate.setVisibility(View.VISIBLE);
                btnStairs.setVisibility(View.VISIBLE);
                btnBoth.setVisibility(View.VISIBLE);
                btnElevator.setVisibility(View.VISIBLE);
                etFrom.setVisibility(View.VISIBLE);
                etTo.setVisibility(View.VISIBLE);
                tvTo.setVisibility(View.VISIBLE);
                ivUpDownArrow.setVisibility(View.VISIBLE);

                imBtnBackArrow.setVisibility(View.GONE);
                ivUTD.setVisibility(View.VISIBLE);
                btnStartNav.setVisibility(View.GONE);

                pinchZoomPan.popCoordinates(null);

            }
        });



        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AsyncResponse asyncResponse = new AsyncResponse() {
                    @Override
                    public void returnResponse(String result) {
                        UiService.printString(result);
                        try {
                            double[] coordinates = parseRoutes(result, "route");
                            saveCoordinates(coordinates);
                            displayPath(coordinates);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                WebServiceCaller.request(MainActivity.this, serverURL, arcGisApiRoute, etFrom.getText().toString(), etTo.getText().toString(), takeStairs, takeElevator, asyncResponse);

            }
        });

        btnStartNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double[] coordinates = null;

                if (getCoordinates() != null) {
                    coordinates = getCoordinates();
                }


                Intent startNavigationActivity = new Intent(getApplicationContext(), NavigationActivity.class);
                startNavigationActivity.putExtra("finalDestination", etTo.getText().toString());
                startNavigationActivity.putExtra("navigationArray", coordinates);
                startActivity(startNavigationActivity);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fast_nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent startQuickNavActivity = new Intent(getApplicationContext(), QuickNavMenuActivity.class);
        startActivity(startQuickNavActivity);

        return true;

    }

    private void selectStairs() {

        btnStairs.setTextColor(getResources().getColor(R.color.colorAccent));
        btnBoth.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnElevator.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnStairs.setBackgroundResource(R.drawable.btn_pressed_background);
        btnBoth.setBackgroundResource(R.drawable.btn_background);
        btnElevator.setBackgroundResource(R.drawable.btn_background);
        btnStairs.setEnabled(false);
        btnBoth.setEnabled(true);
        btnElevator.setEnabled(true);
        takeStairs = true;
        takeElevator = false;

    }

    private void selectBoth() {

        btnStairs.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnBoth.setTextColor(getResources().getColor(R.color.colorAccent));
        btnElevator.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnStairs.setBackgroundResource(R.drawable.btn_background);
        btnBoth.setBackgroundResource(R.drawable.btn_pressed_background);
        btnElevator.setBackgroundResource(R.drawable.btn_background);
        btnStairs.setEnabled(true);
        btnBoth.setEnabled(false);
        btnElevator.setEnabled(true);
        takeStairs = true;
        takeElevator = true;

    }

    private void selectElevator() {

        btnStairs.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnBoth.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnElevator.setTextColor(getResources().getColor(R.color.colorAccent));
        btnStairs.setBackgroundResource(R.drawable.btn_background);
        btnBoth.setBackgroundResource(R.drawable.btn_background);
        btnElevator.setBackgroundResource(R.drawable.btn_pressed_background);
        btnStairs.setEnabled(true);
        btnBoth.setEnabled(true);
        btnElevator.setEnabled(false);
        takeStairs = false;
        takeElevator = true;

    }

    private void selectFloor1() {

        btnFloor1.setTextColor(getResources().getColor(R.color.colorAccent));
        btnFloor2.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFloor3.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFloor1.setBackgroundResource(R.drawable.btn_pressed_background);
        btnFloor2.setBackgroundResource(R.drawable.btn_background);
        btnFloor3.setBackgroundResource(R.drawable.btn_background);
        btnFloor1.setEnabled(false);
        btnFloor2.setEnabled(true);
        btnFloor3.setEnabled(true);

    }

    private void selectFloor2() {

        btnFloor1.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFloor2.setTextColor(getResources().getColor(R.color.colorAccent));
        btnFloor3.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFloor1.setBackgroundResource(R.drawable.btn_background);
        btnFloor2.setBackgroundResource(R.drawable.btn_pressed_background);
        btnFloor3.setBackgroundResource(R.drawable.btn_background);
        btnFloor1.setEnabled(true);
        btnFloor2.setEnabled(false);
        btnFloor3.setEnabled(true);

    }

    private void selectFloor3() {

        btnFloor1.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFloor2.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFloor3.setTextColor(getResources().getColor(R.color.colorAccent));
        btnFloor1.setBackgroundResource(R.drawable.btn_background);
        btnFloor2.setBackgroundResource(R.drawable.btn_background);
        btnFloor3.setBackgroundResource(R.drawable.btn_pressed_background);
        btnFloor1.setEnabled(true);
        btnFloor2.setEnabled(true);
        btnFloor3.setEnabled(false);

    }

    private void savePrefs(int value) {

        SharedPreferences sp = getSharedPreferences("PATHPREFS", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("pathPrefs", value);
        edit.apply();

    }

    private void loadPrefs() {

        SharedPreferences sp = getSharedPreferences("PATHPREFS", MODE_PRIVATE);
        int prefs = sp.getInt("pathPrefs", 0);

        if (prefs == 1) {
            selectStairs();
        }
        else if (prefs == 2) {
            selectElevator();
        }
        else {
            selectBoth();
        }

    }

    public void displayPath(double[] coordinates) {

        btnNavigate.setVisibility(View.GONE);
        btnStairs.setVisibility(View.GONE);
        btnBoth.setVisibility(View.GONE);
        btnElevator.setVisibility(View.GONE);
        etFrom.setVisibility(View.GONE);
        etTo.setVisibility(View.GONE);
        tvTo.setVisibility(View.GONE);
        ivUpDownArrow.setVisibility(View.GONE);

        ivUTD.setVisibility(View.GONE);
        imBtnBackArrow.setVisibility(View.VISIBLE);
        btnStartNav.setVisibility(View.VISIBLE);

        pinchZoomPan.popCoordinates(coordinates);

    }

    public static double[] parseRoutes(String jsonString, String key) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        JSONArray data = json.getJSONArray(key);
        double[] values = new double[data.length()];
        for (int i=0; i< data.length(); i++) {
            values[i] = (double) data.getDouble(i);
        }
        return values;
    }

    public void saveCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double[] getCoordinates() {
        return this.coordinates;
    }

}
