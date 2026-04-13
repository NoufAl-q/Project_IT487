package com.example.mytrip;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
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

    // Palette colors for past trip cards (cycled)
    private static final int[] CARD_COLORS = {
            0xFF7D5F6B, 0xFF5C4A6E, 0xFF8C7060, 0xFF4A5568,
            0xFF6B4E6B, 0xFF4E6B5C, 0xFF6B5C4E, 0xFF4E5C6B
    };

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private final List<Trip> tripList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;
    private TextView tvStatTrips, tvStatPrepared, tvStatRemaining;
    private TextView tvPastTripsLabel;
    private View hsvPastTrips;
    private LinearLayout llPastTrips;

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
        recyclerView    = findViewById(R.id.recyclerViewTrips);
        tvEmpty         = findViewById(R.id.tvEmpty);
        tvStatTrips     = findViewById(R.id.tvStatTrips);
        tvStatPrepared  = findViewById(R.id.tvStatPrepared);
        tvStatRemaining = findViewById(R.id.tvStatRemaining);
        tvPastTripsLabel = findViewById(R.id.tvPastTripsLabel);
        hsvPastTrips    = findViewById(R.id.hsvPastTrips);
        llPastTrips     = findViewById(R.id.llPastTrips);

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

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, SupportActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
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
                ? dbHelper.getAllTrips()
                : dbHelper.searchTrips(query);

        for (String[] row : rawList) {
            tripList.add(new Trip(Integer.parseInt(row[0]), row[1], row[2]));
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(tripList.isEmpty() ? View.VISIBLE : View.GONE);

        // Update stats card
        int totalItems    = dbHelper.getTotalItems();
        int preparedItems = dbHelper.getPreparedItems();
        tvStatTrips.setText(String.valueOf(tripList.size()));
        tvStatPrepared.setText(String.valueOf(preparedItems));
        tvStatRemaining.setText(String.valueOf(totalItems - preparedItems));

        // Update My Previous Trips section (only shown when not searching)
        if (query == null || query.isEmpty()) {
            loadPastTrips();
        }
    }

    /** Builds the My Trips Preview horizontal cards (up to 5 most recent). */
    private void loadPastTrips() {
        List<String[]> pastTrips = dbHelper.getRecentTrips();
        llPastTrips.removeAllViews();

        if (pastTrips.isEmpty()) {
            tvPastTripsLabel.setVisibility(View.GONE);
            hsvPastTrips.setVisibility(View.GONE);
            return;
        }

        tvPastTripsLabel.setVisibility(View.VISIBLE);
        hsvPastTrips.setVisibility(View.VISIBLE);

        int dp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        for (int i = 0; i < pastTrips.size(); i++) {
            String dest = pastTrips.get(i)[1];
            String date = pastTrips.get(i)[2];
            int bgColor = (int) CARD_COLORS[i % CARD_COLORS.length];

            // Card
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    120 * dp, 110 * dp);
            cardParams.setMarginEnd(10 * dp);
            card.setLayoutParams(cardParams);
            card.setRadius(18 * dp);
            card.setCardElevation(2 * dp);
            card.setCardBackgroundColor(bgColor);
            card.setStrokeWidth(0);

            // Inner layout
            LinearLayout inner = new LinearLayout(this);
            inner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setGravity(Gravity.CENTER);
            inner.setPadding(12 * dp, 12 * dp, 12 * dp, 12 * dp);

            // Icon
            ImageView icon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                    32 * dp, 32 * dp);
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_flight);

            // Destination name
            TextView tvDest = new TextView(this);
            tvDest.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tvDest.setText(dest);
            tvDest.setTextColor(Color.WHITE);
            tvDest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvDest.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDest.setPadding(0, 6 * dp, 0, 0);

            // Date
            TextView tvDate = new TextView(this);
            tvDate.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tvDate.setText(date);
            tvDate.setTextColor(0xCCFFFFFF);
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

            inner.addView(icon);
            inner.addView(tvDest);
            inner.addView(tvDate);
            card.addView(inner);
            llPastTrips.addView(card);
        }
    }

    // ── TripAdapter.OnTripClickListener ──────────────────────────────────────

    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip_id", trip.getId());
        intent.putExtra("trip_destination", trip.getDestination());
        intent.putExtra("trip_date", trip.getDate());
        startActivity(intent);
    }

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

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_trips));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { loadTrips(q); return true; }
            @Override public boolean onQueryTextChange(String q) { loadTrips(q); return true; }
        });

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
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;

        } else if (id == R.id.action_support) {
            startActivity(new Intent(this, SupportActivity.class));
            return true;

        } else if (id == R.id.action_dark_mode) {
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
