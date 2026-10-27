package com.oswaldvalois.madmidterm;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Mid Examination IT 2025 - Class 2, Case 2 (even student ID):
 * student name, student ID, enrolled or not, stored in SQLite.
 * Supports ADD, EDIT, DELETE and VIEW.
 */
public class MainActivity extends AppCompatActivity implements StudentAdapter.Listener {

    private static final long NO_SELECTION = -1L;

    private StudentDbHelper dbHelper;
    private StudentAdapter adapter;
    private final List<Student> students = new ArrayList<>();

    private View rootLayout;
    private TextView textFormTitle;
    private TextView textSummary;
    private TextView textEmpty;
    private EditText inputName;
    private EditText inputStudentId;
    private MaterialCheckBox checkEnrolled;
    private MaterialButton buttonUpdate;
    private MaterialButton buttonDelete;

    private long selectedId = NO_SELECTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        dbHelper = new StudentDbHelper(this);

        rootLayout = findViewById(R.id.rootLayout);
        textFormTitle = findViewById(R.id.textFormTitle);
        textSummary = findViewById(R.id.textSummary);
        textEmpty = findViewById(R.id.textEmpty);
        inputName = findViewById(R.id.inputName);
        inputStudentId = findViewById(R.id.inputStudentId);
        checkEnrolled = findViewById(R.id.checkEnrolled);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);

        applyWindowInsets();

        RecyclerView recyclerStudents = findViewById(R.id.recyclerStudents);
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, this);
        recyclerStudents.setAdapter(adapter);

        findViewById(R.id.buttonAdd).setOnClickListener(v -> addStudent());
        buttonUpdate.setOnClickListener(v -> updateStudent());
        buttonDelete.setOnClickListener(v -> confirmDelete());
        findViewById(R.id.buttonClear).setOnClickListener(v -> {
            resetForm();
            showMessage(getString(R.string.msg_form_cleared));
        });

        resetForm();
        loadStudents();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    // ---------------------------------------------------------------- ADD

    private void addStudent() {
        String name = readName();
        if (name == null) {
            return;
        }
        String studentId = readStudentId();
        if (studentId == null) {
            return;
        }
        if (dbHelper.studentIdExists(studentId, NO_SELECTION)) {
            inputStudentId.setError(getString(R.string.error_duplicate_id));
            inputStudentId.requestFocus();
            return;
        }

        long newRowId = dbHelper.insertStudent(name, studentId, checkEnrolled.isChecked());
        if (newRowId == -1L) {
            showMessage(getString(R.string.msg_failed));
            return;
        }

        resetForm();
        loadStudents();
        showMessage(getString(R.string.msg_added, name));
    }

    // ---------------------------------------------------------------- EDIT

    private void updateStudent() {
        if (selectedId == NO_SELECTION) {
            showMessage(getString(R.string.msg_need_selection));
            return;
        }
        String name = readName();
        if (name == null) {
            return;
        }
        String studentId = readStudentId();
        if (studentId == null) {
            return;
        }
        if (dbHelper.studentIdExists(studentId, selectedId)) {
            inputStudentId.setError(getString(R.string.error_duplicate_id));
            inputStudentId.requestFocus();
            return;
        }

        int rows = dbHelper.updateStudent(selectedId, name, studentId, checkEnrolled.isChecked());
        if (rows <= 0) {
            showMessage(getString(R.string.msg_failed));
            return;
        }

        resetForm();
        loadStudents();
        showMessage(getString(R.string.msg_updated, name));
    }

    // ---------------------------------------------------------------- DELETE

    private void confirmDelete() {
        if (selectedId == NO_SELECTION) {
            showMessage(getString(R.string.msg_need_selection));
            return;
        }
        Student student = dbHelper.getStudentById(selectedId);
        if (student == null) {
            resetForm();
            loadStudents();
            showMessage(getString(R.string.msg_failed));
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, student.getName()))
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_delete, (dialog, which) -> deleteSelected())
                .show();
    }

    private void deleteSelected() {
        Student student = dbHelper.getStudentById(selectedId);
        int rows = dbHelper.deleteStudent(selectedId);
        resetForm();
        loadStudents();
        showMessage(rows > 0
                ? getString(R.string.msg_deleted, student != null ? student.getName() : "")
                : getString(R.string.msg_failed));
    }

    // ---------------------------------------------------------------- LIST CALLBACKS

    @Override
    public void onEdit(Student student) {
        selectedId = student.getId();
        inputName.setText(student.getName());
        inputStudentId.setText(student.getStudentId());
        checkEnrolled.setChecked(student.isEnrolled());
        inputName.setError(null);
        inputStudentId.setError(null);
        inputName.requestFocus();
        updateSelectionUi();
        adapter.setSelectedId(selectedId);
    }

    @Override
    public void onDelete(Student student) {
        selectedId = student.getId();
        adapter.setSelectedId(selectedId);
        updateSelectionUi();
        confirmDelete();
    }

    // ---------------------------------------------------------------- HELPERS

    /**
     * Android 15+ draws every app edge-to-edge: keep the content clear of the
     * status/navigation bars and the keyboard, and flip the system bar icons so
     * they stay readable against the (light or dark) app background.
     */
    private void applyWindowInsets() {
        final int basePadding = getResources().getDimensionPixelSize(R.dimen.screen_padding);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(
                    basePadding,
                    basePadding + bars.top,
                    basePadding,
                    basePadding + Math.max(bars.bottom, ime.bottom));
            return windowInsets;
        });

        boolean nightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!nightMode);
        controller.setAppearanceLightNavigationBars(!nightMode);
    }

    private void loadStudents() {
        students.clear();
        students.addAll(dbHelper.getAllStudents());
        adapter.setSelectedId(selectedId);
        textSummary.setText(getString(R.string.summary_count, students.size()));
        textEmpty.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Puts the form back into "adding a new record" mode. */
    private void resetForm() {
        selectedId = NO_SELECTION;
        inputName.setText("");
        inputStudentId.setText("");
        inputName.setError(null);
        inputStudentId.setError(null);
        checkEnrolled.setChecked(false);
        updateSelectionUi();
        if (adapter != null) {
            adapter.setSelectedId(selectedId);
        }
    }

    private void updateSelectionUi() {
        boolean hasSelection = selectedId != NO_SELECTION;
        buttonUpdate.setEnabled(hasSelection);
        buttonDelete.setEnabled(hasSelection);
        textFormTitle.setText(hasSelection ? R.string.form_title_edit : R.string.form_title_add);
    }

    private String readName() {
        String name = inputName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            inputName.setError(getString(R.string.error_name_required));
            inputName.requestFocus();
            return null;
        }
        return name;
    }

    private String readStudentId() {
        String studentId = inputStudentId.getText().toString().trim();
        if (TextUtils.isEmpty(studentId)) {
            inputStudentId.setError(getString(R.string.error_student_id_required));
            inputStudentId.requestFocus();
            return null;
        }
        return studentId;
    }

    private void showMessage(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}