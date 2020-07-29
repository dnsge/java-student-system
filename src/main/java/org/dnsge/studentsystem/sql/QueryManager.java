package org.dnsge.studentsystem.sql;

import org.dnsge.studentsystem.Util;
import org.dnsge.studentsystem.sql.model.*;

import java.sql.Date;
import java.sql.*;
import java.util.*;

public class QueryManager {

    public static final String getStudentByUsernameStatement =
            "SELECT u.id, s.first_name, s.last_name " +
                    "FROM students s " +
                    "JOIN users u on s.id = u.id " +
                    "WHERE u.username = ?";
    public static final String createGradeStatement =
            "INSERT INTO grades(student_id, assignment_id, points) " +
                    "VALUES(?, ?, ?)";
    private static final String createUserStatement =
            "INSERT INTO users(username, password, type) " +
                    "VALUES(?, ?, ?)";
    private static final String loginUserStatement =
            "SELECT id, username, password, type " +
                    "FROM users " +
                    "WHERE username=? " +
                    "LIMIT 1";
    private static final String getCoursesStatement =
            "SELECT c.id, c.name, c.room, " +
                    "t.id, t.first_name, t.last_name, " +
                    "p.id, p.display, p.start_time, " +
                    "e.id " +
                    "FROM enrollments e " +
                    "JOIN courses c on e.course_id = c.id " +
                    "JOIN teachers t on c.teacher_id = t.id " +
                    "JOIN periods p on e.period_id = p.id " +
                    "WHERE e.student_id=?";
    private static final String getAssignmentsStatement =
            "SELECT a.id, a.name, a.total_points, a.due_date " +
                    "FROM assignments a " +
                    "WHERE a.course_id = ? " +
                    "ORDER BY a.due_date DESC";
    private static final String getCourseStatement =
            "SELECT c.teacher_id, c.name, c.room " +
                    "FROM courses c " +
                    "WHERE c.id = ?";
    private static final String getAssignmentStatement =
            "SELECT c.teacher_id, a.name, a.total_points, a.due_date, a.course_id " +
                    "FROM assignments a " +
                    "JOIN courses c on a.course_id = c.id " +
                    "WHERE a.id = ? " +
                    "LIMIT 1";
    private static final String getCourseFromAssignmentStatement =
            "SELECT c.id, c.teacher_id, c.name, c.room " +
                    "FROM courses c " +
                    "JOIN assignments a on c.id = a.course_id " +
                    "WHERE a.id = ?";
    private static final String deleteAssignmentStatement =
            "DELETE FROM assignments a " +
                    "WHERE a.id = ?";
    private static final String createAssignmentStatement =
            "INSERT INTO assignments(name, total_points, due_date, course_id) " +
                    "VALUES(?, ?, ?, ?)";
    private static final String updateAssignmentStatement =
            "UPDATE assignments a " +
                    "SET a.name = ?, a.total_points = ?, a.due_date = ? " +
                    "WHERE a.id = ?";
    private static final String getTeacherCoursesStatement =
            "SELECT id, name, room " +
                    "FROM courses c " +
                    "WHERE c.teacher_id = ?";
    private static final String getStudentAssignmentGradesStatement =
            "SELECT a.id, a.name, a.total_points,  a.due_date, g.id, g.points, g.graded_at " +
                    "FROM assignments a " +
                    "LEFT JOIN grades g on a.id = g.assignment_id " +
                    "WHERE a.course_id = ? " +
                    "AND (g.student_id = ? OR g.student_id IS NULL) " +
                    "ORDER BY a.due_date DESC";
    private static final String getTeacherAssignmentGradesStatement =
            "SELECT g.id, g.student_id, g.points, g.graded_at, s.id, s.first_name, s.last_name " +
                    "FROM grades g " +
                    "JOIN students s on g.student_id = s.id " +
                    "WHERE g.assignment_id = ? " +
                    "ORDER BY g.graded_at DESC";
    private static final String getGradeStatement =
            "SELECT g.student_id, g.assignment_id, g.points, g.graded_at, t.id " +
                    "FROM grades g " +
                    "JOIN assignments a on g.assignment_id = a.id " +
                    "JOIN courses c on a.course_id = c.id " +
                    "JOIN teachers t on c.teacher_id = t.id " +
                    "WHERE g.id = ?";
    private static final String deleteGradeStatement =
            "DELETE FROM grades g " +
                    "WHERE g.id = ?";
    private static final String getCourseEnrollmentsStatement =
            "SELECT e.id, e.student_id, s.id, s.first_name, s.last_name, p.id, p.display, p.start_time " +
                    "FROM enrollments e " +
                    "JOIN students s on e.student_id = s.id " +
                    "JOIN periods p on e.period_id = p.id " +
                    "WHERE e.course_id = ? " +
                    "ORDER BY p.start_time ASC";
    private static final String getPeriodsStatement =
            "SELECT p.id, p.display, p.start_time " +
                    "FROM periods p " +
                    "ORDER BY p.start_time ASC";

    private static final Calendar cal = Calendar.getInstance();
    private final Connection connection;

    public QueryManager(Connection connection) {
        if (connection == null) {
            throw new IllegalStateException("QueryManager must have not-null connection");
        }
        this.connection = connection;
    }

    public static QueryManager getQueryManager() {
        return new QueryManager(MySQLConnector.getInstance().getConnection());
    }

    private static <E> Optional<Collection<E>> collectResults(ResultSet rs, ResultConsumer<E> func) throws SQLException {
        if (rs == null)
            return Optional.empty();

        List<E> items = new ArrayList<>();
        while (rs.next()) {
            items.add(func.apply(rs));
        }

        return Optional.of(items);
    }

    private static <E> Optional<E> collectResult(ResultSet rs, ResultConsumer<E> func) throws SQLException {
        if (rs == null || !rs.next())
            return Optional.empty();

        E res = func.apply(rs);
        if (res == null)
            return Optional.empty();

        return Optional.of(res);
    }

    public User createUser(String username, String password, char type) throws SQLException {
        String hash = Util.hashPass(password);

        PreparedStatement ps = this.connection.prepareStatement(createUserStatement, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        ps.setString(2, hash);
        ps.setObject(3, type, Types.CHAR);

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating new user failed");
        }

        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys != null && generatedKeys.next()) {
            return new User(generatedKeys.getInt(1), username, hash, type);
        } else {
            throw new SQLException("Creating new user failed");
        }
    }

    public Optional<User> loginUser(String username, String password) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(loginUserStatement);
        ps.setString(1, username);
        return collectResult(ps.executeQuery(), rs -> {
            User u = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4).charAt(0));
            if (u.verifyPassword(password)) {
                return u;
            } else {
                return null;
            }
        });
    }

    public Optional<Collection<Enrollment>> getStudentEnrollments(int studentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getCoursesStatement);
        ps.setInt(1, studentId);
        return collectResults(ps.executeQuery(), rs -> {
            Course c = new Course(rs.getInt(1), rs.getInt(4), rs.getString(2), rs.getString(3));
            Teacher t = new Teacher(rs.getInt(4), rs.getString(5), rs.getString(6));
            Period p = new Period(rs.getInt(7), rs.getString(8), rs.getTime(9));

            c.setTeacher(t);

            Enrollment e = new Enrollment(rs.getInt(10), studentId, c.getId(), p.getId());
            e.setCourse(c);
            e.setPeriod(p);

            return e;
        });
    }

    public Optional<Collection<Assignment>> getCourseAssignments(int courseId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getAssignmentsStatement);
        ps.setInt(1, courseId);
        return collectResults(ps.executeQuery(), rs -> new Assignment(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDate(4), courseId));
    }

    public Optional<Course> getCourse(int courseId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getCourseStatement);
        ps.setInt(1, courseId);
        return collectResult(ps.executeQuery(), rs -> new Course(courseId, rs.getInt(1), rs.getString(2), rs.getString(3)));
    }

    public Optional<Assignment> getAssignment(int assignmentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getAssignmentStatement);
        ps.setInt(1, assignmentId);
        return collectResult(ps.executeQuery(), rs -> {
            Assignment a = new Assignment(assignmentId, rs.getString(2), rs.getInt(3), rs.getDate(4), rs.getInt(5));
            Course c = new Course(rs.getInt(5), rs.getInt(1), "", "");
            a.setCourse(c);
            return a;
        });
    }

    public Optional<Course> getCourseFromAssignment(int assignmentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getCourseFromAssignmentStatement);
        ps.setInt(1, assignmentId);
        return collectResult(ps.executeQuery(), rs -> new Course(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4)));
    }

    public void deleteAssignment(int assignmentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(deleteAssignmentStatement);
        ps.setInt(1, assignmentId);
        ps.execute();
    }

    public void createAssignment(String name, int totalPoints, Date dueDate, int courseId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(createAssignmentStatement);
        ps.setString(1, name);
        ps.setInt(2, totalPoints);
        ps.setDate(3, dueDate);
        ps.setInt(4, courseId);
        ps.execute();
    }

    public void updateAssignment(int assignmentId, String name, int totalPoints, Date dueDate) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(updateAssignmentStatement);
        ps.setString(1, name);
        ps.setInt(2, totalPoints);
        ps.setDate(3, dueDate);
        ps.setInt(4, assignmentId);
        ps.execute();
    }

    public Optional<Collection<Course>> getTeacherCourses(int teacherId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getTeacherCoursesStatement);
        ps.setInt(1, teacherId);
        return collectResults(ps.executeQuery(), rs -> new Course(rs.getInt(1), teacherId, rs.getString(2), rs.getString(3)));
    }

    public Optional<Collection<Assignment>> getStudentAssignmentGrades(int studentId, int courseId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getStudentAssignmentGradesStatement);
        ps.setInt(1, courseId);
        ps.setInt(2, studentId);
        return collectResults(ps.executeQuery(), rs -> {
            Assignment a = new Assignment(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDate(4), courseId);
            int gradeId = rs.getInt(5);
            if (!rs.wasNull()) {
                Grade g = new Grade(gradeId, studentId, a.getId(), rs.getInt(6), rs.getTimestamp(7, cal));
                a.setGrade(g);
            }
            return a;
        });
    }

    public Optional<Collection<Grade>> getTeacherAssignmentGrades(int assignmentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getTeacherAssignmentGradesStatement);
        ps.setInt(1, assignmentId);
        return collectResults(ps.executeQuery(), rs -> {
            Grade g = new Grade(rs.getInt(1), rs.getInt(2), assignmentId, rs.getInt(3), rs.getTimestamp(4, cal));
            Student s = new Student(rs.getInt(5), rs.getString(6), rs.getString(7));
            g.setStudent(s);
            return g;
        });
    }

    public Optional<Grade> getGrade(int gradeId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getGradeStatement);
        ps.setInt(1, gradeId);
        return collectResult(ps.executeQuery(), rs -> {
            Grade g = new Grade(gradeId, rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getTimestamp(4, cal));
            Teacher t = new Teacher(rs.getInt(5), "", "");
            g.setTeacher(t);
            return g;
        });
    }

    public void deleteGrade(int gradeId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(deleteGradeStatement);
        ps.setInt(1, gradeId);
        ps.execute();
    }

    public void createGrade(int studentId, int assignmentId, int points) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(createGradeStatement);
        ps.setInt(1, studentId);
        ps.setInt(2, assignmentId);
        ps.setInt(3, points);
        ps.execute();
    }

    public Optional<Student> getStudent(String username) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getStudentByUsernameStatement);
        ps.setString(1, username);
        return collectResult(ps.executeQuery(), rs -> new Student(rs.getInt(1), rs.getString(2), rs.getString(3)));
    }

    public Optional<Collection<Enrollment>> getCourseEnrollments(int courseId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getCourseEnrollmentsStatement);
        ps.setInt(1, courseId);
        return collectResults(ps.executeQuery(), rs -> {
            Enrollment e = new Enrollment(rs.getInt(1), rs.getInt(2), courseId, rs.getInt(6));
            Student s = new Student(rs.getInt(3), rs.getString(4), rs.getString(5));
            Period p = new Period(rs.getInt(6), rs.getString(7), rs.getTime(8));
            e.setStudent(s);
            e.setPeriod(p);
            return e;
        });
    }

    public Optional<Collection<Period>> getPeriods() throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement(getPeriodsStatement);
        return collectResults(ps.executeQuery(), rs -> new Period(rs.getInt(1), rs.getString(2), rs.getTime(3)));
    }

    public Optional<Period> getPeriod(int periodId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement("SELECT p.id, p.display, p.start_time FROM periods p WHERE p.id = ?");
        ps.setInt(1, periodId);
        return collectResult(ps.executeQuery(), rs -> new Period(rs.getInt(1), rs.getString(2), rs.getTime(3)));
    }

    public void createEnrollment(int studentId, int courseId, int periodId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement("INSERT INTO enrollments(student_id, course_id, period_id) VALUES(?, ?, ?)");
        ps.setInt(1, studentId);
        ps.setInt(2, courseId);
        ps.setInt(3, periodId);
        ps.execute();
    }

    public Optional<Enrollment> getEnrollment(int enrollmentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement("SELECT e.student_id, e.course_id, e.period_id, t.id FROM enrollments e JOIN courses c on e.course_id = c.id JOIN teachers t on c.teacher_id = t.id WHERE e.id = ?");
        ps.setInt(1, enrollmentId);
        return collectResult(ps.executeQuery(), rs -> {
            Enrollment e = new Enrollment(enrollmentId, rs.getInt(1), rs.getInt(2), rs.getInt(3));
            Teacher t = new Teacher(rs.getInt(4), "", "");
            e.setTeacher(t);
            return e;
        });
    }

    public void deleteEnrollment(int enrollmentId) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement("DELETE FROM enrollments e WHERE e.id = ?");
        ps.setInt(1, enrollmentId);
        ps.execute();
    }

    @FunctionalInterface
    private interface ResultConsumer<R> {
        R apply(ResultSet t) throws SQLException;
    }
}
