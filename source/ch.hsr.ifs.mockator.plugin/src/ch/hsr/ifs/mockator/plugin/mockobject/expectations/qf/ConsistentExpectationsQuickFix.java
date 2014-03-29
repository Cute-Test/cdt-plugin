package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorQfWithRefactoringSupport;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;

public class ConsistentExpectationsQuickFix extends MockatorQfWithRefactoringSupport {

  @Override
  public Image getImage() {
    return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CONFIG);
  }

  @Override
  public String getDescription() {
    return getCodanArguments(marker).getResolutionDesc();
  }

  @Override
  public String getLabel() {
    return I18N.InconsistentExpectationsQuickfix;
  }

  @Override
  protected ConsistentExpectationsCodanArgs getCodanArguments(IMarker marker) {
    return new ConsistentExpectationsCodanArgs(marker);
  }

  @Override
  protected MockatorRefactoring getRefactoring(ICElement cElement, ITextSelection selection,
      CodanArguments ca) {
    return new ConsistentExpectationsRefactoring(cElement, selection, getCProject(),
        (ConsistentExpectationsCodanArgs) ca, getCppStandard(), getLinkedEditStrategy());
  }

  private LinkedEditModeStrategy getLinkedEditStrategy() {
    AssertionOrder assertionOrder = getAssertionOrderFor();
    return assertionOrder.getLinkedEditModeStrategy(getCProject().getProject());
  }

  private AssertionOrder getAssertionOrderFor() {
    return AssertionOrder.fromProjectSettings(getCProject().getProject());
  }

  @Override
  protected Maybe<LinkedModeInfoCreater> getLinkedModeCreator(ChangeEdit edit, IDocument document,
      MockatorRefactoring refactoring) {
    MockObjectLinkedEditModeFactory factory =
        new MockObjectLinkedEditModeFactory(edit, getExpectationsToAdd(refactoring),
            getCppStandard(), getAssertionOrderFor(), Maybe.<String>none());
    return maybe(factory.getLinkedModeInfoCreator(getLinkedEditStrategy()));
  }

  private static Collection<ExistingTestDoubleMemFun> getExpectationsToAdd(
      MockatorRefactoring refactoring) {
    return ((ConsistentExpectationsRefactoring) refactoring).getExpectationsToAdd();
  }
}
