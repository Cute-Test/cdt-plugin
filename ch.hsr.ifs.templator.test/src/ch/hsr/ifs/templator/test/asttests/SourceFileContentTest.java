package ch.hsr.ifs.templator.test.asttests;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;

public class SourceFileContentTest extends CDTTestingTest {

    @Override
    @Test
    public void runTest() throws Throwable {
        assertEquals("XY.cpp", activeFileName);
        assertEquals("#include <iostream>" + NL + NL + "int main() {}", fileMap.get(activeFileName).getSource());
        assertEquals("int main() {}", fileMap.get(activeFileName).getExpectedSource());
    }
}
