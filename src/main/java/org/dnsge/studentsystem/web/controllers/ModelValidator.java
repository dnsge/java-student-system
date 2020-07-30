package org.dnsge.studentsystem.web.controllers;

import org.dnsge.studentsystem.ex.ValidationException;

import java.util.function.BooleanSupplier;

public class ModelValidator {

    public static void validateAssignment(String name, int totalPoints) {
        validate(() -> within(1, name.length(), 255), "Assignment name must be between 1 and 255 characters long");
        validate(() -> totalPoints > 0, "Assignment total points must be greater than 0");
    }

    public static void validateCourse(String name, String room) {
        validate(() -> within(1, name.length(), 255), "Course name must be between 1 and 255 characters long");
        validate(() -> within(1, room.length(), 10), "Room name must be between 1 and 10 characters long");
    }

    public static void validateGrade(int points) {
        validate(() -> points >= 0, "Grade points must be 0 or greater");
    }

    private static void validate(BooleanSupplier booleanSupplier, String error) {
        if (!booleanSupplier.getAsBoolean())
            throw new ValidationException(error);
    }

    private static boolean within(int a, int b, int c) {
        return a <= b && b <= c;
    }

}
