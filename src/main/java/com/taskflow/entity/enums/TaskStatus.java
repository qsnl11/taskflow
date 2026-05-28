package com.taskflow.entity.enums;

public enum TaskStatus {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    REVIEW("Review"),
    DONE("Done");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}