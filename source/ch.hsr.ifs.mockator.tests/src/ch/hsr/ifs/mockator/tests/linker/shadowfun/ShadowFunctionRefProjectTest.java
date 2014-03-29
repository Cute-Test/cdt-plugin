package ch.hsr.ifs.mockator.tests.linker.shadowfun;

import org.junit.Before;

public class ShadowFunctionRefProjectTest extends ShadowFunctionRefactoringTest {

  @Override
  @Before
  public void setUp() throws Exception {
    addReferencedProject("ExternalFunction", "ExternalFunction.rts");
    super.setUp();
  }
}
