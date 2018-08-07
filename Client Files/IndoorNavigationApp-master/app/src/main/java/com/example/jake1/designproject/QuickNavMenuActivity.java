package com.example.jake1.designproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class QuickNavMenuActivity extends AppCompatActivity {

    private double[] coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_nav_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        Button btnBathrooms = findViewById(R.id.btnBathrooms);
        Button btnStormShelters = findViewById(R.id.btnStormShelters);
        Button btnWaterFountains = findViewById(R.id.btnWaterFountains);
        Button btnFood = findViewById(R.id.btnFood);
        Button btnEntertainment = findViewById(R.id.btnEntertainment);
        ImageView ivUTD = findViewById(R.id.ivUTD);
        ImageButton imBtnBackArrow = findViewById(R.id.imBtnBackArrow);

        setSupportActionBar(toolbar);
        ivUTD.setVisibility(View.GONE);
        imBtnBackArrow.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(R.string.tv_menu_title);

        imBtnBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startMainActivity);
            }
        });

        btnBathrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayQuickNavPath();
            }
        });

        btnStormShelters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                displayQuickNavPath();

            }
        });

        btnWaterFountains.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayQuickNavPath();
            }
        });

        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayQuickNavPath();
            }
        });

        btnEntertainment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayQuickNavPath();
            }
        });

    }

    private void displayQuickNavPath() {

        Intent startMainActivityWithPath = new Intent(getApplicationContext(), MainActivity.class);
        startMainActivityWithPath.putExtra("coordinateArray", coordinates);
        startActivity(startMainActivityWithPath);

    }

}
