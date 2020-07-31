package org.dnsge.studentsystem.web.controllers;

import org.dnsge.studentsystem.Environment;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.Assignment;
import org.dnsge.studentsystem.sql.model.Grade;
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

public class GradeController {

    public static String createGrade(Request req, Response res) {
        return ControllerUtil.mustGetUser(req, res, u -> {
            int studentId, points, assignmentId;
            try {
                String reqBody = req.body();
                JSONObject body = new JSONObject(reqBody);

                studentId = body.getInt("studentId");
                points = body.getInt("points");
                assignmentId = body.getInt("assignmentId");
            } catch (JSONException | NumberFormatException e) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid JSON\n" + e.toString();
            }

            ModelValidator.validateGrade(points);

            try {
                QueryManager qm = QueryManager.getQueryManager();
                Optional<Student> student = qm.getStudent(studentId);
                if (student.isEmpty()) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Student does not exist";
                }

                Optional<Assignment> assignment = qm.getAssignment(assignmentId);
                if (assignment.isEmpty()) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Assignment does not exist";
                }

                if (assignment.get().getCourse().getTeacherId() != u.getId() && Environment.enforcePermissions()) {
                    res.status(HttpStatus.FORBIDDEN_403);
                    return "";
                }

                qm.createGrade(studentId, assignmentId, points);
                res.status(HttpStatus.CREATED_201);
                return "";
            } catch (SQLIntegrityConstraintViolationException e) {
                res.status(HttpStatus.CONFLICT_409);
                return "A grade already exists for that student";
            } catch (SQLException e) {
                res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return e.toString();
            }
        });
    }

    public static String deleteGrade(Request req, Response res) {
        return gradeIdParam(req, res, gradeId -> {
            return ControllerUtil.mustGetUser(req, res, u -> {
                try {
                    QueryManager qm = QueryManager.getQueryManager();
                    Optional<Grade> grade = qm.getGrade(gradeId);
                    if (grade.isEmpty()) {
                        res.status(HttpStatus.NOT_FOUND_404);
                        return "";
                    }

                    if (grade.get().getTeacher().getId() != u.getId() && Environment.enforcePermissions()) {
                        res.status(HttpStatus.FORBIDDEN_403);
                        return "";
                    }

                    qm.deleteGrade(gradeId);
                } catch (SQLException e) {
                    res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    return e.toString();
                }

                res.status(HttpStatus.OK_200);
                return "";
            });
        });
    }

    private static String gradeIdParam(Request req, Response res, Function<Integer, String> cb) {
        int gradeId;
        try {
            gradeId = Integer.parseInt(req.params(":gradeId"));
        } catch (NumberFormatException e) {
            res.status(HttpStatus.BAD_REQUEST_400);
            return "Invalid grade id";
        }
        return cb.apply(gradeId);
    }

}
