package ch.hsr.ifs.cute.gcov.util;

import java.io.Closeable;


public final class StreamUtil {

    private StreamUtil() {}

    public static void tryClose(Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {}
        }
    }
}
