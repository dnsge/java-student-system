package org.dnsge.studentsystem.sql.model;

public class Enrollment {
    private final int id;
    private int studentId, courseId, periodId;

    private Student student;
    private Course course;
    private Period period;
    private Teacher teacher;

    public Enrollment(int id, int studentId, int courseId, int periodId) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.periodId = periodId;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public int getPeriodId() {
        return periodId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Period getPeriod() {
        return period;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
