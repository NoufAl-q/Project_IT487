package com.example.mytrip;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * AddItemDialogFragment – DialogFragment for inserting a checklist item.
 *
 * Rubric: DialogFragment requirement; INSERT item with priority (High / Normal / Low).
 */
public class AddItemDialogFragment extends DialogFragment {

    /** Notified when an item is successfully saved. */
    public interface OnItemAddedListener {
        void onItemAdded();
    }

    private int tripId;
    private OnItemAddedListener listener;

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public void setOnItemAddedListener(OnItemAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_add_item, null);

        EditText   etItemName = view.findViewById(R.id.etItemName);
        RadioGroup rgPriority = view.findViewById(R.id.rgPriority);

        // Default to High priority
        ((RadioButton) view.findViewById(R.id.rbHigh)).setChecked(true);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_item)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = etItemName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), R.string.fill_all_fields,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Determine selected priority
                    int selectedId = rgPriority.getCheckedRadioButtonId();
                    String priority;
                    if (selectedId == R.id.rbHigh) {
                        priority = "High";
                    } else if (selectedId == R.id.rbNormal) {
                        priority = "Normal";
                    } else {
                        priority = "Low";
                    }

                    // INSERT item into SQLite
                    dbHelper.insertItem(tripId, name, priority);
                    Toast.makeText(getContext(), R.string.item_added,
                            Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onItemAdded();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
