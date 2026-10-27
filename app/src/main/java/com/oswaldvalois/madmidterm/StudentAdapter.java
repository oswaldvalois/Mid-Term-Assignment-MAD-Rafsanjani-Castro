package com.oswaldvalois.madmidterm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Shows one card per student, with EDIT and DELETE actions on every card.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface Listener {
        void onEdit(Student student);

        void onDelete(Student student);
    }

    private final List<Student> students;
    private final Listener listener;
    private long selectedId = -1L;

    public StudentAdapter(List<Student> students, Listener listener) {
        this.students = students;
        this.listener = listener;
    }

    /** Highlights the row that is currently loaded in the form. */
    public void setSelectedId(long selectedId) {
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final TextView textName;
        private final TextView textStudentId;
        private final TextView textStatus;
        private final MaterialButton buttonEdit;
        private final MaterialButton buttonDelete;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            textName = itemView.findViewById(R.id.textStudentName);
            textStudentId = itemView.findViewById(R.id.textStudentId);
            textStatus = itemView.findViewById(R.id.textStatus);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(final Student student) {
            Context context = itemView.getContext();

            textName.setText(student.getName());
            textStudentId.setText(context.getString(R.string.item_student_id, student.getStudentId()));

            if (student.isEnrolled()) {
                textStatus.setText(R.string.status_enrolled);
                textStatus.setTextColor(ContextCompat.getColor(context, R.color.status_enrolled));
            } else {
                textStatus.setText(R.string.status_not_enrolled);
                textStatus.setTextColor(ContextCompat.getColor(context, R.color.status_not_enrolled));
            }

            boolean selected = student.getId() == selectedId;
            int strokeWidth = selected
                    ? itemView.getResources().getDimensionPixelSize(R.dimen.card_stroke_width_selected)
                    : itemView.getResources().getDimensionPixelSize(R.dimen.card_stroke_width);
            card.setStrokeWidth(strokeWidth);
            card.setStrokeColor(ContextCompat.getColor(
                    context, selected ? R.color.card_stroke_selected : R.color.card_stroke));

            card.setOnClickListener(v -> listener.onEdit(student));
            buttonEdit.setOnClickListener(v -> listener.onEdit(student));
            buttonDelete.setOnClickListener(v -> listener.onDelete(student));
        }
    }
}