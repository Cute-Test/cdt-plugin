package ch.hsr.ifs.cute.mockator.base.util;

import java.io.File;
import java.util.regex.Matcher;


public abstract class PlatformUtil {

    public static final String PATH_SEGMENT_SEPARATOR = File.separator;
    public static final String SYSTEM_NEW_LINE        = System.getProperty("line.separator");

    public static String toSystemNewLine(final String str) {
        return str.replaceAll("%n", Matcher.quoteReplacement(SYSTEM_NEW_LINE));
    }
}
