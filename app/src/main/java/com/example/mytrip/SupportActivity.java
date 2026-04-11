package com.example.mytrip;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

        // Email button – opens mail client via implicit Intent
        MaterialButton btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@mytrip.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyTrip App – Support Request");
            startActivity(Intent.createChooser(emailIntent, "Send email via"));
        });

        // GitHub button – opens browser via implicit Intent
        MaterialButton btnGitHub = findViewById(R.id.btnGitHub);
        btnGitHub.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/NoufAl-q/MyTrip"));
            startActivity(browserIntent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
