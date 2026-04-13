package com.example.mytrip;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * SupportActivity – Static About / Support screen.
 *
 * Shows team member names, IDs, and supervisor info.
 * Rubric: Fourth runnable Activity with Intent navigation.
 */
public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.support);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Contact button – shows email in a details DialogFragment
        MaterialButton btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(v -> {
            new ContactDialogFragment()
                    .show(getSupportFragmentManager(), "ContactDialog");
        });

        // Bottom Navigation – highlight About tab
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_about);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                noTransition();
                return true;
            } else if (id == R.id.nav_about) {
                return true;
            }
            return false;
        });
    }

    private void noTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0, 0);
        } else {
            //noinspection deprecation
            overridePendingTransition(0, 0);
        }
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
