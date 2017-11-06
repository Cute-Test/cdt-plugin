package ch.hsr.ifs.mockator.plugin.extractinterface;

import java.util.Map;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


@SuppressWarnings("restriction")
public class ExtractInterfaceDescriptor extends CRefactoringDescriptor {

   public static final String NEW_INTERFACE_NAME     = "name";
   public static final String REPLACE_ALL_OCCURENCES = "replace";

   public ExtractInterfaceDescriptor(final String id, final String project, final String desc, final String comment,
                                     final Map<String, String> arguments) {
      super(id, project, desc, comment, MULTI_CHANGE, arguments);
   }

   @Override
   public CRefactoring createRefactoring(final RefactoringStatus status) throws CoreException {
      return new ExtractInterfaceRefactoring(createContext(status));
   }

   private ExtractInterfaceContext createContext(final RefactoringStatus status) throws CoreException {
      final boolean doReplace = Boolean.parseBoolean(arguments.get(REPLACE_ALL_OCCURENCES));
      final String name = arguments.get(NEW_INTERFACE_NAME);
      return new ExtractInterfaceContext.ContextBuilder(getTranslationUnit(), getCProject(), (ITextSelection) getSelection()).withRefactoringStatus(
            status).replaceAllOccurences(doReplace).withNewInterfaceName(name).build();
   }
}
