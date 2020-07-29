package org.dnsge.studentsystem.web.controllers;

import org.dnsge.studentsystem.Environment;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.Course;
import org.dnsge.studentsystem.sql.model.Enrollment;
import org.dnsge.studentsystem.sql.model.Period;
import org.dnsge.studentsystem.sql.model.Student;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import java.util.function.Function;

public class EnrollmentController {
    public static String createEnrollment(Request req, Response res) {
        return ControllerUtil.mustGetUser(req, res, u -> {
            String username;
            int periodId, courseId;
            try {
                String reqBody = req.body();
                JSONObject body = new JSONObject(reqBody);

                username = body.getString("username");
                periodId = body.getInt("periodId");
                courseId = body.getInt("courseId");
            } catch (JSONException | NumberFormatException e) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid JSON\n" + e.toString();
            }

            QueryManager qm = QueryManager.getQueryManager();
            try {
                Optional<Student> student = qm.getStudent(username);
                if (student.isEmpty()) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Student does not exist";
                }

                Optional<Course> course = qm.getCourse(courseId);
                if (course.isEmpty()) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Course does not exist";
                }

                Optional<Period> period = qm.getPeriod(periodId);
                if (period.isEmpty()) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Period does not exist";
                }

                if (course.get().getTeacherId() != u.getId() && Environment.enforcePermissions()) {
                    res.status(HttpStatus.FORBIDDEN_403);
                    return "";
                }

                qm.createEnrollment(student.get().getId(), course.get().getId(), periodId);
                res.status(HttpStatus.CREATED_201);
                return "";
            } catch (SQLIntegrityConstraintViolationException e) {
                res.status(HttpStatus.CONFLICT_409);
                return "That student is already enrolled during that period";
            } catch (SQLException e) {
                res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return e.toString();
            }
        });
    }

    public static String deleteEnrollment(Request req, Response res) {
        return enrollmentIdParam(req, res, enrollmentId -> {
            return ControllerUtil.mustGetUser(req, res, u -> {
                QueryManager qm = QueryManager.getQueryManager();
                try {
                    Optional<Enrollment> enrollment = qm.getEnrollment(enrollmentId);
                    if (enrollment.isEmpty()) {
                        res.status(HttpStatus.NOT_FOUND_404);
                        return "";
                    }

                    if (enrollment.get().getTeacher().getId() != u.getId() && Environment.enforcePermissions()) {
                        res.status(HttpStatus.FORBIDDEN_403);
                        return "";
                    }

                    qm.deleteEnrollment(enrollmentId);
                } catch (SQLException e) {
                    res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    return e.toString();
                }

                res.status(HttpStatus.OK_200);
                return "";
            });
        });
    }

    private static String enrollmentIdParam(Request req, Response res, Function<Integer, String> cb) {
        int enrollmentId;
        try {
            enrollmentId = Integer.parseInt(req.params(":enrollmentId"));
        } catch (NumberFormatException e) {
            res.status(HttpStatus.BAD_REQUEST_400);
            return "Invalid enrollment id";
        }
        return cb.apply(enrollmentId);
    }
}
