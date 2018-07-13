package ch.hsr.ifs.mockator.plugin.extractinterface;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoring;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringDescriptor;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


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
      final boolean doReplace = Boolean.parseBoolean(getParameterMap().get(REPLACE_ALL_OCCURENCES));
      final String name = getParameterMap().get(NEW_INTERFACE_NAME);
      return new ExtractInterfaceContext.ContextBuilder(getTranslationUnit(), getSelection(), getCProject()).withRefactoringStatus(status)
            .replaceAllOccurences(doReplace).withNewInterfaceName(name).build();
   }
}
