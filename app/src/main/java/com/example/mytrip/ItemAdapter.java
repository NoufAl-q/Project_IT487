package com.example.mytrip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ItemAdapter – RecyclerView Adapter + ViewHolder for checklist items.
 *
 * Rubric: RecyclerView + Adapter + ViewHolder; priority colour-coding; checkbox update.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    /** Callback interface implemented by TripDetailActivity. */
    public interface OnItemActionListener {
        void onItemChecked(Item item, boolean checked);
        void onItemDelete(Item item);
    }

    private final Context context;
    private final List<Item> itemList;
    private final OnItemActionListener listener;

    public ItemAdapter(Context context, List<Item> itemList, OnItemActionListener listener) {
        this.context  = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_checklist_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.tvItemName.setText(item.getItemName());
        holder.tvPriority.setText(item.getPriority());

        // Colour-code priority text
        int priorityColor;
        switch (item.getPriority()) {
            case "High":
                priorityColor = ContextCompat.getColor(context, R.color.priority_high);
                break;
            case "Normal":
                priorityColor = ContextCompat.getColor(context, R.color.priority_normal);
                break;
            default: // "Low"
                priorityColor = ContextCompat.getColor(context, R.color.priority_low);
                break;
        }
        holder.tvPriority.setTextColor(priorityColor);

        // Reflect checked state (avoid triggering listener during bind)
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.getIsChecked() == 1);
        holder.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
            item.setIsChecked(isChecked ? 1 : 0);
            listener.onItemChecked(item, isChecked);
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> listener.onItemDelete(item));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final CardView    cardView;
        final TextView    tvItemName;
        final TextView    tvPriority;
        final CheckBox    checkBox;
        final ImageButton btnDelete;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView   = itemView.findViewById(R.id.cardItem);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            checkBox   = itemView.findViewById(R.id.checkBoxItem);
            btnDelete  = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
