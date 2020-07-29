package org.dnsge.studentsystem.sql.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class Grade {
    private final int id, studentId, assignmentId;
    private int points;
    private Timestamp gradedAt;

    private Assignment assignment;
    private Student student;
    private Teacher teacher;

    public Grade(int id, int studentId, int assignmentId, int points, Timestamp gradedAt) {
        this.id = id;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.points = points;
        this.gradedAt = gradedAt;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Timestamp getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(Timestamp gradedAt) {
        this.gradedAt = gradedAt;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public String getGradeText(int totalPoints) {
        float score = (float) this.points / totalPoints * 100;
        return String.format("%d/%d (%.2f%%)", this.points, totalPoints, score);
    }

    public String gradedAtString() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(this.gradedAt);
    }

    public static String calculateAverageGrade(Collection<Grade> grades, Assignment a) {
        int points = 0;
        int totalPoints = 0;
        for (Grade g : grades) {
            points += g.getPoints();
            totalPoints += a.getTotalPoints();
        }

        if (totalPoints == 0) {
            if (points > 0) {
                return "100.00%";
            } else {
                return "--%";
            }
        } else {
            return String.format("%.2f%%", (float) points / totalPoints * 100);
        }
    }
}
