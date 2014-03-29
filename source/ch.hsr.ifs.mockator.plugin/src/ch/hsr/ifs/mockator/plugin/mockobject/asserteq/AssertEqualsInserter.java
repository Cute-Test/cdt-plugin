package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;

public class AssertEqualsInserter {
  private final MockSupportContext context;

  public AssertEqualsInserter(MockSupportContext context) {
    this.context = context;
  }

  public void insertAssertEqual() {
    for (ICPPASTFunctionDefinition testFunction : context.getReferencingFunctions()) {
      getAssertEqualsStrategy(testFunction).insertAssertEqual(context.getRewriter());
    }
  }

  private AbstractAssertEqualsInserter getAssertEqualsStrategy(
      ICPPASTFunctionDefinition testFunction) {
    if (isCuteProject())
      return new CuteAssertEqualsInserter(testFunction, context);
    else
      return new CAssertEqualsInserter(testFunction, context);
  }

  private boolean isCuteProject() {
    return new NatureHandler(context.getProject().getProject())
        .hasNature(MockatorConstants.CUTE_NATURE);
  }
}
