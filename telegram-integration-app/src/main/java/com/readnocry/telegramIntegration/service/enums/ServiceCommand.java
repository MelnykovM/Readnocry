package com.readnocry.telegramIntegration.service.enums;

public enum ServiceCommand {

    HELP("help"),
    REGISTRATION("registration"),
    CANCEL("cancel"),
    START("start"),
    CHAT("chatGPT"),
    CLEAN_HISTORY("clean history"),
    SHOW_HISTORY("show history"),
    SETUP_NOT_SAVE_HISTORY("setup not save history"),
    SETUP_SAVE_HISTORY("setup save history");

    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String v) {
        for (ServiceCommand c : ServiceCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
