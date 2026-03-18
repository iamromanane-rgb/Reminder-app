package com.reminder.backend.models;

public enum AccessLevel {
    READ("read"),
    READ_WRITE("read_write");

    private final String claimValue;

    AccessLevel(String claimValue) {
        this.claimValue = claimValue;
    }

    public String getClaimValue() {
        return claimValue;
    }

    public static AccessLevel fromInput(String value) {
        if (value == null || value.isBlank()) {
            return READ;
        }

        String normalized = value.trim().toLowerCase()
                .replace('-', '_')
                .replace('/', '_')
                .replace(' ', '_');

        if ("read".equals(normalized)) {
            return READ;
        }
        if ("read_write".equals(normalized) || "readwrite".equals(normalized)) {
            return READ_WRITE;
        }

        throw new IllegalArgumentException("invalid access level");
    }
}
