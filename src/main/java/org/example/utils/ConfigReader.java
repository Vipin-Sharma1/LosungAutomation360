package org.example.utils;

public final class ConfigReader {
    private static final String LOGIN_URL = "https://sso.losung360.com/login?referrer=https%3A%2F%2Fapp.losung360.com%2F";
    private static final String DEBUGGER_ADDRESS = "127.0.0.1:9222";

    private ConfigReader() {
    }

    public static String loginMobile() {
        return value("LOSUNG_LOGIN_MOBILE", "8824029491");
    }

    public static String otp() {
        return System.getenv("LOSUNG_OTP");
    }

    public static String loginUrl() {
        return LOGIN_URL;
    }

    public static String debuggerAddress() {
        return DEBUGGER_ADDRESS;
    }

    public static int timeoutSeconds() {
        return Integer.parseInt(value("LOSUNG_TIMEOUT_SECONDS", "30"));
    }

    public static boolean isCiMode() {
        return "true".equalsIgnoreCase(System.getenv("CI"))
                || "true".equalsIgnoreCase(System.getenv("CI_MODE"));
    }

    private static String value(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
