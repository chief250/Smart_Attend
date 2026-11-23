package com.smartattend.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartattend.app.R;
import com.smartattend.app.model.AttendanceSession;
import com.smartattend.app.utils.DateUtils;

import java.util.List;

public class TodaySessionAdapter extends RecyclerView.Adapter<TodaySessionAdapter.ViewHolder> {

    private final Context context;
    private List<AttendanceSession> sessions;
    private final OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(AttendanceSession session);
    }

    public TodaySessionAdapter(Context context, List<AttendanceSession> sessions,
                               OnSessionClickListener listener) {
        this.context = context;
        this.sessions = sessions;
        this.listener = listener;
    }

    public void updateData(List<AttendanceSession> newData) {
        this.sessions = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_today_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceSession session = sessions.get(position);

        holder.tvCourseCode.setText(session.getCourseCode() != null ? session.getCourseCode() : "");
        holder.tvCourseName.setText(session.getCourseName() != null ? session.getCourseName() : "");
        String timeRange = DateUtils.formatTimeOnly(session.getStartTime())
                + " - " + DateUtils.formatTimeOnly(session.getEndTime());
        holder.tvTime.setText(timeRange);

        String status = session.getStatus();
        holder.tvStatus.setText(status != null ? status : "");

        if (session.isAttended()) {
            holder.tvStatus.setText("ATTENDED ✓");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_present);
            holder.tvAction.setText("Attendance Recorded ✓");
            holder.tvAction.setTextColor(context.getResources().getColor(R.color.color_success, context.getTheme()));
            holder.cardView.setStrokeColor(context.getResources().getColor(R.color.color_success, context.getTheme()));
            holder.cardView.setAlpha(0.65f);
            holder.cardView.setOnClickListener(v ->
                    android.widget.Toast.makeText(context, "Attendance already completed for this course today.", android.widget.Toast.LENGTH_SHORT).show()
            );
        } else if ("CLOSED".equalsIgnoreCase(session.getStatus())) {
            holder.tvStatus.setText("CLOSED");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_absent);
            holder.tvAction.setText("Session Closed");
            holder.tvAction.setTextColor(context.getResources().getColor(R.color.color_text_secondary, context.getTheme()));
            holder.cardView.setStrokeColor(context.getResources().getColor(R.color.color_divider, context.getTheme()));
            holder.cardView.setAlpha(0.6f);
            holder.cardView.setOnClickListener(v ->
                    android.widget.Toast.makeText(context, "This attendance session is closed.", android.widget.Toast.LENGTH_SHORT).show()
            );
        } else {
            holder.tvStatus.setText(status != null ? status : "OPEN");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_present);
            holder.tvAction.setText("Tap to scan QR →");
            holder.tvAction.setTextColor(context.getResources().getColor(R.color.color_primary, context.getTheme()));
            holder.cardView.setStrokeColor(context.getResources().getColor(R.color.color_primary, context.getTheme()));
            holder.cardView.setAlpha(1.0f);
            holder.cardView.setOnClickListener(v -> {
                if (listener != null) listener.onSessionClick(session);
            });
        }
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvCourseCode, tvCourseName, tvTime, tvStatus, tvAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAction = itemView.findViewById(R.id.tvAction);
        }
    }
}
