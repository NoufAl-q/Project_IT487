package com.example.mytrip;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * StatisticsActivity – Displays app-wide trip/item statistics.
 *
 * Rubric coverage:
 *   - Third runnable Activity (Intent navigation from MainActivity)
 *   - TableLayout for structured display (see activity_statistics.xml)
 *   - Reads: Total Trips, Total Items, Packed Items, Remaining Items, Next Trip, Completion Rate
 */
public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.statistics);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Read stats from SQLite
        int totalTrips    = dbHelper.getTotalTrips();
        int totalItems    = dbHelper.getTotalItems();
        int preparedItems = dbHelper.getPreparedItems();
        int remaining     = totalItems - preparedItems;
        String nextTrip   = dbHelper.getNextTrip();

        // Completion percentage
        int pct = (totalItems > 0) ? (preparedItems * 100 / totalItems) : 0;

        // Populate TableLayout cells
        ((TextView) findViewById(R.id.tvTotalTrips)).setText(String.valueOf(totalTrips));
        ((TextView) findViewById(R.id.tvTotalItems)).setText(String.valueOf(totalItems));
        ((TextView) findViewById(R.id.tvPreparedItems)).setText(String.valueOf(preparedItems));
        ((TextView) findViewById(R.id.tvRemainingItems)).setText(String.valueOf(remaining));
        ((TextView) findViewById(R.id.tvNextTrip)).setText(nextTrip);
        ((TextView) findViewById(R.id.tvCompletionRate)).setText(pct + "%");

        // Bottom Navigation – highlight Stats tab
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_stats) {
                return true;
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, SupportActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
