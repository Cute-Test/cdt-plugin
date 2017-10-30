package ch.hsr.ifs.mockator.plugin.extractinterface;

import java.util.Map;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;


@SuppressWarnings("restriction")
public class ExtractInterfaceContribution extends CRefactoringContribution {

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public ExtractInterfaceDescriptor createDescriptor(String id, String proj, String desc, String comment, Map args, int flags)
         throws IllegalArgumentException {
      return new ExtractInterfaceDescriptor(ExtractInterfaceRefactoring.ID, proj, desc, comment, args);
   }
}
