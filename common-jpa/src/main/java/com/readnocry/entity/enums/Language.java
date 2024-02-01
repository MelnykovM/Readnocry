package com.readnocry.entity.enums;

public enum Language {

    SPANISH("Spanish"),
    FRENCH("French"),
    GERMAN("German"),
    ITALIAN("Italian"),
    PORTUGUESE("Portuguese"),
    DUTCH("Dutch"),
    RUSSIAN("Russian"),
    CHINESE_SIMPLIFIED("Chinese (Simplified)"),
    CHINESE_TRADITIONAL("Chinese (Traditional)"),
    JAPANESE("Japanese"),
    KOREAN("Korean"),
    ARABIC("Arabic"),
    HINDI("Hindi"),
    TURKISH("Turkish"),
    SWEDISH("Swedish"),
    NORWEGIAN("Norwegian"),
    DANISH("Danish"),
    FINNISH("Finnish"),
    GREEK("Greek"),
    POLISH("Polish"),
    ROMANIAN("Romanian"),
    HUNGARIAN("Hungarian"),
    CZECH("Czech"),
    THAI("Thai"),
    VIETNAMESE("Vietnamese"),
    UKRAINIAN("Ukrainian"),
    MALAY("Malay"),
    INDONESIAN("Indonesian"),
    HEBREW("Hebrew"),
    FILIPINO("Filipino"),
    BENGALI("Bengali"),
    URDU("Urdu"),
    SWAHILI("Swahili");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
