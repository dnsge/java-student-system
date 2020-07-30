package org.dnsge.studentsystem.web.controllers;

import org.dnsge.studentsystem.Environment;
import org.dnsge.studentsystem.ex.ValidationException;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.Assignment;
import org.dnsge.studentsystem.sql.model.Course;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("CodeBlock2Expr")
public class AssignmentController {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String createAssignment(Request req, Response res) {
        return ControllerUtil.mustGetUser(req, res, u -> {
            String name, dueDateString;
            Date dueDate;
            int totalPoints, courseId;
            try {
                String reqBody = req.body();
                JSONObject body = new JSONObject(reqBody);

                name = body.getString("name").trim();
                dueDateString = body.getString("dueDate");
                totalPoints = body.getInt("totalPoints");
                courseId = body.getInt("courseId");

                dueDate = new Date(dateFormat.parse(dueDateString).getTime());
            } catch (JSONException | NumberFormatException e) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid JSON\n" + e.toString();
            } catch (ParseException e) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid date";
            }

            ModelValidator.validateAssignment(name, totalPoints);

            QueryManager qm = QueryManager.getQueryManager();
            try {
                Optional<Course> course = qm.getCourse(courseId);
                if (course.isEmpty()) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Course does not exist";
                }

                if (course.get().getTeacherId() != u.getId() && Environment.enforcePermissions()) {
                    res.status(HttpStatus.FORBIDDEN_403);
                    return "";
                }

                qm.createAssignment(name, totalPoints, dueDate, courseId);
                res.status(HttpStatus.CREATED_201);
                return "";
            } catch (SQLException e) {
                res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return e.toString();
            }
        });
    }

    public static String updateAssignment(Request req, Response res) {
        return assignmentIdParam(req, res, assignmentId -> {
            return ControllerUtil.mustGetUser(req, res, u -> {
                String name, dueDateString;
                Date dueDate;
                int totalPoints;
                try {
                    String reqBody = req.body();
                    JSONObject body = new JSONObject(reqBody);

                    name = body.getString("name");
                    dueDateString = body.getString("dueDate");
                    totalPoints = body.getInt("totalPoints");

                    dueDate = new Date(dateFormat.parse(dueDateString).getTime());
                } catch (JSONException | NumberFormatException e) {
                    System.err.println(e.toString());
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Invalid JSON";
                } catch (ParseException e) {
                    res.status(HttpStatus.BAD_REQUEST_400);
                    return "Invalid date";
                }

                ModelValidator.validateAssignment(name, totalPoints);

                QueryManager qm = QueryManager.getQueryManager();
                try {
                    Optional<Course> course = qm.getCourseFromAssignment(assignmentId);
                    if (course.isEmpty()) {
                        res.status(HttpStatus.BAD_REQUEST_400);
                        return "Course does not exist";
                    }

                    if (course.get().getTeacherId() != u.getId() && Environment.enforcePermissions()) {
                        res.status(HttpStatus.FORBIDDEN_403);
                        return "";
                    }

                    qm.updateAssignment(assignmentId, name, totalPoints, dueDate);
                    res.status(HttpStatus.OK_200);
                    return "";
                } catch (SQLException e) {
                    res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    return e.toString();
                }
            });
        });
    }

    public static String deleteAssignment(Request req, Response res) {
        return assignmentIdParam(req, res, assignmentId -> {
            return ControllerUtil.mustGetUser(req, res, u -> {
                QueryManager qm = QueryManager.getQueryManager();
                try {
                    Optional<Assignment> assignment = qm.getAssignment(assignmentId);
                    if (assignment.isEmpty()) {
                        res.status(HttpStatus.NOT_FOUND_404);
                        return "";
                    }

                    if (assignment.get().getCourse().getTeacherId() != u.getId() && Environment.enforcePermissions()) {
                        res.status(HttpStatus.FORBIDDEN_403);
                        return "";
                    }

                    qm.deleteAssignment(assignmentId);
                } catch (SQLException e) {
                    res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    return e.toString();
                }

                res.status(HttpStatus.OK_200);
                return "";
            });
        });
    }

    private static String assignmentIdParam(Request req, Response res, Function<Integer, String> cb) {
        int assignmentId;
        try {
            assignmentId = Integer.parseInt(req.params(":assignmentId"));
        } catch (NumberFormatException e) {
            res.status(HttpStatus.BAD_REQUEST_400);
            return "Invalid assignment id";
        }
        return cb.apply(assignmentId);
    }
}
