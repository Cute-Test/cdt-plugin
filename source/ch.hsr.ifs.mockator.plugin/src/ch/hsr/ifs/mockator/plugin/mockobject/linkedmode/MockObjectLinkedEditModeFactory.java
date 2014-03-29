package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import java.util.Collection;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;

public class MockObjectLinkedEditModeFactory {
  private final ChangeEdit edit;
  private final Collection<? extends TestDoubleMemFun> memFuns;
  private final CppStandard cppStd;
  private final AssertionOrder assertOrder;
  private final Maybe<String> expectationsName;

  public MockObjectLinkedEditModeFactory(ChangeEdit edit,
      Collection<? extends TestDoubleMemFun> memFuns, CppStandard cppStd,
      AssertionOrder assertOrder, Maybe<String> expectationsVectorName) {
    this.edit = edit;
    this.memFuns = memFuns;
    this.cppStd = cppStd;
    this.assertOrder = assertOrder;
    this.expectationsName = expectationsVectorName;
  }

  public LinkedModeInfoCreater getLinkedModeInfoCreator(LinkedEditModeStrategy editStrategy) {
    switch (editStrategy) {
      case ChooseArguments:
        return new FunArgumentsLinkedEditMode(edit, memFuns, cppStd, assertOrder, expectationsName);
      case ChooseFunctions:
        return new FunSignaturesLinkedEditMode(edit, memFuns, cppStd, assertOrder, expectationsName);
      default:
        throw new MockatorException("Unexpected linked edit mode strategy");
    }
  }
}
