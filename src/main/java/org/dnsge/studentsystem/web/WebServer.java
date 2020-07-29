package org.dnsge.studentsystem.web;

import org.apache.velocity.tools.generic.EscapeTool;
import org.dnsge.studentsystem.Util;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.*;
import org.dnsge.studentsystem.web.controllers.AssignmentController;
import org.dnsge.studentsystem.web.controllers.EnrollmentController;
import org.dnsge.studentsystem.web.controllers.GradeController;
import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class WebServer {

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

    public void run() {
        configure();
        setRoutes();
    }

    private void configure() {
        Spark.port(port);
    }

    private void setRoutes() {
        Spark.get("/login", (req, res) -> {
            res.header("Vary", "Cookie");

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
            res.header("Vary", "Cookie");

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
            res.header("Vary", "Cookie");

            Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
            if (authUser.isEmpty()) {
                res.redirect("/login", 302);
                return "";
            }
            User u = authUser.get();
            QueryManager qm = QueryManager.getQueryManager();

            if (u.getType() == 's') {
                Optional<Collection<Enrollment>> enrollments = qm.getStudentEnrollments(u.getId());

                if (enrollments.isEmpty()) {
                    return "Nothing here!";
                }

                Map<String, Object> model = new HashMap<>();
                model.put("enrollments", enrollments.get().toArray());
                return renderTemplate(model, "student_courses.vm");
            } else if (u.getType() == 't') {
                Optional<Collection<Course>> courses = qm.getTeacherCourses(u.getId());

                if (courses.isEmpty()) {
                    return "Nothing here!";
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
                res.header("Vary", "Cookie");

                int courseId;
                try {
                    courseId = Integer.parseInt(req.params(":courseId"));
                } catch (NumberFormatException e) {
                    return "Invalid course id";
                }

                Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
                if (authUser.isEmpty()) {
                    res.redirect("/login", 302);
                    return "";
                }

                User u = authUser.get();
                QueryManager qm = QueryManager.getQueryManager();

                Optional<Course> course = qm.getCourse(courseId);
                if (course.isEmpty())
                    return "Nothing here!";

                Map<String, Object> model = new HashMap<>();
                model.put("course", course.get());
                model.put("esc", new EscapeTool());
                if (u.getType() == 's') {
                    Optional<Collection<Assignment>> assignments = qm.getStudentAssignmentGrades(u.getId(), courseId);
                    if (assignments.isEmpty())
                        return "Nothing here!";

                    model.put("grade", Assignment.calculateAverageGrade(assignments.get()));
                    model.put("assignments", assignments.get().toArray());
                    return renderTemplate(model, "student_assignments.vm");
                } else if (u.getType() == 't') {
                    Optional<Collection<Assignment>> assignments = qm.getCourseAssignments(courseId);
                    if (assignments.isEmpty())
                        return "Nothing here!";

                    model.put("assignments", assignments.get().toArray());
                    return renderTemplate(model, "teacher_assignments.vm");
                } else {
                    return "Invalid user, please login again";
                }
            });
            Spark.get("/students", (req, res) -> {
                res.header("Vary", "Cookie");

                int courseId;
                try {
                    courseId = Integer.parseInt(req.params(":courseId"));
                } catch (NumberFormatException e) {
                    return "Invalid course id";
                }

                Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
                if (authUser.isEmpty()) {
                    res.redirect("/login", 302);
                    return "";
                }

                User u = authUser.get();
                QueryManager qm = QueryManager.getQueryManager();

                Optional<Course> course = qm.getCourse(courseId);
                if (course.isEmpty())
                    return "Nothing here!";

                Map<String, Object> model = new HashMap<>();
                model.put("course", course.get());
                model.put("esc", new EscapeTool());
                if (u.getType() == 's') {
                    return "Not implemented";
                } else if (u.getType() == 't') {
                    Optional<Collection<Enrollment>> enrollments = qm.getCourseEnrollments(courseId);
                    if (enrollments.isEmpty())
                        return "Nothing here!";

                    Optional<Collection<Period>> periods = qm.getPeriods();
                    if (periods.isEmpty())
                        return "Nothing here!";

                    model.put("enrollments", enrollments.get().toArray());
                    model.put("periods", periods.get().toArray());
                    return renderTemplate(model, "teacher_students.vm");
                } else {
                    return "Invalid user, please login again";
                }
            });
        });

        Spark.get("/assignments/:assignmentId/grades", (req, res) -> {
            res.header("Vary", "Cookie");

            int assignmentId;
            try {
                assignmentId = Integer.parseInt(req.params(":assignmentId"));
            } catch (NumberFormatException e) {
                return "Invalid assignment id";
            }

            Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
            if (authUser.isEmpty()) {
                res.redirect("/login", 302);
                return "";
            }

            User u = authUser.get();
            QueryManager qm = QueryManager.getQueryManager();

            Optional<Assignment> assignment = qm.getAssignment(assignmentId);
            if (assignment.isEmpty())
                return "Nothing here!";

            Optional<Collection<Grade>> grades = qm.getTeacherAssignmentGrades(assignmentId);
            if (grades.isEmpty())
                return "Nothing here!";

            Map<String, Object> model = new HashMap<>();
            model.put("assignment", assignment.get());
            model.put("average", Grade.calculateAverageGrade(grades.get(), assignment.get()));
            model.put("grades", grades.get().toArray());
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
        });
    }
}
