package com.smartattend.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.smartattend.app.AttendanceHistoryActivity;
import com.smartattend.app.R;
import com.smartattend.app.model.Course;
import com.smartattend.app.utils.Constants;

import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final Context context;
    private List<Course> courses;

    public CourseAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
    }

    public void updateData(List<Course> newData) {
        this.courses = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);

        holder.tvCourseCode.setText(course.getCourseCode());
        holder.tvCourseName.setText(course.getCourseName());
        holder.tvPercentage.setText(String.format(Locale.getDefault(),
                "%.1f%%", course.getAttendancePercentage()));
        holder.progressBar.setProgress((int) course.getAttendancePercentage());

        // Color-code attendance percentage
        double pct = course.getAttendancePercentage();
        int color;
        if (pct >= 75) {
            color = context.getResources().getColor(R.color.color_success, context.getTheme());
        } else if (pct >= 50) {
            color = context.getResources().getColor(R.color.color_warning, context.getTheme());
        } else {
            color = context.getResources().getColor(R.color.color_error, context.getTheme());
        }
        holder.tvPercentage.setTextColor(color);

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AttendanceHistoryActivity.class);
            intent.putExtra(Constants.EXTRA_COURSE_ID, course.getId());
            intent.putExtra(Constants.EXTRA_COURSE_NAME, course.getCourseName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvCourseCode, tvCourseName, tvPercentage;
        LinearProgressIndicator progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
