package org.dnsge.studentsystem.sql.model;

public class Course {
    private final int id, teacherId;
    private String name, room;

    private Teacher teacher;

    public Course(int id, int teacherId, String name, String room) {
        this.id = id;
        this.teacherId = teacherId;
        this.name = name;
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
