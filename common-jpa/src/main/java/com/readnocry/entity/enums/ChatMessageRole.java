package com.readnocry.entity.enums;

public enum ChatMessageRole {

    USER("user"),
    ASSISTANT("assistant");

    private final String role;

    ChatMessageRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}