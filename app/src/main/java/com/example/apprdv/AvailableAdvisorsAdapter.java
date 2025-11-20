package com.example.apprdv;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvailableAdvisorsAdapter extends RecyclerView.Adapter<AvailableAdvisorsAdapter.ViewHolder> {

    private List<Advisor> advisors;
    private OnSlotClickListener onSlotClickListener;

    public AvailableAdvisorsAdapter(List<Advisor> advisors, OnSlotClickListener onSlotClickListener) {
        this.advisors = advisors;
        this.onSlotClickListener = onSlotClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advisor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Advisor advisor = advisors.get(position);
        holder.advisorName.setText(advisor.getName());
        holder.advisorAddress.setText(advisor.getAddress());

        // Dynamically add available slots
        holder.slotsContainer.removeAllViews();
        for (String slot : advisor.getAvailableSlots()) {
            Button slotButton = new Button(holder.itemView.getContext());
            slotButton.setText(slot);
            slotButton.setOnClickListener(v -> onSlotClickListener.onSlotClick(advisor, slot));
            holder.slotsContainer.addView(slotButton);
        }
    }

    @Override
    public int getItemCount() {
        return advisors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView advisorName, advisorAddress;
        LinearLayout slotsContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            advisorName = itemView.findViewById(R.id.advisorName);
            advisorAddress = itemView.findViewById(R.id.advisorAddress);
            slotsContainer = itemView.findViewById(R.id.slotsContainer);
        }
    }

    public interface OnSlotClickListener {
        void onSlotClick(Advisor advisor, String selectedSlot);
    }
}
