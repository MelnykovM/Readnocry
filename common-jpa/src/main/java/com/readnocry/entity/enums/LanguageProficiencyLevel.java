package com.readnocry.entity.enums;

public enum LanguageProficiencyLevel {

    LOW("Beginner"),
    MIDDLE("Pre-intermediate"),
    HIGH("Intermediate");

    private final String displayLevelName;

    LanguageProficiencyLevel(String displayLevelName) {
        this.displayLevelName = displayLevelName;
    }

    public String getDisplayLevelName() {
        return displayLevelName;
    }
}
