package ch.hsr.ifs.templator.test.testhelpertest;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTProjectTest;
import ch.hsr.ifs.templator.test.TestHelper;

public class GetCommentAboveTest extends CDTProjectTest {

    @Test
    // The Comment that i wrote
    // Is 2 Lines long
    public void testGetCommentAbove() throws Exception {

        String compareString = " The Comment that i wrote\n" + " Is 2 Lines long\n";

        String readComment = TestHelper.getCommentAbove(getClass());

        assertEquals(compareString, readComment);
    }

    @Override
    protected void setupFiles() throws Exception {
    }

    @Override
    protected void initReferencedProjects() throws Exception {
    }
}
