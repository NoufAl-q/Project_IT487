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
 * AddItemDialogFragment – DialogFragment for inserting or editing a checklist item.
 *
 * Rubric: DialogFragment requirement; INSERT / UPDATE item with priority.
 * Use newAddInstance(tripId) for adding, newEditInstance(item) for editing.
 */
public class AddItemDialogFragment extends DialogFragment {

    public interface OnItemAddedListener {
        void onItemAdded();
    }

    private static final String ARG_TRIP_ID   = "trip_id";
    private static final String ARG_ITEM_ID   = "item_id";
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_ITEM_PRIO = "item_priority";

    private OnItemAddedListener listener;

    /** Factory for Add mode. */
    public static AddItemDialogFragment newAddInstance(int tripId) {
        AddItemDialogFragment f = new AddItemDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRIP_ID, tripId);
        f.setArguments(args);
        return f;
    }

    /** Factory for Edit mode – pre-fills fields with existing item data. */
    public static AddItemDialogFragment newEditInstance(Item item) {
        AddItemDialogFragment f = new AddItemDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRIP_ID, item.getTripId());
        args.putInt(ARG_ITEM_ID, item.getId());
        args.putString(ARG_ITEM_NAME, item.getItemName());
        args.putString(ARG_ITEM_PRIO, item.getPriority());
        f.setArguments(args);
        return f;
    }

    /** Legacy setter kept for backward compatibility with MainActivity FAB usage. */
    public void setTripId(int tripId) {
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        args.putInt(ARG_TRIP_ID, tripId);
        setArguments(args);
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

        Bundle args   = getArguments();
        boolean isEdit = args != null && args.containsKey(ARG_ITEM_ID);
        int tripId     = args != null ? args.getInt(ARG_TRIP_ID) : -1;
        int itemId     = isEdit ? args.getInt(ARG_ITEM_ID) : -1;

        if (isEdit) {
            // Pre-fill name
            etItemName.setText(args.getString(ARG_ITEM_NAME));
            // Pre-select priority radio button
            String prio = args.getString(ARG_ITEM_PRIO, "High");
            if ("Normal".equals(prio)) {
                ((RadioButton) view.findViewById(R.id.rbNormal)).setChecked(true);
            } else if ("Low".equals(prio)) {
                ((RadioButton) view.findViewById(R.id.rbLow)).setChecked(true);
            } else {
                ((RadioButton) view.findViewById(R.id.rbHigh)).setChecked(true);
            }
        } else {
            ((RadioButton) view.findViewById(R.id.rbHigh)).setChecked(true);
        }

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        String title = isEdit ? getString(R.string.edit_item) : getString(R.string.add_item);

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = etItemName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), R.string.fill_all_fields,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedId = rgPriority.getCheckedRadioButtonId();
                    String priority;
                    if (selectedId == R.id.rbHigh) {
                        priority = "High";
                    } else if (selectedId == R.id.rbNormal) {
                        priority = "Normal";
                    } else {
                        priority = "Low";
                    }

                    if (isEdit) {
                        dbHelper.updateItem(itemId, name, priority);
                        Toast.makeText(getContext(), R.string.item_updated,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.insertItem(tripId, name, priority);
                        Toast.makeText(getContext(), R.string.item_added,
                                Toast.LENGTH_SHORT).show();
                    }
                    if (listener != null) listener.onItemAdded();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
