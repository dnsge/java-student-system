package org.dnsge.studentsystem.web.controllers;

import org.dnsge.studentsystem.Environment;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.Course;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

public class CourseController {

    public static String createCourse(Request req, Response res) {
        return ControllerUtil.mustGetUser(req, res, u -> {
            String name, room;
            try {
                String reqBody = req.body();
                JSONObject body = new JSONObject(reqBody);

                name = body.getString("name");
                room = body.getString("room");
            } catch (JSONException | NumberFormatException e) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid JSON\n" + e.toString();
            }

            if (u.getType() != 't') {
                res.status(HttpStatus.FORBIDDEN_403);
                return "";
            }

            QueryManager qm = QueryManager.getQueryManager();
            try {
                qm.createCourse(name, room, u.getId());
                res.status(HttpStatus.CREATED_201);
                return "";
            } catch (SQLException e) {
                res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return e.toString();
            }
        });
    }

    public static String deleteCourse(Request req, Response res) {
        return courseIdParam(req, res, courseId -> {
            return ControllerUtil.mustGetUser(req, res, u -> {
                QueryManager qm = QueryManager.getQueryManager();
                try {
                    Optional<Course> course = qm.getCourse(courseId);
                    if (course.isEmpty()) {
                        res.status(HttpStatus.NOT_FOUND_404);
                        return "";
                    }

                    if (course.get().getTeacherId() != u.getId() && Environment.enforcePermissions()) {
                        res.status(HttpStatus.FORBIDDEN_403);
                        return "";
                    }

                    qm.deleteCourse(courseId);
                } catch (SQLException e) {
                    res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    return e.toString();
                }

                res.status(HttpStatus.OK_200);
                return "";
            });
        });
    }

    private static String courseIdParam(Request req, Response res, Function<Integer, String> cb) {
        int courseId;
        try {
            courseId = Integer.parseInt(req.params(":courseId"));
        } catch (NumberFormatException e) {
            res.status(HttpStatus.BAD_REQUEST_400);
            return "Invalid course id";
        }
        return cb.apply(courseId);
    }

}
