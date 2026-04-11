package com.example.mytrip;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity – Home screen.
 *
 * Rubric coverage:
 *   - RecyclerView + CardView trip list
 *   - FloatingActionButton → AddTripDialogFragment (INSERT)
 *   - Long-press trip → confirmation dialog → deleteTrip (DELETE)
 *   - SearchView → searchTrips LIKE query (SEARCH)
 *   - Dark Mode toggle (student feature 1)
 *   - Intent navigation to TripDetailActivity, StatisticsActivity, SupportActivity
 */
public class MainActivity extends AppCompatActivity
        implements TripAdapter.OnTripClickListener {

    private static final String PREFS = "MyTripPrefs";
    private static final String KEY_DARK = "dark_mode";

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private final List<Trip> tripList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Restore dark-mode preference before inflating UI
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewTrips);
        tvEmpty = findViewById(R.id.tvEmpty);

        // RecyclerView setup
        adapter = new TripAdapter(this, tripList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // FAB – show AddTripDialogFragment (DialogFragment requirement)
        FloatingActionButton fab = findViewById(R.id.fabAddTrip);
        fab.setOnClickListener(v -> {
            AddTripDialogFragment dialog = new AddTripDialogFragment();
            dialog.setOnTripAddedListener(() -> loadTrips(null));
            dialog.show(getSupportFragmentManager(), "AddTripDialog");
        });

        loadTrips(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips(null);
    }

    /** Loads trips from SQLite; passes keyword to LIKE-based searchTrips when non-null. */
    private void loadTrips(String query) {
        tripList.clear();
        List<String[]> rawList = (query == null || query.isEmpty())
                ? dbHelper.getAllTrips()          // SELECT *
                : dbHelper.searchTrips(query);    // SELECT * WHERE destination LIKE '%keyword%'

        for (String[] row : rawList) {
            tripList.add(new Trip(Integer.parseInt(row[0]), row[1], row[2]));
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(tripList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ── TripAdapter.OnTripClickListener ──────────────────────────────────────

    /** Single tap → open TripDetailActivity via Intent (passing trip data). */
    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip_id", trip.getId());
        intent.putExtra("trip_destination", trip.getDestination());
        intent.putExtra("trip_date", trip.getDate());
        startActivity(intent);
    }

    /** Long-press → confirmation dialog → DELETE trip (and its items) from SQLite. */
    @Override
    public void onTripLongClick(Trip trip) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_trip)
                .setMessage(R.string.delete_trip_confirm)
                .setPositiveButton("Yes", (d, w) -> {
                    dbHelper.deleteTrip(trip.getId());
                    Toast.makeText(this, R.string.trip_deleted, Toast.LENGTH_SHORT).show();
                    loadTrips(null);
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ── Options menu ──────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Wire up SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_trips));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { loadTrips(q); return true; }
            @Override public boolean onQueryTextChange(String q) { loadTrips(q); return true; }
        });

        // Update dark-mode label to reflect current state
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        menu.findItem(R.id.action_dark_mode)
                .setTitle(isDark ? R.string.light_mode : R.string.dark_mode);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_statistics) {
            // Navigate to StatisticsActivity (Intent)
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;

        } else if (id == R.id.action_support) {
            // Navigate to SupportActivity (Intent)
            startActivity(new Intent(this, SupportActivity.class));
            return true;

        } else if (id == R.id.action_dark_mode) {
            // Student feature 1: Dark Mode toggle (persisted via SharedPreferences)
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            boolean isDark = prefs.getBoolean(KEY_DARK, false);
            boolean newDark = !isDark;
            prefs.edit().putBoolean(KEY_DARK, newDark).apply();
            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            return true;

        } else if (id == R.id.action_clear_all) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.clear_all)
                    .setMessage(R.string.clear_all_confirm)
                    .setPositiveButton("Yes", (d, w) -> {
                        dbHelper.clearAllData();
                        Toast.makeText(this, R.string.all_cleared, Toast.LENGTH_SHORT).show();
                        loadTrips(null);
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
