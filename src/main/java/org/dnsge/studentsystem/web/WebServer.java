package org.dnsge.studentsystem.web;

import org.apache.velocity.tools.generic.EscapeTool;
import org.dnsge.studentsystem.ex.BadRequestException;
import org.dnsge.studentsystem.ex.NotAuthenticatedException;
import org.dnsge.studentsystem.ex.NotFoundException;
import org.dnsge.studentsystem.Util;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.*;
import org.dnsge.studentsystem.web.controllers.AssignmentController;
import org.dnsge.studentsystem.web.controllers.CourseController;
import org.dnsge.studentsystem.web.controllers.EnrollmentController;
import org.dnsge.studentsystem.web.controllers.GradeController;
import org.eclipse.jetty.http.HttpStatus;
import spark.ModelAndView;
import spark.Request;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.SQLException;
import java.util.*;


public class WebServer {

    private static final String errorTemplate = "<html lang=\"en\"><body><h2>%s</h2></body></html>";
    private static final String errorMessage = "An unknown error occurred. Try again later";

    private final int port;

    public WebServer(int port) {
        this.port = port;
    }

    private static String renderTemplate(Map<String, Object> model, String view) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, view));
    }

    private static String renderTemplate(String view) {
        Map<String, Object> m = new HashMap<>();
        return renderTemplate(m, view);
    }

    private static Optional<Integer> parseParamInt(Request req, String param) {
        try {
            int val = Integer.parseInt(req.params(param));
            return Optional.of(val);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public void run() {
        configure();
        setRoutes();
    }

    private void configure() {
        Spark.port(port);
    }

    private void setRoutes() {
        Spark.exception(NoSuchElementException.class, (ex, req, res) -> {
            res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            res.type("text/html");
            res.body(String.format(errorTemplate, errorMessage));
        });

        Spark.exception(NotFoundException.class, (ex, req, res) -> {
            res.status(HttpStatus.NOT_FOUND_404);
            res.body(renderTemplate("not_found.vm"));
        });

        Spark.exception(NotAuthenticatedException.class, (ex, req, res) -> {
            res.redirect("/login", 302);
            res.body("");
        });

        Spark.exception(BadRequestException.class, (ex, req, res) -> {
            res.status(400);
            if (ex.getMessage() != null) {
                res.body(ex.getMessage());
            } else {
                res.body("Bad request");
            }
        });

        Spark.exception(SQLException.class, (ex, req, res) -> {
            System.err.printf("SQLException: %s\n", ex.toString());
            res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            res.type("text/html");
            res.body(String.format(errorTemplate, errorMessage));
        });

        Spark.after((req, res) -> res.header("Vary", "Cookie"));

        Spark.redirect.get("/", "/home");
        Spark.get("/login", (req, res) -> {
            Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
            if (authUser.isPresent()) {
                res.redirect("/home", 302);
                return "";
            } else {
                Map<String, Object> model = new HashMap<>();
                String error = req.cookie("error");
                if (error != null && !error.equals("")) {
                    model.put("error", Util.urlDecodeString(error));
                    res.removeCookie("/login", "error");
                }
                return renderTemplate(model, "loginPage.vm");
            }
        });

        Spark.get("/register", (req, res) -> {
            Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
            if (authUser.isPresent()) {
                res.redirect("/home", 302);
                return "";
            } else {
                Map<String, Object> model = new HashMap<>();
                String error = req.cookie("error");
                if (error != null && !error.equals("")) {
                    model.put("error", Util.urlDecodeString(error));
                    res.removeCookie("/register", "error");
                }
                return renderTemplate(model, "registerPage.vm");
            }
        });

        Spark.get("/home", (req, res) -> {
            User u = AuthenticationManager.getAuthenticatedUser(req).orElseThrow(NotAuthenticatedException::new);
            QueryManager qm = QueryManager.getQueryManager();

            if (u.getType() == 's') {
                Collection<Enrollment> enrollments = qm.getStudentEnrollments(u.getId()).orElseThrow();

                Map<String, Object> model = new HashMap<>();
                model.put("enrollments", enrollments.toArray());
                return renderTemplate(model, "student_courses.vm");
            } else if (u.getType() == 't') {
                Optional<Collection<Course>> courses = qm.getTeacherCourses(u.getId());

                if (courses.isEmpty()) {
                    res.status(500);
                    return errorMessage;
                }

                Map<String, Object> model = new HashMap<>();
                model.put("courses", courses.get().toArray());
                return renderTemplate(model, "teacher_courses.vm");
            } else {
                return "Invalid user, please login again";
            }
        });

        Spark.path("/courses/:courseId", () -> {
            Spark.get("/assignments", (req, res) -> {
                int courseId = parseParamInt(req, ":courseId")
                        .orElseThrow(() -> new BadRequestException("Invalid course id"));

                User u = AuthenticationManager.getAuthenticatedUser(req).orElseThrow(NotAuthenticatedException::new);
                QueryManager qm = QueryManager.getQueryManager();
                Course course = qm.getCourse(courseId).orElseThrow(NotFoundException::new);

                Map<String, Object> model = new HashMap<>();
                model.put("course", course);
                model.put("esc", new EscapeTool());
                if (u.getType() == 's') {
                    Collection<Assignment> assignments = qm.getStudentAssignmentGrades(u.getId(), courseId).orElseThrow();
                    model.put("grade", Assignment.calculateAverageGrade(assignments));
                    model.put("assignments", assignments.toArray());
                    return renderTemplate(model, "student_assignments.vm");
                } else if (u.getType() == 't') {
                    Collection<Assignment> assignments = qm.getCourseAssignments(courseId).orElseThrow();
                    model.put("assignments", assignments.toArray());
                    return renderTemplate(model, "teacher_assignments.vm");
                } else {
                    return "Invalid user, please login again";
                }
            });
            Spark.get("/students", (req, res) -> {
                int courseId = parseParamInt(req, ":courseId")
                        .orElseThrow(() -> new BadRequestException("Invalid course id"));

                User u = AuthenticationManager.getAuthenticatedUser(req).orElseThrow(NotAuthenticatedException::new);
                QueryManager qm = QueryManager.getQueryManager();
                Course course = qm.getCourse(courseId).orElseThrow(NotFoundException::new);

                Map<String, Object> model = new HashMap<>();
                model.put("course", course);
                model.put("esc", new EscapeTool());
                if (u.getType() == 's') {
                    res.status(403);
                    return errorMessage;
                } else if (u.getType() == 't') {
                    Collection<Enrollment> enrollments = qm.getCourseEnrollments(courseId).orElseThrow();
                    Collection<Period> periods = qm.getPeriods().orElseThrow();
                    Collection<Student> allStudents = qm.getStudentsNotEnrolledInCourse(courseId).orElseThrow();
                    model.put("enrollments", enrollments.toArray());
                    model.put("periods", periods.toArray());
                    model.put("students", allStudents.toArray());
                    return renderTemplate(model, "teacher_students.vm");
                } else {
                    return "Invalid user, please login again";
                }
            });
        });

        Spark.get("/assignments/:assignmentId/grades", (req, res) -> {
            int assignmentId = parseParamInt(req, ":assignmentId")
                    .orElseThrow(() -> new BadRequestException("Invalid assignment id"));

            User u = AuthenticationManager.getAuthenticatedUser(req).orElseThrow(NotAuthenticatedException::new);

            QueryManager qm = QueryManager.getQueryManager();
            Assignment assignment = qm.getAssignment(assignmentId).orElseThrow(NotFoundException::new);
            Collection<Grade> grades = qm.getTeacherAssignmentGrades(assignmentId).orElseThrow();
            Collection<Student> allStudents = qm.getStudentsWithoutAssignmentGrade(assignment.getCourseId(), assignmentId).orElseThrow();

            Map<String, Object> model = new HashMap<>();
            model.put("assignment", assignment);
            model.put("average", Grade.calculateAverageGrade(grades, assignment));
            model.put("grades", grades.toArray());
            model.put("students", allStudents.toArray());
            model.put("esc", new EscapeTool());
            if (u.getType() == 's') {
                return "Not implemented";
            } else if (u.getType() == 't') {
                return renderTemplate(model, "teacher_grades.vm");
            } else {
                return "Invalid user, please login again";
            }
        });

        // web api
        Spark.path("/api", () -> {
            Spark.post("/login", AuthenticationManager::handleLogin);
            Spark.post("/register", AuthenticationManager::handleRegister);
            Spark.get("/logout", AuthenticationManager::handleLogout);
            Spark.path("/assignments", () -> {
                Spark.post("/", AssignmentController::createAssignment);
                Spark.put("/:assignmentId", AssignmentController::updateAssignment);
                Spark.delete("/:assignmentId", AssignmentController::deleteAssignment);
            });
            Spark.path("/grades", () -> {
                Spark.post("/", GradeController::createGrade);
                Spark.delete("/:gradeId", GradeController::deleteGrade);
            });
            Spark.path("/enrollments", () -> {
                Spark.post("/", EnrollmentController::createEnrollment);
                Spark.delete("/:enrollmentId", EnrollmentController::deleteEnrollment);
            });
            Spark.path("/courses", () -> {
                Spark.post("/", CourseController::createCourse);
                Spark.delete("/:courseId", CourseController::deleteCourse);
            });
        });
    }
}
