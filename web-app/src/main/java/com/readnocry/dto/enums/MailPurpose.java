package com.readnocry.dto.enums;

public enum MailPurpose {

    ACCOUNT_ACTIVATION("User account activation"),
    TELEGRAM_CONNECTION("Connection telegram to account");

    private final String value;

    MailPurpose(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
