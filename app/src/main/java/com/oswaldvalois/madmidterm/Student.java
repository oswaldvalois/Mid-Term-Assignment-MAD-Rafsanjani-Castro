package com.oswaldvalois.madmidterm;

/**
 * Plain data holder for one student record.
 * Case 2 of the Mid Examination: name, student ID, enrolled or not.
 */
public class Student {

    private final long id;
    private final String name;
    private final String studentId;
    private final boolean enrolled;

    public Student(long id, String name, String studentId, boolean enrolled) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.enrolled = enrolled;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean isEnrolled() {
        return enrolled;
    }
}