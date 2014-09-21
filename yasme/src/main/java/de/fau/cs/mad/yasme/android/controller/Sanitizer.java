package de.fau.cs.mad.yasme.android.controller;

public class Sanitizer {
    public static String sanitize(String input) {
        // Replace all non-alphanumerical and non-whitespace characters
        return input.replaceAll("[^A-Za-z0-9\\s]","");
    }

    public static String sanitizeExtra(String input, String extra) {
        return input.replaceAll("[^A-Za-z0-9\\s"+extra+"]","");
    }
}
