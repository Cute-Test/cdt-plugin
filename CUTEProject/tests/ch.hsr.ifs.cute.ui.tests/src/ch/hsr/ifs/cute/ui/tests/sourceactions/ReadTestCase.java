/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.tests.sourceactions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.ui.tests.UiTestPlugin;


public class ReadTestCase {// TODO checking for null values

    public ArrayList<String>  testname  = new ArrayList<>();
    public ArrayList<Integer> cursorpos = new ArrayList<>();
    public ArrayList<String>  test      = new ArrayList<>();
    public ArrayList<String>  expected  = new ArrayList<>();
    public ArrayList<String>  parameter = new ArrayList<>();

    enum state {
        TEST, SAVETEST, EXPECTED, SAVEEXPECTED, CURSOR, PARAMETER
    };

    state m;

    public ReadTestCase(String file, boolean parseCursor) {
        StringBuilder builder = new StringBuilder();

        String newline = System.getProperty("line.separator");
        try {
            String testnametmp = null;
            BufferedReader br = readTest(file);
            while (br.ready()) {
                String str = br.readLine();
                if (str.startsWith("//test")) {
                    m = state.SAVEEXPECTED;
                    testnametmp = str.substring(6);
                }
                if (str.startsWith("//expected")) {
                    m = state.SAVETEST;
                    continue;
                }
                if (str.startsWith("//parameter ")) {
                    int startoffset = "//parameter ".length();
                    parameter.add(str.substring(startoffset));
                    continue;
                }
                if (str.startsWith("//") && m != state.SAVEEXPECTED) continue;

                switch (m) {
                case TEST:
                    builder.append(str + newline);
                    break;
                case SAVETEST:
                    testname.add(testnametmp);
                    test.add(builder.toString());
                    builder = new StringBuilder();
                    m = state.EXPECTED;
                case EXPECTED:
                    builder.append(str + newline);
                    break;
                case SAVEEXPECTED:
                    if (builder.length() > 0) {
                        expected.add(builder.toString());
                        builder = new StringBuilder();
                    }
                    m = state.TEST;
                    break;
                default:
                    break;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // handle the last expected
        if (builder.length() > 0) {
            expected.add(builder.toString());
            builder = new StringBuilder();
        }
        /*
         * parameter.add(" foo cow4"); parameter.add(" foo cow4");
         */

        if (parseCursor) parseForCursorPos();
    }

    public ReadTestCase(String file) {
        this(file, true);
    }

    public static BufferedReader readTest(String file) throws IOException {
        Bundle bundle1 = UiTestPlugin.getDefault().getBundle();
        Path path1 = new Path(file);
        URL url1 = FileLocator.toFileURL(FileLocator.find(bundle1, path1, null));
        BufferedReader br = new BufferedReader(new InputStreamReader(url1.openStream()));
        return br;
    }

    public void parseForCursorPos() {
        for (String str : test) {
            cursorpos.add(str.indexOf("^"));
        }
        removeCaretFromTest();
    }

    public Integer[] parseForMultiCursorPosition() {
        ArrayList<Integer> result = new ArrayList<>();
        for (String str : test) {
            int x = 0;
            while (x != -1) {
                x = str.indexOf("^", x);
                if (x != -1) {
                    result.add(x);
                    x += 1;
                }
            }
        }

        return result.toArray(new Integer[0]);
    }

    public void removeCaretFromTest() {
        for (int i = 0; i < test.size(); i++) {
            String str = test.get(i);
            test.remove(i);
            test.add(i, str.replaceAll("[\\^]", ""));
        }
    }
}
