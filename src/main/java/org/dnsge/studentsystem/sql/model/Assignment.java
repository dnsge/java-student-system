package org.dnsge.studentsystem.sql.model;

import java.sql.Date;
import java.util.Collection;

public class Assignment {

    private final int id, courseId;
    private String name;
    private int totalPoints;
    private Date dueDate;

    private Course course;
    private Grade grade;
    private Teacher teacher;

    public Assignment(int id, String name, int totalPoints, Date dueDate, int courseId) {
        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.totalPoints = totalPoints;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getGradeText() {
        if (this.grade == null) {
            return String.format("-/%d", this.totalPoints);
        } else {
            return this.grade.getGradeText(this.totalPoints);
        }
    }

    public static String calculateAverageGrade(Collection<Assignment> assignments) {
        int totalPoints = 0;
        int totalPossiblePoints = 0;
        for (Assignment a : assignments) {
            if (a.getGrade() != null) {
                totalPoints += a.getGrade().getPoints();
                totalPossiblePoints += a.getTotalPoints();
            }
        }

        if (totalPossiblePoints == 0) {
            if (totalPoints > 0) {
                return "100.00%";
            } else {
                return "--%";
            }
        } else {
            return String.format("%.2f%%", (float) totalPoints / totalPossiblePoints * 100);
        }
    }
}
