package de.fau.cs.mad.yasme.android.controller;

public class Sanitizer {
    private static String regex = "A-Za-z0-9\\s";

    public static String sanitize(String input) {
        // Replace all non-alphanumerical and non-whitespace characters
        return input.replaceAll("[^"+regex+"]","");
    }

    public static String sanitizeExtra(String input, String extra) {
        return input.replaceAll("[^"+regex+extra+"]","");
    }

    public String getRegex() { return this.regex; }
}
