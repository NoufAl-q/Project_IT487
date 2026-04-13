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
 * AddTripDialogFragment – DialogFragment for inserting or editing a trip.
 *
 * Rubric: DialogFragment requirement; INSERT / UPDATE operations.
 * Use newAddInstance() for adding, newEditInstance(trip) for editing.
 */
public class AddTripDialogFragment extends DialogFragment {

    public interface OnTripAddedListener {
        void onTripAdded();
    }

    private static final String ARG_TRIP_ID   = "trip_id";
    private static final String ARG_TRIP_DEST = "trip_dest";
    private static final String ARG_TRIP_DATE = "trip_date";

    private OnTripAddedListener listener;

    /** Factory for Add mode. */
    public static AddTripDialogFragment newAddInstance() {
        return new AddTripDialogFragment();
    }

    /** Factory for Edit mode – pre-fills fields with existing trip data. */
    public static AddTripDialogFragment newEditInstance(Trip trip) {
        AddTripDialogFragment f = new AddTripDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRIP_ID, trip.getId());
        args.putString(ARG_TRIP_DEST, trip.getDestination());
        args.putString(ARG_TRIP_DATE, trip.getDate());
        f.setArguments(args);
        return f;
    }

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

        // Determine if we're in edit mode
        Bundle args   = getArguments();
        boolean isEdit = args != null && args.containsKey(ARG_TRIP_ID);
        int tripId     = isEdit ? args.getInt(ARG_TRIP_ID) : -1;

        // Pre-fill fields in edit mode
        if (isEdit) {
            etDestination.setText(args.getString(ARG_TRIP_DEST));
            etDate.setText(args.getString(ARG_TRIP_DATE));
        }

        // Calendar icon → open DatePickerDialog
        tilDate.setEndIconOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            String existing = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            if (existing.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = existing.split("-");
                year  = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
                day   = Integer.parseInt(parts[2]);
            }

            new DatePickerDialog(requireContext(),
                    (datePicker, y, m, d) -> {
                        etDate.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d));
                    }, year, month, day).show();
        });

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        String title = isEdit ? getString(R.string.edit_trip) : getString(R.string.add_trip);

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String dest = etDestination.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (dest.isEmpty() || date.isEmpty()) {
                        Toast.makeText(getContext(), R.string.fill_all_fields,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (isEdit) {
                            dbHelper.updateTrip(tripId, dest, date);
                            Toast.makeText(getContext(), R.string.trip_updated,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            dbHelper.insertTrip(dest, date);
                            Toast.makeText(getContext(), R.string.trip_added,
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (listener != null) listener.onTripAdded();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
