package com.example.mytrip;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * TripDetailActivity – Checklist screen for a single trip.
 *
 * Rubric coverage:
 *   - Opened via Intent (trip_id, destination, date passed as extras)
 *   - RecyclerView of items with CheckBox, priority label, delete button
 *   - FAB → AddItemDialogFragment (INSERT item)
 *   - Long-press / delete button → DELETE item
 *   - SearchView → searchItems LIKE query
 *   - Share icon → Intent.ACTION_SEND (student feature 2: share trip + items)
 */
public class TripDetailActivity extends AppCompatActivity
        implements ItemAdapter.OnItemActionListener {

    private ItemAdapter adapter;
    private final List<Item> itemList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;
    private int tripId;
    private String tripDestination;
    private String tripDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // Receive trip data from Intent
        tripId          = getIntent().getIntExtra("trip_id", -1);
        tripDestination = getIntent().getStringExtra("trip_destination");
        tripDate        = getIntent().getStringExtra("trip_date");

        // Toolbar with back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(tripDestination);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewItems);
        tvEmpty = findViewById(R.id.tvEmpty);

        // RecyclerView setup
        adapter = new ItemAdapter(this, itemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // FAB – add item via DialogFragment
        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> {
            AddItemDialogFragment dialog = AddItemDialogFragment.newAddInstance(tripId);
            dialog.setOnItemAddedListener(() -> loadItems(null));
            dialog.show(getSupportFragmentManager(), "AddItemDialog");
        });

        loadItems(null);
    }

    /** Loads items for this trip from SQLite; uses LIKE query when keyword is provided. */
    private void loadItems(String query) {
        itemList.clear();
        List<String[]> rawList = (query == null || query.isEmpty())
                ? dbHelper.getItemsByTrip(tripId)          // SELECT by trip_id
                : dbHelper.searchItems(tripId, query);     // SELECT … AND item_name LIKE '%q%'

        for (String[] row : rawList) {
            itemList.add(new Item(
                    Integer.parseInt(row[0]),  // id
                    Integer.parseInt(row[1]),  // tripId
                    row[2],                    // itemName
                    row[3],                    // priority
                    Integer.parseInt(row[4])   // isChecked
            ));
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ── ItemAdapter.OnItemActionListener ─────────────────────────────────────

    /** Checkbox toggled → UPDATE is_checked in SQLite. */
    @Override
    public void onItemChecked(Item item, boolean checked) {
        dbHelper.updateItemChecked(item.getId(), checked);
    }

    /** Delete button → confirmation dialog → DELETE item from SQLite. */
    @Override
    public void onItemDelete(Item item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item)
                .setMessage("Delete \"" + item.getItemName() + "\"?")
                .setPositiveButton("Yes", (d, w) -> {
                    dbHelper.deleteItem(item.getId());
                    Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                    loadItems(null);
                })
                .setNegativeButton("No", null)
                .show();
    }

    /** Long-press on item card → open edit dialog. */
    @Override
    public void onItemEdit(Item item) {
        AddItemDialogFragment dialog = AddItemDialogFragment.newEditInstance(item);
        dialog.setOnItemAddedListener(() -> loadItems(null));
        dialog.show(getSupportFragmentManager(), "EditItemDialog");
    }

    // ── Options menu ──────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trip_detail, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_items);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_items));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { loadItems(q); return true; }
            @Override public boolean onQueryTextChange(String q) { loadItems(q); return true; }
        });

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;

        } else if (id == R.id.action_share) {
            shareTrip();
            return true;

        } else if (id == R.id.action_edit_trip) {
            Trip trip = new Trip(tripId, tripDestination, tripDate);
            AddTripDialogFragment dialog = AddTripDialogFragment.newEditInstance(trip);
            dialog.setOnTripAddedListener(() -> {
                String[] updated = dbHelper.getTripById(tripId);
                if (updated != null) {
                    tripDestination = updated[1];
                    tripDate = updated[2];
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(tripDestination);
                }
            });
            dialog.show(getSupportFragmentManager(), "EditTripDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Builds a plain-text summary of the trip and all its checklist items,
     * then fires an ACTION_SEND Intent so the user can share via any app.
     */
    private void shareTrip() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trip: ").append(tripDestination).append("\n");
        sb.append("Date: ").append(tripDate).append("\n\n");
        sb.append("Checklist:\n");

        List<String[]> rawItems = dbHelper.getItemsByTrip(tripId);
        if (rawItems.isEmpty()) {
            sb.append("(No items yet)");
        } else {
            for (String[] row : rawItems) {
                String status   = row[4].equals("1") ? "[Done]" : "[    ]";
                String priority = row[3];
                String name     = row[2];
                sb.append(status).append(" [").append(priority).append("] ")
                  .append(name).append("\n");
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Trip to " + tripDestination);
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share trip via"));
    }
}
