package com.sutromedia.android.lib.diagnostic;

class Log {

    static void v(final boolean enabled, final String tag, final String message, final Exception error) {
	v(enabled, tag, message);
	v(enabled, tag, error);
    }

    static void v(boolean enabled, final String tag, final String message) {
	if (enabled && message != null) {
	    android.util.Log.d(Tag.TAG, tag + ":" + message);
	}
    }

    static void v(boolean enabled, final String tag, final Exception error) {
	if (enabled && error != null) {
	    StackTraceElement[] elements = error.getStackTrace();
	    v(enabled, tag, "STACK TRACE");
	    for (StackTraceElement item : elements) {
		v(enabled, tag + "  - ", item.toString());
	    }
	}
    }

    static void printCurrentStackTrace(final boolean enabled, final String tag, final String message) {
	Exception error = new Exception();
	v(enabled, tag, message, error);	
    }

    static void printCurrentStackTrace(final boolean enabled, final String tag) {
	printCurrentStackTrace(enabled, tag, null);
    }
}