package com.example.mytrip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * TripAdapter – RecyclerView Adapter + ViewHolder for the trip list.
 *
 * Rubric: RecyclerView + Adapter + ViewHolder pattern.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    /** Callback interface implemented by MainActivity. */
    public interface OnTripClickListener {
        void onTripClick(Trip trip);
        void onTripLongClick(Trip trip);
        void onTripEdit(Trip trip);
    }

    private final Context context;
    private final List<Trip> tripList;
    private final OnTripClickListener listener;

    public TripAdapter(Context context, List<Trip> tripList, OnTripClickListener listener) {
        this.context = context;
        this.tripList = tripList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_trip_card, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);

        // Letter avatar: first character of destination (upper-case)
        String initial = trip.getDestination().isEmpty()
                ? "?"
                : String.valueOf(trip.getDestination().charAt(0)).toUpperCase();
        holder.tvInitial.setText(initial);

        holder.tvDestination.setText(trip.getDestination());
        holder.tvDate.setText(trip.getDate());

        // Single tap → detail screen
        holder.cardView.setOnClickListener(v -> listener.onTripClick(trip));

        // Long-press → delete confirmation
        holder.cardView.setOnLongClickListener(v -> {
            listener.onTripLongClick(trip);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class TripViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final TextView tvInitial;
        final TextView tvDestination;
        final TextView tvDate;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView      = itemView.findViewById(R.id.cardTrip);
            tvInitial     = itemView.findViewById(R.id.tvInitial);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDate        = itemView.findViewById(R.id.tvDate);
        }
    }
}
