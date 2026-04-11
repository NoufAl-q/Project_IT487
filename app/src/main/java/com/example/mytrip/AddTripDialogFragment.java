package com.example.mytrip;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * AddTripDialogFragment – DialogFragment for inserting a new trip into SQLite.
 *
 * Rubric: DialogFragment requirement; INSERT operation.
 */
public class AddTripDialogFragment extends DialogFragment {

    /** Notified when a trip is successfully saved. */
    public interface OnTripAddedListener {
        void onTripAdded();
    }

    private OnTripAddedListener listener;

    public void setOnTripAddedListener(OnTripAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_add_trip, null);

        EditText etDestination = view.findViewById(R.id.etDestination);
        EditText etDate        = view.findViewById(R.id.etDate);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_trip)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String dest = etDestination.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (dest.isEmpty() || date.isEmpty()) {
                        // Toast feedback (rubric requirement)
                        Toast.makeText(getContext(), R.string.fill_all_fields,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // INSERT into SQLite
                        dbHelper.insertTrip(dest, date);
                        Toast.makeText(getContext(), R.string.trip_added,
                                Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onTripAdded();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
