package com.smartattend.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartattend.app.R;

import java.util.List;

public class HelpFaqAdapter extends RecyclerView.Adapter<HelpFaqAdapter.ViewHolder> {

    private final List<FaqItem> items;

    public static class FaqItem {
        public final String question;
        public final String answer;
        public boolean expanded;

        public FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.expanded = false;
        }
    }

    public HelpFaqAdapter(List<FaqItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaqItem item = items.get(position);
        holder.tvQuestion.setText(item.question);
        holder.tvAnswer.setText(item.answer);
        holder.tvAnswer.setVisibility(item.expanded ? View.VISIBLE : View.GONE);
        holder.ivExpand.setRotation(item.expanded ? 180f : 0f);

        holder.itemView.setOnClickListener(v -> {
            item.expanded = !item.expanded;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView ivExpand;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }
    }
}
