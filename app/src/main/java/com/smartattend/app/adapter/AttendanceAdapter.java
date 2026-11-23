package com.smartattend.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.smartattend.app.AttendanceDetailsActivity;
import com.smartattend.app.R;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.utils.Constants;
import com.smartattend.app.utils.DateUtils;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private final Context context;
    private List<AttendanceRecord> records;

    public AttendanceAdapter(Context context, List<AttendanceRecord> records) {
        this.context = context;
        this.records = records;
    }

    public void updateData(List<AttendanceRecord> newData) {
        this.records = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = records.get(position);

        holder.tvCourseName.setText(record.getCourseName() != null ? record.getCourseName() : "N/A");
        holder.tvCourseCode.setText(record.getCourseCode() != null ? record.getCourseCode() : "");
        String displayDate = record.getSessionDate() != null
                ? record.getSessionDate() : record.getCheckInTime();
        holder.tvDate.setText(DateUtils.formatDate(displayDate));
        holder.tvTime.setText(DateUtils.formatDateTime(record.getCheckInTime()));

        String status = record.getStatus();
        holder.tvStatus.setText(status != null ? status : "N/A");

        if (Constants.STATUS_PRESENT.equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_present);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else if (Constants.STATUS_LATE.equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_late);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_absent);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AttendanceDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_ATTENDANCE_RECORD, new Gson().toJson(record));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvCourseCode, tvDate, tvTime, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
