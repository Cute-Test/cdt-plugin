package ch.hsr.ifs.cute.it.tests.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;


public final class FileUtils {

    public static String getCodeFromIFile(IFile file) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
        StringBuilder code = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            code.append(line);
            code.append('\n');
        }
        br.close();
        return code.toString();
    }
}
