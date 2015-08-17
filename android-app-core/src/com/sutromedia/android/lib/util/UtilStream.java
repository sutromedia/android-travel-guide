package com.sutromedia.android.lib.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class UtilStream {

    private static final int BUFFER_SIZE = 1024;

    public static void copy(
        final InputStream in,
        final OutputStream out,
        final int bufferSize) throws IOException {

        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        in.close();
    }

    public static void copy(
        final InputStream in,
        final OutputStream out) throws IOException {

        copy(in, out, BUFFER_SIZE);
    }
}
