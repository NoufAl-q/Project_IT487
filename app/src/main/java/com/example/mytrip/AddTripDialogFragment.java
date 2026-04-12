package com.example.mytrip;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * AddTripDialogFragment – DialogFragment for inserting a new trip into SQLite.
 *
 * Rubric: DialogFragment requirement; INSERT operation.
 * Date field supports both calendar picker (DatePickerDialog) and manual text entry.
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
        TextInputLayout tilDate = view.findViewById(R.id.tilDate);

        // Calendar icon → open DatePickerDialog; field stays editable for manual input
        tilDate.setEndIconOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            // If user already typed a date, try to pre-select it in the picker
            String existing = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            if (existing.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = existing.split("-");
                year  = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1; // Calendar months are 0-indexed
                day   = Integer.parseInt(parts[2]);
            }

            DatePickerDialog picker = new DatePickerDialog(requireContext(),
                    (datePicker, y, m, d) -> {
                        // Format as YYYY-MM-DD and populate the field
                        String formatted = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                        etDate.setText(formatted);
                    }, year, month, day);
            picker.show();
        });

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_trip)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String dest = etDestination.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (dest.isEmpty() || date.isEmpty()) {
                        Toast.makeText(getContext(), R.string.fill_all_fields,
                                Toast.LENGTH_SHORT).show();
                    } else {
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
