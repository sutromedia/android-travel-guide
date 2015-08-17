package com.sutromedia.android.lib.diagnostic;

public class Report extends Log {

    private static boolean sEnable = false;

    public static void enable(boolean value) {
	sEnable = value;
    }

    public static void logUnexpectedException(
	final String message,
	final Exception error) {
	
	v(sEnable, Tag.TAG_REPORT, message, error);
    }

    public static void logUnexpectedException(final Exception error) {
	v(sEnable, Tag.TAG_REPORT, null, error);
    }

    public static void handleUnexpected(final String message) {
	v(sEnable, Tag.TAG_REPORT, message, null);
	printCurrentStackTrace(sEnable, Tag.TAG_REPORT);
    }

    public static void v(final String message) {
	v(sEnable, Tag.TAG_REPORT, message, null);
    }
}