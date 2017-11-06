package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import java.util.Collection;
import java.util.Optional;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;


public class MockObjectLinkedEditModeFactory {

   private final ChangeEdit                             edit;
   private final Collection<? extends TestDoubleMemFun> memFuns;
   private final CppStandard                            cppStd;
   private final AssertionOrder                         assertOrder;
   private final Optional<String>                       expectationsName;

   public MockObjectLinkedEditModeFactory(final ChangeEdit edit, final Collection<? extends TestDoubleMemFun> memFuns, final CppStandard cppStd,
                                          final AssertionOrder assertOrder, final Optional<String> expectationsVectorName) {
      this.edit = edit;
      this.memFuns = memFuns;
      this.cppStd = cppStd;
      this.assertOrder = assertOrder;
      expectationsName = expectationsVectorName;
   }

   public LinkedModeInfoCreater getLinkedModeInfoCreator(final LinkedEditModeStrategy editStrategy) {
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
