package com.sutromedia.android.lib.diagnostic;

public class Debug extends Log {

    private static boolean sEnable = false;

    public static void enable(boolean value) {
	sEnable = value;
    }

    public static void v(final String message) {
	v(sEnable, Tag.TAG_DEBUG, message, null);
    }

    public static void v(final String message, final Exception error) {
	v(sEnable, Tag.TAG_DEBUG, null, error);
	v(message);
    }

    public static void printStackTrace(final Exception error) {
	v(sEnable, Tag.TAG_DEBUG, null, error);
    }

    public static void printStackTrace() {
	printCurrentStackTrace(sEnable, Tag.TAG_DEBUG);
    }
}