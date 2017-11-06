package ch.hsr.ifs.mockator.plugin.extractinterface;

import java.util.Map;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;


@SuppressWarnings("restriction")
public class ExtractInterfaceContribution extends CRefactoringContribution {

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public ExtractInterfaceDescriptor createDescriptor(final String id, final String proj, final String desc, final String comment, final Map args,
         final int flags) throws IllegalArgumentException {
      return new ExtractInterfaceDescriptor(ExtractInterfaceRefactoring.ID, proj, desc, comment, args);
   }
}
